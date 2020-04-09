package com.lightbend.hedgehog.scalamock

import com.lightbend.hedgehog.testkit.{GeneratorResults, Seeds}
import org.scalamock.MockFactoryBase

trait GeneratorMockResults extends GeneratorResults {
  this: MockFactoryBase =>

  val mockSeeds: MockSeeds = new MockSeeds with MockFactoryBase {
    override type ExpectationException = GeneratorMockResults.this.ExpectationException

    override protected def newExpectationException(message: String, methodName: Option[Symbol]): ExpectationException =
      GeneratorMockResults.this.newExpectationException(message, methodName)
  }

  override def seeds: Seeds = mockSeeds
}
