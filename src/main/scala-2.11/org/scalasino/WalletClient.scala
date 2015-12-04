package org.scalasino

import akka.persistence.fsm.PersistentFSM
import akka.persistence.query.journal.leveldb.scaladsl.LeveldbReadJournal
import akka.persistence.query.{EventEnvelope, PersistenceQuery}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import org.scalasino.model._

import scala.reflect.ClassTag

class WalletClient(playerId: String) extends PersistentFSM[WalletClientState, WalletClientData, WalletEvent] {

  override def persistenceId: String = playerId

  override implicit def domainEventClassTag = ClassTag(classOf[WalletEvent])

  startWith(TransactionAwaiting, WalletUninitialized)



  when(TransactionAwaiting) {
    case Event(BetAndWin(id, bet, win), _) => goto(BetAttempting) applying (BetAndWinArrived(id, bet, win))
  }

  when(BetAttempting) {
    case Event(_, _) => goto(TransactionAwaiting) applying (BetAndWinDone)
  }

  whenUnhandled {
    case Event(_, _) => goto(TransactionAwaiting) applying (BetAndWinDone)
  }

  initialize()

  override def onRecoveryCompleted(): Unit = {
    log.info( "Recovery is completed. Current state is {}", stateName )
  }

  override def applyEvent(domainEvent: WalletEvent, currentData: WalletClientData): WalletClientData = {
    domainEvent match {
      case tx@BetAndWinArrived(id, bet, win) => {
        val readJournal = PersistenceQuery(context.system).readJournalFor[LeveldbReadJournal](LeveldbReadJournal.Identifier)
        val source: Source[EventEnvelope, Unit] = readJournal.eventsByPersistenceId("player1", 0, Long.MaxValue)
        implicit val mat = ActorMaterializer()
        source.runForeach { event => println("Event: " + event) }
        currentData
      }
      case BetAndWinDone => WalletUninitialized
    }
  }


}
