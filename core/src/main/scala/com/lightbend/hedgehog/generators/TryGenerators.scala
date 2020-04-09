package com.lightbend.hedgehog.generators

import com.lightbend.hedgehog.generators.ExceptionGenerators._
import com.lightbend.hedgehog.implicits.GeneratorImplicits._
import hedgehog.Gen

import scala.util.{Failure, Success, Try}

object TryGenerators {

  def genSuccess[A](gen: Gen[A]): Gen[Success[A]] =
    gen.map(Success(_))

  def genFailure: Gen[Failure[Nothing]] =
    genException.map(Failure(_))

  def genTry[A](gen: Gen[A]): Gen[Try[A]] =
    Gen.choice1(genSuccess(gen).widen, genFailure.widen)
}
