package com.lightbend.hedgehog.generators

import com.lightbend.hedgehog.Sizes
import com.lightbend.hedgehog.generators.IntGenerators.genInt
import com.lightbend.hedgehog.generators.OptionGenerators._
import com.lightbend.hedgehog.scalamock.{GeneratorSpec, TestRunnerMock}
import com.lightbend.hedgehog.testkit.Probabilities
import hedgehog.runner.Test
import hedgehog.{Gen, Property, Result}

object OptionGeneratorSpec extends TestRunnerMock with GeneratorSpec with Probabilities {

  override def tests: List[Test] =
    test("genSome", genSome(genInt)).tests ++
      test("genNone", genNone).tests ++
      test("genOption", genOption(genInt))
        .addGenProbabilities(_ + " generates some and none for min size", propGenOptionMinSizeProbabilities)
        .addGenProbabilities(_ + " generates some and none for max size", propGenOptionMaxSizeProbabilities)
        .tests

  private def propGenOptionMinSizeProbabilities[A](gen: Gen[Option[A]]): Property =
    gen
      .resize(Sizes.MinSize)
      .forAll
      .cover(OneToOne, "is defined", _.isDefined)
      .cover(OneToOne, "is undefined", _.isEmpty)
      .map(_ => Result.success)

  private def propGenOptionMaxSizeProbabilities[A](gen: Gen[Option[A]]): Property =
    gen
      .resize(Sizes.MaxSize)
      .forAll
      .cover(OneHundredAndOneToTwo, "is defined", _.isDefined)
      .cover(TwoToOneHundredAndOne, "is undefined", _.isEmpty)
      .map(_ => Result.success)
}
