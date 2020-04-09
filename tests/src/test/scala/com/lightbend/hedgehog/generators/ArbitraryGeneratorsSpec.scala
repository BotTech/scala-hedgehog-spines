package com.lightbend.hedgehog.generators

import com.lightbend.hedgehog.generators.ArbitraryGenerators._
import com.lightbend.hedgehog.generators.IntGenerators._
import com.lightbend.hedgehog.implicits.ArbitraryImplicits._
import com.lightbend.hedgehog.implicits.TryImplicits._
import com.lightbend.hedgehog.scalamock.{GeneratorSpec, TestRunnerMock}
import hedgehog._
import hedgehog.runner._
import org.scalactic.TripleEquals._

import scala.util.{Success, Try}

object ArbitraryGeneratorsSpec extends TestRunnerMock with GeneratorSpec {

  // TODO: Test probabilities of generated values.

  override def tests: List[Test] =
    test("genFrequency", genFreq)
      .addProp(_ + " returns the correct frequency", propCorrectFrequency)
      .addProp(_ + " blows up if args too far apart", propFrequencyFails)
      .tests ++
      test("genElementFrequency", genElemFreq)
        .addProp(_ + " returns the correct element frequency", propCorrectElementFrequency)
        .tests ++
      test("genElementFrequencyUnsafe", genElemFreqUnsafe)
        .addProp(_ + " returns the correct element frequency unsafely", propCorrectElementFrequencyUnsafe)
        .tests

  private def propCorrectFrequency: Property = forAll {
    for {
      (a, b) <- genFreqArgs
    } yield Try(genFreqPair(a, b)._1) ==== Success(math.max(a, b) - math.min(a, b) + 1)
  }

  private def propFrequencyFails: Property = forAll {
    for {
      (a, b) <- genInvalidFreqArgs
    } yield Try(genFreqPair(a, b)).failedResult(_.isA[ArithmeticException])
  }

  private def propCorrectElementFrequency: Property = forAll {
    for {
      x  <- genInt
      xs <- genInt.list(Range.linear(1, 100))
    } yield Try(genElementFrequency(x, xs: _*)._1) ==== Success(xs.size + 1)
  }

  private def propCorrectElementFrequencyUnsafe: Property = forAll {
    for {
      xs <- genInt.list(Range.linear(1, 100))
    } yield Try(genElementFrequencyUnsafe(xs)._1) ==== Success(xs.size)
  }

  private def genFreq: Gen[(Int, Int)] =
    genFreqArgs.flatMap {
      case (a, b) => genFreqPair(a, b)._2
    }

  private def genFreqArgs: Gen[(Int, Int)] =
    for {
      a <- genInt
      // TODO: Generalise this.
      b <- if (a < 0) Gen.int(Range.linearFrom(a, Int.MinValue, a + Int.MaxValue - 1))
      else if (a === 0) Gen.int(Range.linearFrom(a, Int.MinValue, Int.MaxValue))
      else Gen.int(Range.linearFrom(a, Int.MaxValue, a - Int.MaxValue + 1))
    } yield (a, b)

  private def genInvalidFreqArgs: Gen[(Int, Int)] =
    for {
      a <- genInt
      // TODO: Generalise this.
      b <- if (a < 0) Gen.int(Range.linear(a + Int.MaxValue, Int.MaxValue))
      else if (a === 0) Gen.constant(Int.MaxValue)
      else Gen.int(Range.linear(a - Int.MaxValue, Int.MinValue))
    } yield (a, b)

  private def genFreqPair[A: Numeric](a: A, b: A): (Int, Gen[(A, A)]) = genFrequency(a, b) {
    case (a, b) => Gen.constant((a, b))
  }

  private def genElemFreq: Gen[Int] =
    for {
      x  <- genInt
      xs <- genInt.list(Range.linear(1, 100))
      (_, gen) = genElementFrequency(x, xs: _*)
      i <- gen
    } yield i

  private def genElemFreqUnsafe: Gen[Int] =
    for {
      xs <- genInt.list(Range.linear(1, 100))
      (_, gen) = genElementFrequencyUnsafe(xs)
      i <- gen
    } yield i
}
