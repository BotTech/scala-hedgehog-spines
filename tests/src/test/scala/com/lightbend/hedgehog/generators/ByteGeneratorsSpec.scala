package com.lightbend.hedgehog.generators

import com.lightbend.hedgehog.generators.ByteGenerators._
import com.lightbend.hedgehog.scalamock.{GeneratorSpec, TestRunnerMock}
import hedgehog.runner.Test

object ByteGeneratorsSpec extends TestRunnerMock with GeneratorSpec {

  // TODO: Add generic collection tests.

  override def tests: List[Test] =
    test("genByte", genByte).addLinearNumericRangeTests(0, Byte.MinValue, Byte.MaxValue).tests ++
      test("genBytes", genBytes).tests ++
      test("genNonEmptyBytes", genNonEmptyBytes).tests
}
