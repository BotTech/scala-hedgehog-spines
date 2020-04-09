package com.lightbend.hedgehog.generators

import com.lightbend.hedgehog.generators.DoubleGenerators._
import com.lightbend.hedgehog.scalamock.{GeneratorSpec, TestRunnerMock}
import hedgehog.runner.Test

object DoubleGeneratorsSpec extends TestRunnerMock with GeneratorSpec {

  override def tests: List[Test] =
    test("genDouble", genDouble).addLinearNumericRangeTests(0, Double.MinValue, Double.MaxValue).tests
}
