package com.lightbend.hedgehog.implicits

import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

import com.lightbend.hedgehog.implicits.FutureImplicits.FutureSyntax
import com.lightbend.hedgehog.implicits.OptionImplicits._
import com.lightbend.hedgehog.implicits.TryImplicits._
import hedgehog.Result

import scala.concurrent.duration._
import scala.concurrent.{Await, Future, TimeoutException}
import scala.util.{Failure, Success, Try}

trait FutureImplicits {

  implicit def durationInt(n: Int): DurationConversions = new DurationInt(n)

  implicit def durationLong(n: Long): DurationConversions = new DurationLong(n)

  implicit def durationDouble(n: Double): DurationConversions = new DurationDouble(n)

  implicit def futureSyntax[A](future: Future[A]): FutureSyntax[A] = new FutureSyntax(future)
}

object FutureImplicits {

  private val DiagnosisProperty = "hedgehog.future.diagnosisDuration"

  implicit class FutureSyntax[A](private val future: Future[A]) extends AnyVal {

    def readyWithin(atMost: Duration): Result = {
      val start = Instant.now
      def wait(atMost: Duration, timeout: => Result, failure: Throwable => Result, success: => Result): Result =
        Try(Await.ready(future, atMost)) match {
          case Failure(_: TimeoutException) => timeout
          case Failure(exception)           => failure(exception)
          case _: Success[_]                => success
        }
      def timeout: Result = {
        val result         = Result.failure.log(s"Future was not ready within ${atMost.toString}")
        val diagnosisStart = Instant.now
        def eventuallyReady = {
          val end               = Instant.now
          val diagnosisDuration = durationBetween(diagnosisStart, end)
          val duration          = durationBetween(start, end)
          result
            .log(s"Future eventually completed after waiting an additional ${diagnosisDuration.toString}")
            .log(s"Consider increasing the original wait time to at least ${duration.toString}")
        }
        diagnosisDurationProperty match {
          case Some(duration) =>
            wait(
              duration,
              result.log(s"Future was still not ready after waiting an additional ${duration.toString}"),
              logFailure(result, _),
              eventuallyReady
            )
          case None => result
        }
      }
      wait(atMost, timeout, logFailure(Result.failure, _), Result.success)
    }

    def successfulResultWithin(atMost: Duration, f: A => Result): Result =
      readyWithin(atMost).and(future.value.isSome(_.successfulResult(f)))

    def failedResultWithin(atMost: Duration, f: Throwable => Result): Result =
      readyWithin(atMost).and(future.value.isSome(_.failedResult(f)))
  }

  @SuppressWarnings(Array("org.wartremover.warts.TryPartial", "TryGet"))
  private def diagnosisDurationProperty: Option[Duration] =
    Try {
      Option(System.getProperty(DiagnosisProperty)).map(Duration(_))
    }.recoverWith {
      case e: NumberFormatException =>
        Failure(new NumberFormatException(s"Invalid value for property '$DiagnosisProperty': ${e.getMessage}"))
    }.get

  private def logFailure(result: Result, exception: Throwable): Result =
    result
      .log("Error occurred waiting for future to be ready")
      .log(TryImplicits.stackTrace(exception))

  private def durationBetween(start: Instant, end: Instant) =
    Duration(start.until(end, ChronoUnit.MILLIS), TimeUnit.MILLISECONDS)
}
