package com.lightbend.hedgehog.implicits

import com.lightbend.hedgehog.generators.IntGenerators.genInt
import com.lightbend.hedgehog.generators.core.ResultGenerators._
import com.lightbend.hedgehog.generators.TryGenerators._
import com.lightbend.hedgehog.runner.TestRunner
import hedgehog._
import hedgehog.runner._

object TrySyntaxSpec extends TestRunner with TryImplicits {

  override def tests: List[Test] = List(
    property("successfulResult of failure is a failure", propSuccessfulResultOfFailureIsAFailure),
    property("successfulResult of success is result of f", propSuccessfulResultOfSuccessIsResultOfFunction),
    property("failedResult of success is a failure", propFailedResultOfSuccessIsAFailure),
    property("failedResult of failure is result of f", propFailedResultOfFailureIsResultOfFunction)
  )

  private def propSuccessfulResultOfFailureIsAFailure: Property =
    for {
      failure <- genFailure.forAll
    } yield {
      val result = failure.successfulResult((_: Any) => Result.failure)
      result ==== Result.failure
        .log(s"${failure.toString} is not a Success")
        .log(TryImplicits.stackTrace(failure.exception))
    }

  private def propSuccessfulResultOfSuccessIsResultOfFunction: Property =
    for {
      success <- genSuccess(genInt).forAll
      result  <- genResult.forAll
    } yield success.successfulResult(_ => result) ==== result

  private def propFailedResultOfSuccessIsAFailure: Property =
    for {
      success <- genSuccess(genInt).forAll
    } yield {
      val result = success.failedResult(_ => Result.failure)
      result ==== Result.failure.log(s"${success.toString} is not a Failure")
    }

  private def propFailedResultOfFailureIsResultOfFunction: Property =
    for {
      failure <- genFailure.forAll
      result  <- genResult.forAll
    } yield failure.failedResult(_ => result) ==== result
}
