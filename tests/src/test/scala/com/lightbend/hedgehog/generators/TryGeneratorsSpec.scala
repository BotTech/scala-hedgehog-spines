package com.lightbend.hedgehog.generators

import com.lightbend.hedgehog.generators.IntGenerators._
import com.lightbend.hedgehog.generators.TryGenerators._
import com.lightbend.hedgehog.scalamock.{GeneratorSpec, TestRunnerMock}
import com.lightbend.hedgehog.testkit.Probabilities
import hedgehog._
import hedgehog.runner._

import scala.util.Try

object TryGeneratorsSpec extends TestRunnerMock with GeneratorSpec with Probabilities {

  override def tests: List[Test] =
    test("genSuccess", genSuccess(genInt)).tests ++
      test("genFailure", genFailure).tests ++
      test("genTry", genTry(genInt))
        .addGenProbabilities(_ + " generates success and failure evenly", propGenTryProbabilities)
        .tests

  private def propGenTryProbabilities[A](gen: Gen[Try[A]]): Property =
    gen.forAll
      .cover(OneToOne, "is success", _.isSuccess)
      .cover(OneToOne, "is failure", _.isFailure)
      .map(_ => Result.success)
}
