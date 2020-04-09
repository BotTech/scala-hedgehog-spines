package com.lightbend.hedgehog.generators

import com.lightbend.hedgehog.generators.FutureGenerators._
import com.lightbend.hedgehog.generators.IntGenerators._
import com.lightbend.hedgehog.implicits.OptionImplicits._
import com.lightbend.hedgehog.scalamock.{GeneratorSpec, TestRunnerMock}
import com.lightbend.hedgehog.testkit.Probabilities
import hedgehog._
import hedgehog.runner._

import scala.concurrent.Future

object FutureGeneratorsSpec extends TestRunnerMock with GeneratorSpec with Probabilities {

  override def tests: List[Test] =
    test("genSuccessfulFuture", genSuccessfulFuture(genInt))
      .addGenProp(_ + " generates all success", propAllSuccess)
      .tests ++
      test("genFailedFuture", genFailedFuture)
        .addGenProp(_ + " generates all failures", propAllFailures)
        .tests ++
      test("genCompletedFuture", genCompletedFuture(genInt))
        .addGenProp(_ + " generates all completed", propAllCompleted)
        .addGenProbabilities(_ + " generates success and failed evenly", propGenCompletedProbabilities)
        .tests ++
      test("genIncompleteFuture", genIncompleteFuture)
        .addGenProp(_ + " generates all incomplete", propAllIncomplete)
        .tests ++
      test("genFuture", genFuture(genInt))
        .addGenProbabilities(_ + " generates incomplete and complete evenly", propGenFutureProbabilities)
        .tests

  private def propAllSuccess[A](gen: Gen[Future[A]]): Property =
    gen.forAll.map(_.value.isSome(_.isSuccess ==== true))

  private def propAllFailures[A](gen: Gen[Future[A]]): Property =
    gen.forAll.map(_.value.isSome(_.isFailure ==== true))

  private def propAllCompleted[A](gen: Gen[Future[A]]): Property =
    gen.forAll.map(_.isCompleted ==== true)

  private def propGenCompletedProbabilities[A](gen: Gen[Future[A]]): Property =
    gen.forAll
      .cover(OneToOne, "is success", _.value.exists(_.isSuccess))
      .cover(OneToOne, "is failed", _.value.exists(_.isFailure))
      .map(_ => Result.success)

  private def propAllIncomplete[A](gen: Gen[Future[A]]): Property =
    gen.forAll.map(_.isCompleted ==== false)

  private def propGenFutureProbabilities[A](gen: Gen[Future[A]]): Property =
    gen.forAll
      .cover(OneToOne, "is complete", _.isCompleted)
      .cover(OneToOne, "is incomplete", !_.isCompleted)
      .map(_ => Result.success)
}
