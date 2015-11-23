package org.scalasino

import scala.util.Random

trait RandomNumberGenerator {
  val r = Random

  def nextInt(n: Int): Int = r.nextInt(n)
}
