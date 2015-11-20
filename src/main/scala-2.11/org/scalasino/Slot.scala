package org.scalasino

import akka.actor._
import org.scalasino.model._

class Slot(name: String) extends FSM[SlotState, Data] with ActorLogging {

  val r = scala.util.Random

  val walletClient = context.system.actorOf(Props(new WalletClient))

  var s: ActorRef = null

  startWith(SpinAwaiting, Uninitialized)

  when(SpinAwaiting) {
    case Event(Spin(bet), Uninitialized) => {
      val r1 = r.nextInt(7) + 1
      val r2 = r.nextInt(7) + 1
      val r3 = r.nextInt(7) + 1
      val qualifiedForPickAndClick: Boolean = r1 == 7 && r2 == 7 && r3 == 7
      s = sender();
      val win: BigDecimal = if (r1 == r2 && r2 == r3 && r3 == r1) bet * BigDecimal(r1 / 3) else 0
      goto(Processing) using SpinOutcome(bet, win, r1, r2, r3, qualifiedForPickAndClick)
    }
  }


  when(Processing) {
    case Event(WalletSuccess(id), SpinOutcome(_, _, _, _, _, false)) => goto(SpinAwaiting) using Uninitialized
    case Event(WalletSuccess(id), data) => goto(PickAndClickAwaiting) using data
    case Event(WalletFailure, data) => goto(PickAndClickAwaiting) using data
  }


  onTransition {
    case SpinAwaiting -> Processing =>
      nextStateData match {
        case SpinOutcome(bet, win, _, _, _, _) =>
          walletClient ! BetAndWin(1, bet, win)
      }

    case Processing -> _ =>
      s ! stateData
  }

}
