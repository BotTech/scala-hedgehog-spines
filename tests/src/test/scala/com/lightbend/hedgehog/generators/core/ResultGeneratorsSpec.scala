package com.lightbend.hedgehog.generators.core

import com.lightbend.hedgehog.generators.core.ResultGenerators._
import com.lightbend.hedgehog.implicits.ArbitraryImplicits._
import com.lightbend.hedgehog.scalamock.{GeneratorSpec, TestRunnerMock}
import com.lightbend.hedgehog.testkit.Probabilities
import hedgehog.core.Result.Failure
import hedgehog.runner.Test
import hedgehog.{Gen, Property, Result}
import org.scalactic.TripleEquals._

object ResultGeneratorsSpec extends TestRunnerMock with GeneratorSpec with Probabilities {

  override def tests: List[Test] =
    test("genSuccess", genSuccessfulResult).addConstantTest(_ + " is success", Result.success).tests ++
      test("genFailure", genFailedResult).addValueTest(_ + " is failure", _.isA[Failure]).tests ++
      test("genResult", genResult)
        .addGenExample(_ + " generates success as minimum", testGenMinFrom(_, Result.success))
        .addGenExample(_ + " generates failure as maximum", testGenMaxResultTo(_)(_.isA[Failure]))
        .addGenProbabilities(_ + " generates results with probabilities", propProbabilities)
        .tests

  private def propProbabilities(gen: Gen[Result]): Property =
    gen.forAll
      .cover(OneToOne, "success", _ === Result.success)
      .cover(OneToOne, "failure", failureCover)
      .map(_ => Result.success)

  private def failureCover(result: Result): Boolean = result match {
    case Result.Success => false
    case _: Failure     => true
  }
}
