package com.lightbend.hedgehog.generators

import com.lightbend.hedgehog.generators.ShortGenerators._
import com.lightbend.hedgehog.scalamock.{GeneratorSpec, TestRunnerMock}
import hedgehog.runner.Test

object ShortGeneratorsSpec extends TestRunnerMock with GeneratorSpec {

  override def tests: List[Test] =
    test("genShort", genShort).addLinearNumericRangeTests(0, Short.MinValue, Short.MaxValue).tests
}
