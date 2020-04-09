package com.lightbend.hedgehog.scalamock

import com.lightbend.hedgehog.testkit.GeneratorTests
import com.lightbend.hedgehog.testkit.GeneratorTests._
import com.lightbend.hedgehog.{Logs, Sizes}
import hedgehog.Gen
import hedgehog.core.NumericPlus
import hedgehog.predef.IntegralPlus
import org.scalamock.MockFactoryBase

// TODO: Re-implement this. It is a bit awkward to use.

trait GeneratorSpec extends Logs with GeneratorMockResults with Sizes {
  this: MockFactoryBase =>

  def test[A](name: String, gen: Gen[A]): GeneratorTests[A] =
    GeneratorTests[A](name, gen)

  implicit class GeneratorMockTests[A](tests: GeneratorTests[A]) {

    def addSingletonTests(value: A): GeneratorTests[A] =
      tests.addGenTests(singletonTests(value))

    def addConstantRangeTests(min: A, max: A)(implicit o: Ordering[A]): GeneratorTests[A] =
      tests.addGenTests(constantRangeTests(GeneratorSpec.this, min, max))

    def addLinearRangeTests(min: A, max: A)(scaleMin: A => A)(implicit o: Ordering[A]): GeneratorTests[A] =
      tests.addGenTests(linearRangeTests(GeneratorSpec.this, min, max)(scaleMin))

    def addLinearNumericRangeTests(
        origin: A,
        min: A,
        max: A
      )(implicit n: Numeric[A],
        np: NumericPlus[A]
      ): GeneratorTests[A] =
      tests.addGenTests(linearNumericRangeTests(GeneratorSpec.this, origin, min, max))

    def addLinearBigNumericRangeTests(
        origin: A,
        min: A,
        max: A
      )(implicit n: Numeric[A],
        ip: IntegralPlus[A]
      ): GeneratorTests[A] =
      tests.addGenTests(linearBigNumericRangeTests(GeneratorSpec.this, origin, min, max))
  }
}
