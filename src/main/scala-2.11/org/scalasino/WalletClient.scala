package org.scalasino

import akka.actor.ActorRef
import akka.actor.FSM.->
import akka.persistence.fsm.PersistentFSM
import akka.persistence.query.journal.leveldb.scaladsl.LeveldbReadJournal
import akka.persistence.query.{EventEnvelope, PersistenceQuery}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import org.scalasino.model._

import scala.reflect.ClassTag

class WalletClient(playerId: String, wallet: ActorRef) extends PersistentFSM[WalletClientState, WalletClientData, WalletEvent] {

  override def persistenceId: String = playerId

  override implicit def domainEventClassTag = ClassTag(classOf[WalletEvent])

  startWith(TransactionAwaiting, WalletUninitialized)

  when(TransactionAwaiting) {
    case Event(BetAndWin(id, bet, win), _) => goto(BetAttempting) applying (BetAndWinArrived(sender(), id, bet, win))
  }

  when(BetAttempting) {
    case Event(TxSuccess(id), _) => goto(WinRetrying) applying (TxProcessingSucceed(id))
  }

  when(WinRetrying) {
    case Event(TxSuccess(id), _) => goto(TransactionAwaiting) applying (TxProcessingSucceed(id))
  }

  whenUnhandled {
    case Event("ReadJournal", _) => stay() replying {
      val readJournal = PersistenceQuery(context.system).readJournalFor[LeveldbReadJournal](LeveldbReadJournal.Identifier)
      val source: Source[EventEnvelope, Unit] = readJournal.eventsByPersistenceId(persistenceId, 0, Long.MaxValue)
      implicit val mat = ActorMaterializer()
      log.info("==========================")
      source.runForeach { event => log.info("Event: " + event) }
      log.info("==========================")
      "ok"
    }

    case Event("Reset", _) => goto(TransactionAwaiting) applying (WalletReset)
  }

  onTransition {
    case TransactionAwaiting -> BetAttempting =>
      nextStateData match {
        case WalletProcessing(client, bets, wins) =>
          wallet ! bets.head
      }

    case BetAttempting -> WinRetrying =>
      nextStateData match {
        case WalletProcessing(client, bets, wins) => wallet ! wins.head
      }

    case WinRetrying -> TransactionAwaiting =>
      stateData match {
        case WalletProcessing(client, _, _) => client ! BetAndWinDone
      }
  }

  initialize()

  override def onRecoveryCompleted(): Unit = {
    log.info("Recovery is completed. Current state is {}", stateName)
  }

  override def applyEvent(domainEvent: WalletEvent, currentData: WalletClientData): WalletClientData = {
    domainEvent match {
      case BetAndWinArrived(client, id, bet, win) => {
        val betTx = Tx("BET_" + id, bet)
        val winTx = Tx("WIN_" + id, win)
        WalletProcessing(client, List(betTx), List(winTx))
      }

      case TxProcessingSucceed(id) => currentData match {
        case WalletProcessing(client, bets, wins) => {
          if (!bets.isEmpty && bets.head.id == id)
            WalletProcessing(client, bets.tail, wins)
          else
            WalletUninitialized
        }
      }

      case WalletReset => WalletUninitialized
    }
  }
}
