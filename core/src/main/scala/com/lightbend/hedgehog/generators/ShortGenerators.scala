package com.lightbend.hedgehog.generators

import hedgehog.{Gen, Range}

object ShortGenerators {

  def genShort: Gen[Short] = Gen.short(Range.linearFrom(0, Short.MinValue, Short.MaxValue))
}
