package com.lightbend.hedgehog.generators.core

import com.lightbend.hedgehog.generators.{DoubleGenerators, IntGenerators, LongGenerators, ShortGenerators}
import hedgehog.core.NumericPlus
import hedgehog.predef.{DecimalPlus, IntegralPlus}
import hedgehog.{Gen, Range}

object RangeGenerators {

  def genSingletonRange[A](gen: Gen[A]): Gen[Range[A]] =
    gen.map(Range.singleton)

  def genConstantRange[A: Ordering](gen: Gen[A]): Gen[Range[A]] =
    genZXY(gen).map((Range.constantFrom[A] _).tupled)

  def genLinearRange[A: Integral: IntegralPlus: NumericPlus](gen: Gen[A]): Gen[Range[A]] =
    genZXY(gen).map((Range.linearFrom[A] _).tupled)

  def genLinearFracRange[A: Fractional: DecimalPlus: NumericPlus](gen: Gen[A]): Gen[Range[A]] =
    genZXY(gen).map((Range.linearFracFrom[A] _).tupled)

  def genRange[A: Integral: IntegralPlus: NumericPlus](gen: Gen[A]): Gen[Range[A]] =
    Gen.choice1(genSingletonRange(gen), genConstantRange(gen), genLinearRange(gen))

  def genFracRange[A: Fractional: DecimalPlus: NumericPlus](gen: Gen[A]): Gen[Range[A]] =
    Gen.choice1(genSingletonRange(gen), genConstantRange(gen), genLinearFracRange(gen))

  def genShortRange: Gen[Range[Short]] =
    genRange(ShortGenerators.genShort)

  def genIntRange: Gen[Range[Int]] =
    genRange(IntGenerators.genInt)

  def genLongRange: Gen[Range[Long]] =
    genRange(LongGenerators.genLong)

  def genDoubleRange: Gen[Range[Double]] =
    genFracRange(DoubleGenerators.genDouble)

  /**
    * Generates three values `(z, x, y)` where `z` is between `x` and `y`. The ordering of `x` and `y` is random.
    */
  private[generators] def genZXY[A](gen: Gen[A])(implicit ordering: Ordering[A]): Gen[(A, A, A)] =
    for {
      a <- gen
      b <- gen
      c <- gen
    } yield {
      import ordering._
      // Given three values we just have to find the one that is between the other two.
      if (a <= b) {
        if (b <= c) (b, a, c)
        else if (a <= c) (c, a, b)
        else (a, b, c)
      } else {
        if (a <= c) (a, b, c)
        else if (b <= c) (c, a, b)
        else (b, a, c)
      }
    }
}
