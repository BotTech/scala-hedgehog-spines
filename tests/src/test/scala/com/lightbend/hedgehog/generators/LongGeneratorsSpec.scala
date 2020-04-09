package com.lightbend.hedgehog.generators

import com.lightbend.hedgehog.generators.IntGenerators.genZero
import com.lightbend.hedgehog.generators.LongGenerators._
import com.lightbend.hedgehog.scalamock.{GeneratorSpec, TestRunnerMock}
import hedgehog.runner.Test

object LongGeneratorsSpec extends TestRunnerMock with GeneratorSpec {

  override def tests: List[Test] =
    test("genZero", genZero).addConstantTest(_ + " generates zero for all sizes", 0).tests ++
      test("genNegativeLong", genNegativeLong).addLinearBigNumericRangeTests(-1, -1, Long.MinValue).tests ++
      test("genNonPositiveLong", genNonPositiveLong).addLinearBigNumericRangeTests(0, 0, Long.MinValue).tests ++
      test("genNonNegativeLong", genNonNegativeLong).addLinearBigNumericRangeTests(0, 0, Long.MaxValue).tests ++
      test("genPositiveLong", genPositiveLong).addLinearBigNumericRangeTests(1, 1, Long.MaxValue).tests ++
      test("genLong", genLong).addLinearBigNumericRangeTests(0, Long.MinValue, Long.MaxValue).tests
}
