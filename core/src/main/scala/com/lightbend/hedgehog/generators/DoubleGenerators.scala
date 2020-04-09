package com.lightbend.hedgehog.generators

import hedgehog.{Gen, Range}

object DoubleGenerators {

  def genDouble: Gen[Double] = Gen.double(Range.linearFracFrom(0, Double.MinValue, Double.MaxValue))
}
