package com.lightbend.hedgehog.implicits

import com.lightbend.hedgehog.Results._
import com.lightbend.hedgehog.generators.ExceptionGenerators._
import com.lightbend.hedgehog.generators.FutureGenerators._
import com.lightbend.hedgehog.generators.IntGenerators.genInt
import com.lightbend.hedgehog.generators.core.ResultGenerators._
import com.lightbend.hedgehog.implicits.ArbitraryImplicits._
import com.lightbend.hedgehog.implicits.TryImplicits._
import com.lightbend.hedgehog.runner.TestRunner
import hedgehog._
import hedgehog.core.{Info, Log}
import hedgehog.runner._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success, Try}

object FutureSyntaxSpec extends TestRunner with FutureImplicits {

  override def tests: List[Test] = List(
    property("readyWithin of completed is a success", propReadyWithinOfCompletedIsSuccess),
    property("readyWithin of incomplete is a failure", propReadyWithinOfIncompleteIsAFailure),
    property("readyWithin undefined duration is a failure", propReadyWithinOfUndefinedIsAFailure),
    property("successfulResult of failed is a failure", propSuccessfulResultOfFailedIsAFailure),
    property("successfulResult of success is result of f", propSuccessfulResultOfSuccessIsResultOfFunction),
    property("failedResult of success is a failure", propFailedResultOfSuccessIsAFailure),
    property("failedResult of failed is result of f", propFailedResultOfFailedIsResultOfFunction),
    property("invalid diagnosis property blows up", propInvalidDiagnosisPropertyBlowsUp),
    property("diagnosis of incomplete is a failure", propValidDiagnosisOfIncomplete),
    example("diagnosis of eventually complete fails with details", testValidDiagnosisOfEventuallyComplete)
  )

  private def propReadyWithinOfCompletedIsSuccess: Property =
    genCompletedFuture(genInt).forAll.map(_.readyWithin(0.seconds))

  private def propReadyWithinOfIncompleteIsAFailure: Property =
    genIncompleteFuture[Int].forAll.map { incomplete =>
      val result = incomplete.readyWithin(0.seconds)
      result ==== Result.failure.log("Future was not ready within 0 seconds")
    }

  private def propReadyWithinOfUndefinedIsAFailure: Property =
    genIncompleteFuture[Int].forAll.map { incomplete =>
      val result = incomplete.readyWithin(Duration.Undefined)
      not(result, "Expected failure").and(
        Result.assert(result.logs.headOption.contains[Log]("Error occurred waiting for future to be ready"))
      )
    }

  private def propSuccessfulResultOfFailedIsAFailure: Property =
    genException.forAll.map { exception =>
      val failed = Future.failed(exception)
      val result = failed.successfulResultWithin(0.seconds, (_: Any) => Result.failure)
      result ==== Result.failure
        .log(s"${Failure(exception).toString} is not a Success")
        .log(TryImplicits.stackTrace(exception))
    }

  private def propSuccessfulResultOfSuccessIsResultOfFunction: Property = forAll {
    for {
      success <- genSuccessfulFuture(genInt)
      result  <- genResult
    } yield success.successfulResultWithin(0.seconds, _ => result) ==== result
  }

  private def propFailedResultOfSuccessIsAFailure: Property =
    genInt.forAll.map { x =>
      val successful = Future.successful(x)
      val result     = successful.failedResultWithin(0L.seconds, _ => Result.failure)
      result ==== Result.failure.log(s"${Success(x).toString} is not a Failure")
    }

  private def propFailedResultOfFailedIsResultOfFunction: Property =
    for {
      failure <- genFailedFuture[Int].forAll
      result  <- genResult.forAll
    } yield failure.failedResultWithin(0.0.seconds, _ => result) ==== result

  private def propInvalidDiagnosisPropertyBlowsUp: Property =
    genIncompleteFuture[Int].forAll.map { incomplete =>
      withDiagnosisProperty("x") {
        Try(incomplete.readyWithin(0.seconds)).failedResult { t =>
          t.isA[NumberFormatException].and {
            t.getMessage ==== "Invalid value for property 'hedgehog.future.diagnosisDuration': format error x"
          }
        }
      }
    }

  private def propValidDiagnosisOfIncomplete: Property =
    genIncompleteFuture[Int].forAll.map { incomplete =>
      withDiagnosisProperty("1ns") {
        incomplete.readyWithin(0.seconds) ==== Result.failure
          .log("Future was not ready within 0 seconds")
          .log("Future was still not ready after waiting an additional 1 nanosecond")
      }
    }

  private def testValidDiagnosisOfEventuallyComplete: Result =
    // TODO: Find a way to mock Future
    //  See https://github.com/paulbutcher/ScalaMock/issues/296
    //val future = stub[MockableFuture[Int]]
    //(future.ready(_: Duration)(_: CanAwait)).when(*, *).throws(new TimeoutException("timeout")).once()
    //(future.ready(_: Duration)(_: CanAwait)).when(*, *).returns(future).once()
    withDiagnosisProperty("200ms") {
      val promise = Promise[Int]
      val _ = Future {
        Thread.sleep(100)
        promise.success(0)
      }
      val result = promise.future.readyWithin(0.millis)
      not(result, "Expected failure").and(
        result.logs match {
          case first :: Info(second) :: Info(third) :: Nil =>
            Result.all(
              List(
                first ==== "Future was not ready within 0 milliseconds",
                Result.assert(second.startsWith("Future eventually completed after waiting an additional")),
                Result.assert(third.startsWith("Consider increasing the original wait time to at least"))
              )
            )
          case _ => Result.failure.log("Unexpected log messages.")
        }
      )
    }

  private def withDiagnosisProperty[A](value: String)(f: => Result): Result = {
    val property      = "hedgehog.future.diagnosisDuration"
    val originalValue = Option(System.getProperty(property))
    try {
      val _ = System.setProperty(property, value)
      f
    } finally {
      originalValue match {
        case Some(value) =>
          val _ = System.setProperty(property, value)
        case None =>
          val _ = System.clearProperty(property)
      }
    }
  }
}
