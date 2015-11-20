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

  val slot = system.actorOf(Props(new Slot("testSlot")))

  override def afterAll: Unit = system.terminate()

  "Slot Actor" should {
    "accept spin" in {
      slot ! Spin(2.00)
      expectMsg(SpinOutcome(2.00, 2.00, 1, 1, 1, false))
      /*      within(10 millis) {
              slot ! GetBalance
              expectMsg(money(10, "EUR"))
            }*/
    }
  }
}
