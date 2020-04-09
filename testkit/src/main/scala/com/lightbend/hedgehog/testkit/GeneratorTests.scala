package com.lightbend.hedgehog.testkit

import com.lightbend.hedgehog.Sizes._
import com.lightbend.hedgehog.implicits.ArbitraryImplicits._
import com.lightbend.hedgehog.testkit.GeneratorProperties._
import hedgehog._
import hedgehog.core.NumericPlus
import hedgehog.predef.{DecimalPlus, IntegralPlus}
import hedgehog.runner._

class GeneratorTests[A](name: String, gen: Gen[A], testVector: Vector[Test]) {

  def tests: List[Test] = this.testVector.toList

  def addTest(test: Test): GeneratorTests[A] =
    new GeneratorTests[A](name, gen, this.testVector :+ test)

  def addTests(tests: List[Test]): GeneratorTests[A] =
    new GeneratorTests[A](name, gen, this.testVector :++ tests)

  def addGenTest(test: (String, Gen[A]) => Test): GeneratorTests[A] =
    addTest(test(name, gen))

  def addGenTests(tests: (String, Gen[A]) => List[Test]): GeneratorTests[A] =
    addTests(tests(name, gen))

  def addProp(name: String => String, property: Property): GeneratorTests[A] =
    addTest(runner.property(name(this.name), property))

  def addGenProp(name: String => String, property: Gen[A] => Property): GeneratorTests[A] =
    addProp(name, property(gen))

  def addPropWithConfig(name: String => String, property: Property, configure: Test => Test): GeneratorTests[A] =
    addTest(configure(runner.property(name(this.name), property)))

  def addGenPropWithConfig(
      name: String => String,
      property: Gen[A] => Property,
      configure: Test => Test
    ): GeneratorTests[A] =
    addPropWithConfig(name, property(gen), configure)

  def addExample(name: String => String, result: => Result): GeneratorTests[A] =
    addTest(example(name(this.name), result))

  def addGenExample(name: String => String, result: Gen[A] => Result): GeneratorTests[A] =
    addTest(example(name(this.name), result(gen)))

  def addConstantTest[B >: A](name: String => String, value: B): GeneratorTests[A] =
    addValueTest(name, (_: B) ==== value)

  def addValueTest[B >: A](name: String => String, result: B => Result): GeneratorTests[A] =
    addProp(name, gen.map(result).forAll)
}

object GeneratorTests {

  def apply[A](name: String, gen: Gen[A]): GeneratorTests[A] =
    new GeneratorTests[A](name, gen, Vector.empty).addGenTest(baseTest)

  def baseTest(name: String, gen: Gen[_]): Test =
    property(s"$name does not discard", propNoDiscards(gen))

  def singletonTests[A](value: A)(name: String, gen: Gen[A]): List[Test] =
    List(
      property(s"$name generates a single value", gen.forAll.map(_ ==== value))
    )

  def constantRangeTests[A: Ordering](
      results: GeneratorResults,
      min: A,
      max: A
    )(
      name: String,
      gen: Gen[A]
    ): List[Test] = List(
    property(s"$name generates values inside min and max", gen.forAll.map(_.inside(min, max))),
    example(s"$name generates min as minimum from", results.testGenMinFrom(gen, min)),
    example(s"$name generates max as minimum to", results.testGenMinTo(gen, max)),
    example(s"$name generates min as maximum from", results.testGenMaxFrom(gen, min)),
    example(s"$name generates max as maximum to", results.testGenMaxTo(gen, max))
  )

  def linearRangeTests[A: Ordering](
      results: GeneratorResults,
      min: A,
      max: A
    )(
      scaleMin: A => A
    )(
      name: String,
      gen: Gen[A]
    ): List[Test] = List(
    property(s"$name generates values inside min and max", gen.forAll.map(_.inside(min, max))),
    example(s"$name generates scaled min as minimum from", results.testGenMinFrom(gen, scaleMin(min))),
    example(s"$name generates scaled max as minimum to", results.testGenMinTo(gen, scaleMin(max))),
    example(s"$name generates min as maximum from", results.testGenMaxFrom(gen, min)),
    example(s"$name generates max as maximum to", results.testGenMaxTo(gen, max))
  )

  def linearNumericRangeTests[A: NumericPlus](
      results: GeneratorResults,
      origin: A,
      min: A,
      max: A
    )(
      name: String,
      gen: Gen[A]
    )(implicit n: Numeric[A]
    ): List[Test] = linearRangeTests(results, min, max)(a => n.plus(origin, minScaled(a)))(name, gen)

  def linearBigNumericRangeTests[A: IntegralPlus](
      results: GeneratorResults,
      origin: A,
      min: A,
      max: A
    )(
      name: String,
      gen: Gen[A]
    )(implicit n: Numeric[A]
    ): List[Test] = linearRangeTests(results, min, max)(a => n.plus(origin, minScaledBig(a)))(name, gen)

  def minScaled[A](a: A)(implicit n: NumericPlus[A]): A =
    n.timesDouble(a, MinSize.percentage)

  def minScaledBig[A](a: A)(implicit i: IntegralPlus[A]): A =
    i.fromBigInt(NumericPlus.BigIntRatio.timesDouble(i.toBigInt(a), MinSize.percentage))

  def minScaledFracBig[A](a: A)(implicit i: DecimalPlus[A]): A =
    i.fromBigDecimal(NumericPlus.BigDecimalRatio.timesDouble(i.toBigDecimal(a), MinSize.percentage))
}
