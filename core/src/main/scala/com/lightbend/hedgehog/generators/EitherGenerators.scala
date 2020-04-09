package com.lightbend.hedgehog.generators

import com.lightbend.hedgehog.implicits.GeneratorImplicits._
import hedgehog.Gen

object EitherGenerators {

  def genLeft[A](gen: Gen[A]): Gen[Left[A, Nothing]] =
    gen.map(Left(_))

  def genRight[B](gen: Gen[B]): Gen[Right[Nothing, B]] =
    gen.map(Right(_))

  def genEither[A, B](genA: Gen[A], genB: Gen[B]): Gen[Either[A, B]] =
    Gen.choice1(genLeft(genA).widen, genRight(genB).widen)
}
