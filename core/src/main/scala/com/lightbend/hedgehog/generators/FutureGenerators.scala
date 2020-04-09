package com.lightbend.hedgehog.generators

import hedgehog.Gen

import scala.concurrent.{Future, Promise}
import ExceptionGenerators._

object FutureGenerators {

  def genSuccessfulFuture[A](gen: Gen[A]): Gen[Future[A]] =
    gen.map(Future.successful)

  def genFailedFuture[A]: Gen[Future[A]] =
    genException.map(Future.failed)

  def genCompletedFuture[A](gen: Gen[A]): Gen[Future[A]] =
    Gen.choice1(genSuccessfulFuture(gen), genFailedFuture)

  def genIncompleteFuture[A]: Gen[Future[A]] =
    Gen.constant(Promise().future)

  def genFuture[A](gen: Gen[A]): Gen[Future[A]] =
    Gen.choice1(genCompletedFuture(gen), genIncompleteFuture)
}
