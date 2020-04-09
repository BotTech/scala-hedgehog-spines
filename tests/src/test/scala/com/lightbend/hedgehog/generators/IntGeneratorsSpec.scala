package com.lightbend.hedgehog.generators

import com.lightbend.hedgehog.generators.IntGenerators._
import com.lightbend.hedgehog.scalamock.{GeneratorSpec, TestRunnerMock}
import hedgehog.runner.Test

object IntGeneratorsSpec extends TestRunnerMock with GeneratorSpec {

  override def tests: List[Test] =
    test("genZero", genZero).addConstantTest(_ + " generates zero for all sizes", 0).tests ++
      test("genNegativeInt", genNegativeInt).addLinearNumericRangeTests(-1, -1, Int.MinValue).tests ++
      test("genNonPositiveInt", genNonPositiveInt).addLinearNumericRangeTests(0, 0, Int.MinValue).tests ++
      test("genNonNegativeInt", genNonNegativeInt).addLinearNumericRangeTests(0, 0, Int.MaxValue).tests ++
      test("genPositiveInt", genPositiveInt).addLinearNumericRangeTests(1, 1, Int.MaxValue).tests ++
      test("genInt", genInt).addLinearNumericRangeTests(0, Int.MinValue, Int.MaxValue).tests
}
