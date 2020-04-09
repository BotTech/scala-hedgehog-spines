package com.lightbend.hedgehog.generators

import hedgehog.Gen
import hedgehog.Range

object IntGenerators {

  def genNegativeInt: Gen[Int] = Gen.int(Range.linear(-1, Int.MinValue))

  def genNonPositiveInt: Gen[Int] = Gen.int(Range.linear(0, Int.MinValue))

  def genNonNegativeInt: Gen[Int] = Gen.int(Range.linear(0, Int.MaxValue))

  def genPositiveInt: Gen[Int] = Gen.int(Range.linear(1, Int.MaxValue))

  def genZero: Gen[Int] = Gen.constant(0)

  def genInt: Gen[Int] = Gen.int(Range.linearFrom(0, Int.MinValue, Int.MaxValue))
}
