package com.lightbend.hedgehog.generators.core

import com.lightbend.hedgehog.generators.core.SeedGenerators._
import com.lightbend.hedgehog.scalamock.{GeneratorSpec, TestRunnerMock}
import com.lightbend.hedgehog.testkit.Probabilities
import hedgehog._
import hedgehog.core.Seed
import hedgehog.runner.Test

object SeedGeneratorsSpec extends TestRunnerMock with GeneratorSpec with Probabilities {

  override def tests: List[Test] =
    test("genSeed", genSeed)
      .addGenProbabilitiesWithConfig(_ + " generates a fair seed", propProbabilities, _.withTests(100000))
      .tests

  private def propProbabilities(genSeed: Gen[Seed]): Property = {
    val gen = for {
      from <- Gen.long(Range.constant(Long.MinValue, Long.MaxValue - BirthdayDays + 1))
      seed <- genSeed
    } yield {
      // Use the birthday paradox to get just over 50% probability of a match if the seed is random.
      val (_, _, hasMatch) = (1 to BirthdayPeople.toInt).foldLeft((seed, Set.empty[Long], false)) {
        case (acc @ (_, _, true), _) => acc
        case ((seed, values, false), _) =>
          val (nextSeed, value) = seed.chooseLong(from, from + BirthdayDays - 1)
          val hasMatch          = values.contains(value)
          (nextSeed, values + value, hasMatch)
      }
      hasMatch
    }
    gen.forAll
      .cover(BirthdayMatch, "match", identity)
      .cover(BirthdayNoMatch, "no match", hasMatch => !hasMatch)
      .map(_ => Result.success)
  }
}
