package org.scalasino

import akka.actor.{Props, ActorSystem}
import akka.persistence.query.journal.leveldb.scaladsl.LeveldbReadJournal
import akka.persistence.query.{EventEnvelope, PersistenceQuery}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import com.typesafe.config.ConfigFactory
import org.scalamock.scalatest.MockFactory
import org.scalasino.model._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class WalletClientTest extends TestKit(ActorSystem("WalletTest", ConfigFactory.defaultApplication()))
with Matchers
with WordSpecLike
with BeforeAndAfterAll
with ImplicitSender
with MockFactory {

  val walletClient = system.actorOf(Props(classOf[WalletClient], "player1"), "testClient")

  override def afterAll: Unit = system.terminate()

  "Wallet Client Actor" should {
    "betAndWin" in {
      //w ! BetAndWin(1, 1.00, 2.00)
      walletClient ! "reset"
      walletClient ! BetAndWin(2, 7.00, 13.00)
      expectMsg(BetAndWinDone)
    }

  }
}
