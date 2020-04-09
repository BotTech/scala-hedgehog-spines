package com.lightbend.hedgehog.implicits

import com.lightbend.hedgehog.Logs
import com.lightbend.hedgehog.Ranges._
import com.lightbend.hedgehog.generators.IntGenerators._
import com.lightbend.hedgehog.generators.core.RangeGenerators._
import com.lightbend.hedgehog.implicits.ArbitraryImplicits._
import com.lightbend.hedgehog.runner.TestRunner
import hedgehog._
import hedgehog.runner.{property, Test}

import scala.math.Ordering.Double.TotalOrdering

// scalastyle:off number.of.methods

object RangeSyntaxSpec extends TestRunner with Logs with RangeImplicits {

  override def tests: List[Test] = List(
    property("x of constant range is first bound", propXConstantRangeIsFirstBound),
    property("x of linear range is first bound", propXLinearRangeIsFirstBound),
    property("y of constant range is second bound", propYConstantRangeIsSecondBound),
    property("y of linear range is second bound", propYLinearRangeIsSecondBound),
    property("min of constant range is lowest of either bound", propMinConstantRangeIsLowestBound),
    property("min of linear range is lowest of either bound", propMinLinearRangeIsLowestBound),
    property("max of constant range is highest of either bound", propMaxConstantRangeIsHighestBound),
    property("max of linear range is highest of either bound", propMaxLinearRangeIsHighestBound),
    property("length of range is the distance between int bounds", propLengthRangeDistanceBetweenIntBounds),
    property("length of range is the distance between short bounds", propLengthRangeDistanceBetweenShortBounds),
    property("length of range is the distance between long bounds", propLengthRangeDistanceBetweenLongBounds),
    property("length of range is the distance between double bounds", propLengthRangeDistanceBetweenDoubleBounds),
    property("clampX on range clamps x", propClampXRangeXClamped),
    property("clampX on range clamps origin", propClampXRangeOriginClamped),
    property("clampX on range leaves y unchanged", propClampXRangeYUnchanged),
    property("clampY on range clamps y", propClampYRangeYClamped),
    property("clampY on range clamps origin", propClampYRangeOriginClamped),
    property("clampY on range leaves x unchanged", propClampYRangeXUnchanged),
    property("clamp on range clamps x", propClampRangeXClamped),
    property("clamp on range clamps origin", propClampRangeOriginClamped),
    property("clamp on range clamps y", propClampRangeYClamped),
    property("clampMin on range clamps x", propClampMinRangeXClamped),
    property("clampMin on range clamps origin", propClampMinRangeOriginClamped),
    property("clampMin on range clamps y", propClampMinRangeYClamped),
    property("clampMax on range clamps x", propClampMaxRangeXClamped),
    property("clampMax on range clamps origin", propClampMaxRangeOriginClamped),
    property("clampMax on range clamps y", propClampMaxRangeYClamped),
    property("singleton range is a singleton", propSingletonRangeIsSingleton),
    property("constant range is not a singleton", propConstantRangeIsNotSingleton),
    property("linear range is not a singleton", propLinearRangeIsNotSingleton),
    property("singleton range is constant", propSingletonRangeIsConstant),
    property("constant range is constant", propConstantRangeIsConstant),
    property("linear range is not constant", propLinearRangeIsNotConstant),
    property("singleton range is not linear", propSingletonRangeIsNotLinear),
    property("constant range is not linear", propConstantRangeIsNotLinear),
    property("linear range is linear", propLinearRangeIsLinear)
  )

  private def propXConstantRangeIsFirstBound: Property = forAll {
    genZXY.map {
      case (z, x, y) => Range.constantFrom(z, x, y).x ==== x
    }
  }

  private def propXLinearRangeIsFirstBound: Property = forAll {
    genZXY.map {
      case (z, x, y) => Range.linearFrom(z, x, y).x ==== x
    }
  }

  private def propYConstantRangeIsSecondBound: Property = forAll {
    genZXY.map {
      case (z, x, y) => Range.constantFrom(z, x, y).y ==== y
    }
  }

  private def propYLinearRangeIsSecondBound: Property = forAll {
    genZXY.map {
      case (z, x, y) => Range.linearFrom(z, x, y).y ==== y
    }
  }

  private def propMinConstantRangeIsLowestBound: Property = forAll {
    genZXY.map {
      case (z, x, y) => Range.constantFrom(z, x, y).min ==== math.min(x, y)
    }
  }

  private def propMinLinearRangeIsLowestBound: Property = forAll {
    genZXY.map {
      case (z, x, y) => Range.linearFrom(z, x, y).min ==== math.min(x, y)
    }
  }

  private def propMaxConstantRangeIsHighestBound: Property = forAll {
    genZXY.map {
      case (z, x, y) => Range.constantFrom(z, x, y).max ==== math.max(x, y)
    }
  }

  private def propMaxLinearRangeIsHighestBound: Property = forAll {
    genZXY.map {
      case (z, x, y) => Range.linearFrom(z, x, y).max ==== math.max(x, y)
    }
  }

  private def propLengthRangeDistanceBetweenIntBounds: Property = forAll {
    genIntRange.map { range =>
      val length = range.max.toLong - range.min.toLong + 1
      range.length ==== math.min(Int.MaxValue, length).toInt
    }
  }

  private def propLengthRangeDistanceBetweenShortBounds: Property = forAll {
    genShortRange.map { range =>
      val length = range.max.toInt - range.min.toInt + 1
      range.length ==== length
    }
  }

  private def propLengthRangeDistanceBetweenLongBounds: Property = forAll {
    genLongRange.map { range =>
      val length = BigInt(range.max) - BigInt(range.min) + 1
      range.length ==== Ordering.BigInt.min(BigInt(Int.MaxValue), length).toInt
    }
  }

  private def propLengthRangeDistanceBetweenDoubleBounds: Property = forAll {
    genDoubleRange.map { range =>
      val length = BigDecimal(range.max) - BigDecimal(range.min) + 1
      range.length ==== Ordering.BigDecimal.min(BigDecimal(Int.MaxValue), length).toInt
    }
  }

  private def propClampXRangeXClamped: Property =
    propClampXRangeNClamped(_.x)

  private def propClampXRangeOriginClamped: Property =
    propClampXRangeNClamped(_.origin)

  private def propClampXRangeNClamped(n: Range[Int] => Int): Property =
    for {
      range <- genIntRange.forAllWithLog(logRange)
      x     <- genInt.log("x")
    } yield {
      val clamped = range.clampX(x)
      testClamped(x, range.y, n(range), n(clamped))
    }

  private def propClampYRangeYClamped: Property =
    propClampYRangeNClamped(_.y)

  private def propClampYRangeOriginClamped: Property =
    propClampYRangeNClamped(_.origin)

  private def propClampYRangeNClamped(n: Range[Int] => Int): Property =
    for {
      range <- genIntRange.forAllWithLog(logRange)
      y     <- genInt.log("y")
    } yield {
      val clamped = range.clampY(y)
      testClamped(range.x, y, n(range), n(clamped))
    }

  private def propClampRangeXClamped: Property =
    propClampRangeNClamped(_.x)

  private def propClampRangeYClamped: Property =
    propClampRangeNClamped(_.y)

  private def propClampRangeOriginClamped: Property =
    propClampRangeNClamped(_.origin)

  private def propClampRangeNClamped(n: Range[Int] => Int): Property =
    for {
      range <- genIntRange.forAllWithLog(logRange)
      x     <- genInt.log("x")
      y     <- genInt.log("y")
    } yield {
      val clamped = range.clamp(x, y)
      testClamped(x, y, n(range), n(clamped))
    }

  private def propClampMinRangeXClamped: Property =
    propClampMinRangeNClamped(_.x)

  private def propClampMinRangeYClamped: Property =
    propClampMinRangeNClamped(_.y)

  private def propClampMinRangeOriginClamped: Property =
    propClampMinRangeNClamped(_.origin)

  private def propClampMinRangeNClamped(n: Range[Int] => Int): Property =
    for {
      min   <- genInt.log("min")
      range <- genIntRange.forAllWithLog(logRange)
    } yield {
      val clamped = range.clampMin(min)
      testClamped(math.max(min, range.x), math.max(min, range.y), n(range), n(clamped))
    }

  private def propClampMaxRangeXClamped: Property =
    propClampMaxRangeNClamped(_.x)

  private def propClampMaxRangeYClamped: Property =
    propClampMaxRangeNClamped(_.y)

  private def propClampMaxRangeOriginClamped: Property =
    propClampMaxRangeNClamped(_.origin)

  private def propClampMaxRangeNClamped(n: Range[Int] => Int): Property =
    for {
      max   <- genInt.log("max")
      range <- genIntRange.forAllWithLog(logRange)
    } yield {
      val clamped = range.clampMax(max)
      testClamped(math.min(max, range.x), math.min(max, range.y), n(range), n(clamped))
    }

  private def testClamped[A](x: A, y: A, originalN: A, clampedN: A)(implicit ordering: Ordering[A]): Result = {
    import ordering._
    val minXY = min(x, y)
    val maxXY = max(x, y)
    if (originalN <= minXY) clampedN ==== minXY
    else if (originalN >= maxXY) clampedN ==== maxXY
    else clampedN ==== originalN
  }

  private def propSingletonRangeIsSingleton: Property =
    genSingletonRange(genInt).forAll.map(rangeType(_.singleton, Singleton))

  private def propConstantRangeIsNotSingleton: Property =
    genConstantRange(genInt).forAll.map(notRangeType(_.singleton, Singleton))

  private def propLinearRangeIsNotSingleton: Property =
    genLinearRange(genInt).forAll.map(notRangeType(_.singleton, Singleton))

  private def propSingletonRangeIsConstant: Property =
    genSingletonRange(genInt).forAll.map(rangeType(_.constant, Singleton))

  private def propConstantRangeIsConstant: Property =
    genConstantRange(genInt).forAll.map(rangeType(_.constant, Constant))

  private def propLinearRangeIsNotConstant: Property =
    genLinearRange(genInt).forAll.map(notRangeType(_.constant, Constant))

  private def propSingletonRangeIsNotLinear: Property =
    genSingletonRange(genInt).forAll.map(notRangeType(_.linear, Linear))

  private def propConstantRangeIsNotLinear: Property =
    genConstantRange(genInt).forAll.map(notRangeType(_.linear, Linear))

  private def propLinearRangeIsLinear: Property =
    genLinearRange(genInt).forAll.map(rangeType(_.linear, Linear))

  private def propClampXRangeYUnchanged: Property = forAll {
    for {
      range <- genIntRange
      x     <- genInt
    } yield {
      val clamped = range.clampX(x)
      clamped.y ==== range.y
    }
  }

  private def propClampYRangeXUnchanged: Property = forAll {
    for {
      range <- genIntRange
      y     <- genInt
    } yield {
      val clamped = range.clampY(y)
      clamped.x ==== range.x
    }
  }

  private def genZXY: Gen[(Int, Int, Int)] =
    for {
      // In practice z should be between x and y but for these tests they do not use it
      // which is a nice property in and of itself.
      z <- genInt
      x <- genInt
      y <- genInt
    } yield (z, x, y)

  private def rangeType[A](p: Range[A] => Boolean, mode: ScalingMode)(range: Range[A]): Result =
    Result.assert(p(range)).and(range.scalingMode ==== mode)

  private def notRangeType[A](p: Range[A] => Boolean, mode: ScalingMode)(range: Range[A]): Result =
    Result.assert(!p(range)).and(range.scalingMode !=== mode)
}
