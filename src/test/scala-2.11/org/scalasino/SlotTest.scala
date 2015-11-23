package org.scalasino


import akka.actor.{Props, ActorSystem}
import akka.testkit.{ImplicitSender, TestFSMRef, TestKit}
import org.scalasino.model.{PickAndClickAwaiting, Spin, SpinAwaiting, SpinOutcome}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import org.scalamock.scalatest.MockFactory

class SlotTest extends TestKit(ActorSystem("SlotTest"))
with Matchers
with WordSpecLike
with BeforeAndAfterAll
with ImplicitSender
with MockFactory {

  val r = stub[RandomNumberGenerator]

  val walletClient = system.actorOf(Props(new WalletClient()))
  val slot = TestFSMRef(new Slot("testSlot", r, walletClient))

  override def afterAll: Unit = system.terminate()

  "Slot Actor" should {
    "win 1x" in {
      (r.nextInt _) when 7 returns 2
      slot ! Spin(2.00)
      expectMsg(SpinOutcome(self, 2.00, 2.00, 3, 3, 3, false))
      assert(slot.stateName == SpinAwaiting)
    }

    "get pick and click qualified" in {
      (r.nextInt _) when 7 returns 6
      slot ! Spin(2.00)
      expectMsg(SpinOutcome(self, 2.00, 4.00, 7, 7, 7, true))
      assert(slot.stateName == PickAndClickAwaiting)
    }

  }
}
