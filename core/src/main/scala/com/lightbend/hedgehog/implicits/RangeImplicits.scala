package com.lightbend.hedgehog.implicits

import com.lightbend.hedgehog.Ranges._
import com.lightbend.hedgehog.implicits.RangeImplicits.RangeSyntax
import hedgehog.{Range, Size}
import org.scalactic.TripleEquals._

trait RangeImplicits {

  implicit def rangeSyntax[A](range: Range[A]): RangeSyntax[A] = new RangeSyntax[A](range)
}

object RangeImplicits {

  implicit class RangeSyntax[A](private val range: Range[A]) extends AnyVal {

    def x: A = range.bounds(Size(Size.max))._1

    def y: A = range.bounds(Size(Size.max))._2

    def min(implicit o: Ordering[A]): A = o.min(x, y)

    def max(implicit o: Ordering[A]): A = o.max(x, y)

    // This only works for all the known Numerics. Custom Numerics may fail.
    def length(implicit n: Numeric[A]): Int = {
      import n._
      val min    = range.min
      val max    = range.max
      val intMax = fromInt(Int.MaxValue)
      // Fortunately all large Numerics can store the max Int without any loss in
      // precision otherwise this would not work.
      def smallerThanInt = toInt(intMax) !== Int.MaxValue
      if (smallerThanInt) {
        // This could overflow an A but the largest Numeric that is smaller than an
        // Int is a Short or Char which not overflow an Int.
        // Short.MaxValue.toInt - Short.MinValue.toInt === 65535
        toInt(max) - toInt(min) + 1
      } else {
        val diff = max - min
        // Check for overflow.
        if (min < zero && diff < max) {
          // Oops we overflowed A so we would definitely overflow an Int.
          Int.MaxValue
        } else {
          val length = n.min(diff, intMax).toInt
          if (length < Int.MaxValue) length + 1
          else length
        }
      }
    }

    def clampX(x: A)(implicit n: Numeric[A]): Range[A] = clamp(x, y)

    def clampY(y: A)(implicit n: Numeric[A]): Range[A] = clamp(x, y)

    def clamp(x: A, y: A)(implicit n: Numeric[A]): Range[A] = adjust(Range.clamp(x, y, _))

    def clampMin(min: A)(implicit o: Ordering[A]): Range[A] = adjust(o.max(min, _))

    def clampMax(max: A)(implicit o: Ordering[A]): Range[A] = adjust(o.min(max, _))

    private def adjust(f: A => A): Range[A] = {
      def shiftBounds(size: Size): (A, A) = range.bounds(size) match {
        case (a, b) => (f(a), f(b))
      }
      Range(
        origin = f(range.origin),
        bounds = shiftBounds
      )
    }

    def singleton: Boolean = {
      val (x, y) = range.bounds(Size(Size.max))
      x === y
    }

    def constant: Boolean = range.bounds(Size(0)) === range.bounds(Size(Size.max))

    def linear: Boolean = !constant

    def scalingMode: ScalingMode =
      if (singleton) Singleton
      else if (linear) Linear
      else Constant
  }
}
