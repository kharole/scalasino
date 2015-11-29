package org.scalasino

import akka.actor.Actor
import akka.persistence.fsm.PersistentFSM
import org.scalasino.model._

import scala.reflect.ClassTag

class WalletClient(playerId: String) extends PersistentFSM[WalletClientState, WalletClientData, WalletEvent] {

  override def persistenceId: String = playerId

  override implicit def domainEventClassTag = ClassTag(classOf[WalletEvent])

  startWith(TransactionAwaiting, WalletUninitialized)

  when(TransactionAwaiting) {
    case Event(BetAndWin(id, bet, win), _) => goto(BetAttempting) applying (BetAndWinArrived(id, bet, win))
  }

  initialize()

  override def applyEvent(domainEvent: WalletEvent, currentData: WalletClientData): WalletClientData = {
    domainEvent match {
      case tx @ BetAndWinArrived(id, bet, win) => currentData
    }
  }


}
