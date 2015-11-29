package org.scalasino

import akka.actor.{Props, ActorSystem}
import akka.testkit.{ImplicitSender, TestFSMRef, TestKit, TestProbe}
import com.typesafe.config.ConfigFactory
import org.scalamock.scalatest.MockFactory
import org.scalasino.model._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class walletClientTest extends TestKit(ActorSystem("SlotTest", ConfigFactory.defaultApplication()))
with Matchers
with WordSpecLike
with BeforeAndAfterAll
with ImplicitSender
with MockFactory {

  val walletClient = system.actorOf(Props(new WalletClient("player1")), "testClient")

  override def afterAll: Unit = system.terminate()

  "Wallet Client Actor" should {
    "betAndWin" in {
      walletClient ! BetAndWin(1, 1.00, 2.00)
    }

  }
}
