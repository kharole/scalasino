package org.scalasino

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

  case class SpinOutcome(bet: BigDecimal, win: BigDecimal, r1: Integer, r2: Integer, r3: Integer, qualifiedForPickAndClick: Boolean) extends Data

  case class PickAndClickOutcome(bet: BigDecimal, win: BigDecimal) extends Data

  // received events
  final case class Spin(bet: BigDecimal)

  final case class Pick(choice: List[Integer])


  final case class WalletSuccess(id: Integer)

  final case object WalletFailure

  final case class BetAndWin(id: Integer, bet: BigDecimal, win: BigDecimal)

}
