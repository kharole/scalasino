package org.scalasino


import akka.actor.{Props, ActorSystem}
import akka.testkit.{TestProbe, ImplicitSender, TestFSMRef, TestKit}
import org.scalasino.model._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import org.scalamock.scalatest.MockFactory
import scala.concurrent.duration._

class SlotTest extends TestKit(ActorSystem("SlotTest"))
with Matchers
with WordSpecLike
with BeforeAndAfterAll
with ImplicitSender
with MockFactory {

  val r = stub[RandomNumberGenerator]

  val walletClient = TestProbe()
  val slot = TestFSMRef(new Slot("testSlot", r, walletClient.ref))

  override def afterAll: Unit = system.terminate()

  "Slot Actor" should {
    "win 1x" in {
      (r.nextInt _) when 7 returns 2
      slot ! Spin(2.00)
      walletClient.expectMsg(BetAndWin(1, 2.00, 2.00))
      walletClient.reply(WalletSuccess(1))
      expectMsg(SpinOutcome(self, 2.00, 2.00, 3, 3, 3, false))
      assert(slot.stateName == SpinAwaiting)
    }

    "get pick and click qualified" in {
      (r.nextInt _) when 7 returns 6
      slot ! Spin(2.00)
      walletClient.expectMsg(BetAndWin(1, 2.00, 4.00))
      walletClient.reply(WalletSuccess(1))
      expectMsg(SpinOutcome(self, 2.00, 4.00, 7, 7, 7, true))
      assert(slot.stateName == PickAndClickAwaiting)
    }

    "long processing" in {
      (r.nextInt _) when 7 returns 2
      slot ! Spin(2.00)
      walletClient.expectMsg(BetAndWin(1, 2.00, 2.00))
      Thread.sleep(500)
      walletClient.reply(WalletSuccess(1))
      assert(slot.stateName == Unavailable)
    }
  }
}
