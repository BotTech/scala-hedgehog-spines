package com.lightbend.hedgehog.testkit

import com.lightbend.hedgehog.Sizes.{MaxSize, MinSize}
import com.lightbend.hedgehog.implicits.GeneratorImplicits._
import hedgehog._
import hedgehog.core.Seed

trait GeneratorResults {

  def seeds: Seeds

  def testGenMinFrom[A](gen: Gen[A], min: A): Result =
    testGenMin(gen, seeds.fromSeed, min)

  def testGenMinResultFrom[A](gen: Gen[A])(result: A => Result): Result =
    testGenMinResult(gen, seeds.fromSeed)(result)

  def testGenMinTo[A](gen: Gen[A], min: A): Result =
    testGenMin(gen, seeds.toSeed, min)

  def testGenMinResultTo[A](gen: Gen[A])(result: A => Result): Result =
    testGenMinResult(gen, seeds.toSeed)(result)

  def testGenMaxFrom[A](gen: Gen[A], max: A): Result =
    testGenMax(gen, seeds.fromSeed, max)

  def testGenMaxResultFrom[A](gen: Gen[A])(result: A => Result): Result =
    testGenMaxResult(gen, seeds.fromSeed)(result)

  def testGenMaxTo[A](gen: Gen[A], max: A): Result =
    testGenMax(gen, seeds.toSeed, max)

  def testGenMaxResultTo[A](gen: Gen[A])(result: A => Result): Result =
    testGenMaxResult(gen, seeds.toSeed)(result)

  def testGenMin[A](gen: Gen[A], seed: Seed, min: A): Result =
    testGenMinResult(gen, seed)(_ ==== min)

  def testGenMinResult[A](gen: Gen[A], seed: Seed)(result: A => Result): Result =
    gen.generates(MinSize, seed)(result)

  def testGenMax[A](gen: Gen[A], seed: Seed, max: A): Result =
    testGenMaxResult(gen, seed)(_ ==== max)

  def testGenMaxResult[A](gen: Gen[A], seed: Seed)(result: A => Result): Result =
    gen.generates(MaxSize, seed)(result)
}
