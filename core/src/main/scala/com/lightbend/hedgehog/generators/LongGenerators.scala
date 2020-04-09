package com.lightbend.hedgehog.generators

import hedgehog.{Gen, Range}

object LongGenerators {

  def genNegativeLong: Gen[Long] = Gen.long(Range.linear(-1, Long.MinValue))

  def genNonPositiveLong: Gen[Long] = Gen.long(Range.linear(0, Long.MinValue))

  def genNonNegativeLong: Gen[Long] = Gen.long(Range.linear(0, Long.MaxValue))

  def genPositiveLong: Gen[Long] = Gen.long(Range.linear(1, Long.MaxValue))

  def genZero: Gen[Long] = Gen.constant(0)

  def genLong: Gen[Long] = Gen.long(Range.linearFrom(0, Long.MinValue, Long.MaxValue))
}
