package com.lightbend.hedgehog.implicits

import com.lightbend.hedgehog.implicits.EitherImplicits.EitherSyntax
import hedgehog.Result

trait EitherImplicits {

  implicit def eitherSyntax[A, B](either: Either[A, B]): EitherSyntax[A, B] = new EitherSyntax(either)
}

object EitherImplicits {

  implicit class EitherSyntax[A, B](private val either: Either[A, B]) extends AnyVal {

    def rightResult(f: B => Result): Result = either match {
      case Left(_)      => Result.failure.log(s"${either.toString} is not Right")
      case Right(value) => f(value)
    }

    def leftResult(f: A => Result): Result = either match {
      case Right(_)    => Result.failure.log(s"${either.toString} is not Left")
      case Left(value) => f(value)
    }
  }
}
