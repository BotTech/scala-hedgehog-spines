package com.lightbend.hedgehog.generators.core

import com.lightbend.hedgehog.generators.LongGenerators._
import hedgehog.Gen
import hedgehog.core.Seed

// Don't use any of our other custom generators in here since we use this to test them and would
// end up with circular reasoning which would be prone to false positives.
// These ought to go into their own project to prevent that but that is too much overhead.
object SeedGenerators {

  def genSeed: Gen[Seed] = genLong.map(Seed.fromLong)
}
