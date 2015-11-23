package org.scalasino


import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestFSMRef, TestKit}
import org.scalasino.model.{PickAndClickAwaiting, Spin, SpinAwaiting, SpinOutcome}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class SlotTest extends TestKit(ActorSystem("SlotTest"))
with Matchers
with WordSpecLike
with BeforeAndAfterAll
with ImplicitSender {

  val r = new RandomNumberGenerator(Vector())

  val slot = TestFSMRef(new Slot("testSlot", r))

  override def afterAll: Unit = system.terminate()

  "Slot Actor" should {
    "win 1x" in {
      r.setMock(Vector(2, 2, 2))
      slot ! Spin(2.00)
      expectMsg(SpinOutcome(self, 2.00, 2.00, 3, 3, 3, false))
      assert(slot.stateName == SpinAwaiting)
    }

    "get pick and click qualified" in {
      r.setMock(Vector(6, 6, 6))
      slot ! Spin(2.00)
      expectMsg(SpinOutcome(self, 2.00, 4.00, 7, 7, 7, true))
      assert(slot.stateName == PickAndClickAwaiting)
    }

  }
}
