package org.scalasino

import akka.actor.Actor
import org.scalasino.model.{WalletSuccess, BetAndWin}

class WalletClient extends Actor {

  override def receive: Receive = {
    case BetAndWin(id, bet, win) =>
      sender() ! WalletSuccess(id)
  }

}
