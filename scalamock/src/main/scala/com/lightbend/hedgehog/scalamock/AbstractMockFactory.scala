package com.lightbend.hedgehog.scalamock

import org.scalamock.MockFactoryBase

trait AbstractMockFactory extends MockFactoryBase {

  override type ExpectationException = TestFailedException

  override protected def newExpectationException(message: String, methodName: Option[Symbol]): TestFailedException =
    TestFailedException(message)
}
