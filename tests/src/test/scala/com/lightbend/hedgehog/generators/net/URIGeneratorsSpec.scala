package com.lightbend.hedgehog.generators.net

import com.lightbend.hedgehog.generators.net.URIGenerators._
import com.lightbend.hedgehog.scalamock.{GeneratorSpec, TestRunnerMock}
import hedgehog.runner.Test

object URIGeneratorsSpec extends TestRunnerMock with GeneratorSpec {

  override def tests: List[Test] =
    test("genURI", genURI).tests
}
