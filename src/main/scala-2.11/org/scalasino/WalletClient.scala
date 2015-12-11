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

  val maxRetryCount = 3;

  override implicit def domainEventClassTag = ClassTag(classOf[WalletEvent])

  startWith(TransactionAwaiting, WalletUninitialized)

  when(TransactionAwaiting) {
    case Event(BetAndWin(id, bet, win), _) =>
      goto(BetAttempting) applying (BetAndWinArrived(sender(), id, bet, win))
  }

  when(BetAttempting) {
    case Event(TxSuccess(id), _) => goto(WinRetrying) applying (BetSucceeded(id))
    case Event(TxFailure(id), WalletProcessing(client, bets, wins, retryCount)) =>
      if (retryCount > 0)
        goto(BetAttempting) applying (BetFailed(id))
      else
        goto(BetRollingBack) applying (BetFailed(id))
  }

  when(WinRetrying) {
    case Event(TxSuccess(id), _) => goto(TransactionAwaiting) applying (BetSucceeded(id))
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
    case _ -> BetAttempting =>
      nextStateData match {
        case WalletProcessing(client, bets, wins, retryCount) =>
          wallet ! bets.head
      }

    case BetAttempting -> WinRetrying =>
      nextStateData match {
        case WalletProcessing(client, bets, wins, retryCount) => wallet ! wins.head
      }

    case WinRetrying -> TransactionAwaiting =>
      stateData match {
        case WalletProcessing(client, _, _, _) => client ! BetAndWinDone
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
        WalletProcessing(client, List(betTx), List(winTx), maxRetryCount)
      }

      case BetSucceeded(id) => currentData match {
        case WalletProcessing(client, bh :: bt, wins, _) => WalletProcessing(client, bt, wins, maxRetryCount)
        case WalletProcessing(client, Nil, wins, _) => WalletProcessing(client, Nil, wins, maxRetryCount)
      }

      case WinSucceeded(id) => currentData match {
        case WalletProcessing(client, bets, wh :: wt, _) => WalletProcessing(client, bets, wt, maxRetryCount)
        case WalletProcessing(client, bets, Nil, _) => WalletUninitialized
      }

      case BetFailed(id) => currentData match {
        case WalletProcessing(client, bets, wins, retryCount) =>
          WalletProcessing(client, bets, wins, retryCount - 1)
      }

      case WalletReset => WalletUninitialized
    }
  }
}
