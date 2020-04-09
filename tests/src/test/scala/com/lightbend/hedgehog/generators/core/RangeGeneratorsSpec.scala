package com.lightbend.hedgehog.generators.core

import com.lightbend.hedgehog.generators.DoubleGenerators._
import com.lightbend.hedgehog.generators.IntGenerators._
import com.lightbend.hedgehog.generators.core.RangeGenerators._
import com.lightbend.hedgehog.generators.core.SizeGenerators._
import com.lightbend.hedgehog.implicits.ArbitraryImplicits._
import com.lightbend.hedgehog.scalamock.{GeneratorSpec, TestRunnerMock}
import com.lightbend.hedgehog.testkit.Probabilities
import hedgehog._
import hedgehog.core.Cover
import hedgehog.runner.Test
import org.scalactic.TripleEquals._

import scala.Ordering.Double.TotalOrdering
import scala.math.Ordering

object RangeGeneratorsSpec extends TestRunnerMock with GeneratorSpec with Probabilities {

  override def tests: List[Test] =
    singletonRangeTests ++
      constantRangeTests ++
      linearRangeTests("genLinearRange", genLinearRange(genInt)) ++
      linearRangeTests("genLinearFracRange", genLinearFracRange(genDouble)) ++
      numericRangeTests("genShortRange", genShortRange) ++
      numericRangeTests("genIntRange", genIntRange) ++
      numericRangeTests("genLongRange", genLongRange) ++
      numericRangeTests("genDoubleRange", genDoubleRange) ++
      xzyTests

  private def singletonRangeTests =
    test("genSingletonRange", genSingletonRange(genInt))
      .addGenProp(_ + " has origin within bounds", propOriginWithinBounds)
      .addGenProp(_ + " does not scale with size", propNotScalesWithSize)
      .addGenProp(_ + " generates only a single value", propSingleValue)
      .addGenProbabilities(_ + " generates ranges with probabilities", propProbabilities)
      .tests

  private def constantRangeTests =
    test("genConstantRange", genConstantRange(genInt))
      .addGenProp(_ + " has origin within bounds", propOriginWithinBounds)
      .addGenProp(_ + " does not scale with size", propNotScalesWithSize)
      .addGenProbabilities(_ + " generates ranges with probabilities", propProbabilities)
      .tests

  private def linearRangeTests[A: Ordering](name: String, gen: Gen[Range[A]]) =
    test(name, gen)
      .addGenProp(_ + " has origin within bounds", propOriginWithinBounds)
      .addGenProp(_ + " does scale with size", propDoesScaleWithSize)
      .addGenProbabilities(_ + " generates values with probabilities", propProbabilities)
      .tests

  private def numericRangeTests[A: Ordering](name: String, gen: Gen[Range[A]]) =
    test(name, gen)
      .addGenProp(_ + " has origin within bounds", propOriginWithinBounds)
      .addGenProbabilities(_ + " generates values with probabilities", propNumericRangeProbabilities)
      .tests

  private def xzyTests =
    test("genZXY", genZXY(genInt))
      .addGenProp(_ + " generates z inside x and y", propGenZXYZInside)
      .addGenProbabilities(_ + " generates values with probabilities", propGenZXYProbabilities)
      .tests

  private def propOriginWithinBounds[A: Ordering](gen: Gen[Range[A]]): Property =
    gen.map(range => testOriginWithinBounds(range)).forAll

  private def propSingleValue[A](gen: Gen[Range[A]]): Property = forAll {
    for {
      size  <- genSize
      range <- gen.resize(size)
    } yield {
      val (x, y) = range.bounds(size)
      x ==== y
    }
  }

  private def propNotScalesWithSize[A](gen: Gen[Range[A]]): Property =
    propScalesWithSize(gen, scales = false)

  private def propDoesScaleWithSize[A](gen: Gen[Range[A]]): Property =
    propScalesWithSize(gen, scales = true)

  private def propScalesWithSize[A](gen: Gen[Range[A]], scales: Boolean): Property = forAll {
    for {
      sizes <- genDiffSizes
      (sizeA, sizeB) = sizes
      range <- gen
    } yield
      if (scales) range.bounds(sizeA) !=== range.bounds(sizeB)
      else range.bounds(sizeA) ==== range.bounds(sizeB)
  }

  private def propProbabilities[A: Ordering](gen: Gen[Range[A]]): Property =
    gen.forAll
      .cover(OneToOne, "forwards", forwardsCover)
      .cover(OneToOne, "backwards", backwardsCover)
      .map(_ => Result.success)

  private def propNumericRangeProbabilities[A: Ordering](genRange: Gen[Range[A]]): Property = {
    val gen = for {
      range <- genRange
      sizes <- genDiffSizes
      (sizeA, sizeB) = sizes
    } yield (range, sizeA, sizeB)
    gen.forAll
    // We get a few extra singletons here from the constants but that's ok.
      .cover(OneToTwo, "singleton", (singletonCover[A] _).tupled)
      .cover(OneToTwo, "constant", (constantCover[A] _).tupled)
      .cover(OneToTwo, "linear", (linearCover[A] _).tupled)
      .cover(OneToOne, "forwards", x => forwardsCover(x._1))
      .cover(OneToOne, "backwards", x => backwardsCover(x._1))
      .map(_ => Result.success)
  }

  private def singletonCover[A](range: Range[A], sizeA: Size, sizeB: Size): Cover = {
    val (xA, yA) = range.bounds(sizeA)
    val (xB, yB) = range.bounds(sizeB)
    xA === xB && yA === yB && xA === yA
  }

  private def constantCover[A](range: Range[A], sizeA: Size, sizeB: Size): Cover = {
    val (xA, yA) = range.bounds(sizeA)
    val (xB, yB) = range.bounds(sizeB)
    xA === xB && yA === yB && (xA !== yA)
  }

  private def linearCover[A](range: Range[A], sizeA: Size, sizeB: Size): Cover =
    range.bounds(sizeA) !== range.bounds(sizeB)

  private def forwardsCover[A](range: Range[A])(implicit ordering: Ordering[A]): Cover = {
    import ordering._
    x(range) <= y(range)
  }

  private def backwardsCover[A](range: Range[A])(implicit ordering: Ordering[A]): Cover = {
    import ordering._
    y(range) <= x(range)
  }

  private def propGenZXYZInside[A: Ordering](gen: Gen[(A, A, A)]): Property =
    gen.map {
      case (z, x, y) =>
        x.lessThanOrEqual(z)
          .and(z.lessThanOrEqual(y))
          .or(y.lessThanOrEqual(z).and(z.lessThanOrEqual(x)))
    }.forAll

  private def propGenZXYProbabilities[A](gen: Gen[(A, A, A)])(implicit o: Ordering[A]): Property =
    gen.forAll
      .cover(OneToTenThousand, "different", (differentCover _).tupled)
      .cover(OneToOne, "x <= y", { case (_, x, y) => o.lteq(x, y) })
      .cover(OneToOne, "y <= x", { case (_, x, y) => o.lteq(y, x) })
      .map(_ => Result.success)

  private def differentCover(a: Any, b: Any, c: Any): Cover =
    !(a === b || b === c || c === a)

  private def genDiffSizes: Gen[(Size, Size)] =
    for {
      sizeA <- genSize
      // We could just use a filter but there are two problems with that:
      // 1) It creates invalid Sizes https://github.com/hedgehogqa/scala-hedgehog/issues/137.
      // 2) They will count towards the number of discards and so will fail in the probability property.
      sizeB <- Gen.int(Range.linear(sizeA.value + 1, sizeA.value + Size.max))
    } yield (sizeA, Size(sizeB % (Size.max + 1)))

  private def testOriginWithinBounds[A: Ordering](range: Range[A]): Result =
    range.origin.inside(x(range), y(range))

  // Don't use the RangeImplicits to get these otherwise there could be some circular reasoning in the tests.
  private def x[A](range: Range[A]): A = range.bounds(Size(Size.max))._1
  private def y[A](range: Range[A]): A = range.bounds(Size(Size.max))._2
}
