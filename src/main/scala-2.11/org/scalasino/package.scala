package org.scalasino

import akka.actor.ActorRef
import akka.persistence.fsm.PersistentFSM.FSMState

package object model {

  // === Slot states ============

  sealed trait SlotState

  case object SpinAwaiting extends SlotState

  case object PickAndClickAwaiting extends SlotState

  case object Processing extends SlotState

  case object Unavailable extends SlotState

  //=============================

  sealed trait Data

  case object Uninitialized extends Data

  case class SpinOutcome(client: ActorRef, bet: BigDecimal, win: BigDecimal, r1: Integer, r2: Integer, r3: Integer, qualifiedForPickAndClick: Boolean) extends Data

  case class PickAndClickOutcome(client: ActorRef, bet: BigDecimal, win: BigDecimal) extends Data

  // received events
  final case class Spin(bet: BigDecimal)

  final case class PickAndClick(choice: List[Integer])

  final case class WalletSuccess(id: String)

  final case object WalletFailure

  final case class BetAndWin(id: String, bet: BigDecimal, win: BigDecimal)


  sealed trait WalletClientState extends FSMState

  case object TransactionAwaiting extends WalletClientState {
    override def identifier: String = "TransactionAwaiting"
  }

  case object BetAttempting extends WalletClientState {
    override def identifier: String = "BetAttempting"
  }

  case object BetRollingBack extends WalletClientState {
    override def identifier: String = "BetRollingBack"
  }

  case object WinRetrying extends WalletClientState {
    override def identifier: String = "WinRetrying"
  }

  sealed trait WalletClientData

  case object WalletUninitialized extends WalletClientData

  case class WalletProcessing(client: ActorRef, bets: List[Tx], wins: List[Tx], retryCount: Int) extends WalletClientData

  sealed trait WalletEvent

  case class BetAndWinDone(id: String) extends WalletEvent

  case object WalletReset extends WalletEvent

  case class BetAndWinArrived(client: ActorRef, id: String, bet: BigDecimal, win: BigDecimal) extends WalletEvent

  case class BetSucceeded(id: String) extends WalletEvent

  case class BetFailed(id: String) extends WalletEvent

  case class WinSucceeded(id: String) extends WalletEvent

  case class WinFailed(id: String) extends WalletEvent

  case class Tx(id: String, amount: BigDecimal)

  case class TxSuccess(id: String)

  case class TxReject(id: String)

  case class TxFailure(id: String)

}
