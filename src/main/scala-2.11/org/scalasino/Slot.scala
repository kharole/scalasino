package org.scalasino

import akka.actor._
import org.scalasino.model._

class Slot(name: String, r: RandomNumberGenerator, walletClient: ActorRef) extends FSM[SlotState, Data] with ActorLogging {

  startWith(SpinAwaiting, Uninitialized)

  when(SpinAwaiting) {
    case Event(Spin(bet), Uninitialized) => {
      val r1 = r.nextInt(7) + 1
      val r2 = r.nextInt(7) + 1
      val r3 = r.nextInt(7) + 1
      val qualifiedForPickAndClick: Boolean = r1 == 7 && r2 == 7 && r3 == 7
      val win: BigDecimal = if (r1 == r2 && r2 == r3 && r3 == r1) bet * BigDecimal(r1 / 3) else 0
      goto(Processing) using SpinOutcome(sender(), bet, win, r1, r2, r3, qualifiedForPickAndClick)
    }
  }

  when(Processing) {
    case Event(WalletSuccess(id), SpinOutcome(_, _, _, _, _, _, false)) =>
      goto(SpinAwaiting) using Uninitialized
    case Event(WalletSuccess(id), outcome) =>
      goto(PickAndClickAwaiting) using outcome
  }

  when(PickAndClickAwaiting) {
    case Event(PickAndClick(choice), SpinOutcome(_, bet, _, _, _, _, _)) => {
      goto(Processing) using PickAndClickOutcome(sender(), bet, 0)
    }
  }

  onTransition {
    case _ -> Processing =>
      nextStateData match {
        case SpinOutcome(_, bet, win, _, _, _, _) =>
          walletClient ! BetAndWin(1, bet, win)
      }

    case Processing -> _ =>
      stateData match {
        case SpinOutcome(client, _, _, _, _, _, _) => client ! stateData
        case PickAndClickOutcome(client, _, _) => client ! stateData
      }
  }

}
