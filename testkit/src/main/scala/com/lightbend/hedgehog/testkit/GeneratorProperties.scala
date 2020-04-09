package com.lightbend.hedgehog.testkit

import com.lightbend.hedgehog.generators.core.SeedGenerators.genSeed
import hedgehog.core._
import hedgehog.{Gen, Property, Result}

object GeneratorProperties {

  def propNoDiscards(gen: Gen[_]): Property =
    for {
      seed <- genSeed.forAll
    } yield {
      val config = PropertyConfig.default
      val report = Property.check(config, gen.forAll.map(_ => Result.success), seed)
      report ==== Report(config.testLimit, DiscardCount(0), Coverage.empty, OK)
    }

  def propAllDiscards(gen: Gen[_]): Property =
    for {
      seed <- genSeed.forAll
    } yield {
      val config = PropertyConfig.default
      val report = Property.check(config, gen.forAll.map(_ => Result.success), seed)
      report ==== Report(0, DiscardCount(config.testLimit.value), Coverage.empty, GaveUp)
    }
}
