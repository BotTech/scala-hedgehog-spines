package com.lightbend.hedgehog.generators.cats

import cats.effect.IO
import com.lightbend.hedgehog.generators.ExceptionGenerators._
import hedgehog.Gen

object IOGenerators {

  def genSuccessfulIO[A](gen: Gen[A]): Gen[IO[A]] =
    gen.map(IO.pure)

  def genFailedIO[A]: Gen[IO[A]] =
    genException.map(IO.raiseError)

  def genIO[A](gen: Gen[A]): Gen[IO[A]] =
    Gen.choice1(genSuccessfulIO(gen), genFailedIO)
}
