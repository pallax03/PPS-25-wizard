package it.unibo.pps.wizard.util

import scala.util.Random

object Id:
  def apply(idBase: Int = 36): String =
    BigInt.long2bigInt(Random.nextLong(Long.MaxValue)).toString(idBase)
