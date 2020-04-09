package com.lightbend.hedgehog.generators

import com.lightbend.hedgehog.generators.EitherGenerators._
import com.lightbend.hedgehog.generators.IntGenerators.genInt
import com.lightbend.hedgehog.scalamock.{GeneratorSpec, TestRunnerMock}
import com.lightbend.hedgehog.testkit.Probabilities
import hedgehog.runner.Test
import hedgehog.{Gen, Property, Result}

object EitherGeneratorsSpec extends TestRunnerMock with GeneratorSpec with Probabilities {

  override def tests: List[Test] =
    test("genLeft", genLeft(genInt)).tests ++
      test("genRight", genRight(genInt)).tests ++
      test("genEither", genEither(genInt, genInt))
        .addGenProbabilities(_ + " generates left and right evenly", propGenEitherProbabilities)
        .tests

  private def propGenEitherProbabilities[A, B](gen: Gen[Either[A, B]]): Property =
    gen.forAll
      .cover(OneToOne, "is left", _.isLeft)
      .cover(OneToOne, "is right", _.isRight)
      .map(_ => Result.success)
}
