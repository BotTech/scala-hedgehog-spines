package com.lightbend.hedgehog.implicits

import java.io.{PrintWriter, StringWriter}

import com.lightbend.hedgehog.implicits.TryImplicits.TrySyntax
import hedgehog.Result

import scala.util.{Failure, Success, Try}

trait TryImplicits {

  implicit def trySyntax[A](tryA: Try[A]): TrySyntax[A] = new TrySyntax(tryA)
}

object TryImplicits {

  implicit class TrySyntax[A](private val tryA: Try[A]) extends AnyVal {

    def successfulResult(f: A => Result): Result = tryA match {
      case failure @ Failure(exception) =>
        Result.failure
          .log(s"${failure.toString} is not a Success")
          .log(stackTrace(exception))
      case Success(value) => f(value)
    }

    def failedResult(f: Throwable => Result): Result = tryA match {
      case success: Success[A] => Result.failure.log(s"${success.toString} is not a Failure")
      case Failure(exception)  => f(exception)
    }
  }

  private[implicits] def stackTrace(t: Throwable): String = {
    val writer = new StringWriter()
    t.printStackTrace(new PrintWriter(writer))
    writer.toString
  }
}
