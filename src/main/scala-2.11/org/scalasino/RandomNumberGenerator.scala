package org.scalasino

import scala.util.Random

class RandomNumberGenerator(var mock: Vector[Int]) {
  val r = Random
  var i:Int = -1

  def nextInt(n:Integer):Integer = if(mock.size > 0) { i = i +1; mock(i%mock.size); } else r.nextInt(n)

  def setMock(newMock:Vector[Int]) = {
    mock = newMock
  }
}
