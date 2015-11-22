package org.scalasino


import akka.actor.{Props, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalasino.model.{Spin, SpinOutcome}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class SlotTest extends TestKit(ActorSystem("SlotTest"))
with Matchers
with WordSpecLike
with BeforeAndAfterAll
with ImplicitSender {

  val r = new RandomNumberGenerator(Vector())
  val slot = system.actorOf(Props(new Slot("testSlot", r)))

  override def afterAll: Unit = system.terminate()

  "Slot Actor" should {
    "win 1x" in {
      r.setMock(Vector(2,2,2))
      slot ! Spin(2.00)
      expectMsg(SpinOutcome(2.00, 2.00, 3, 3, 3, false))
      /*      within(10 millis) {
              slot ! GetBalance
              expectMsg(money(10, "EUR"))
            }*/
    }

    "get pick and click qualified" in {
      r.setMock(Vector(6,6,6))
      slot ! Spin(2.00)
      expectMsg(SpinOutcome(2.00, 4.00, 7, 7, 7, true))
    }

  }
}
