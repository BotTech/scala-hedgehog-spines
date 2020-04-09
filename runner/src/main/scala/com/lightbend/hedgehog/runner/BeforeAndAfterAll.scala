package com.lightbend.hedgehog.runner

import hedgehog.core.PropertyConfig
import hedgehog.runner.Test

trait BeforeAndAfterAll extends Runner {

  abstract override def runTests[A](
      className: String,
      tests: List[Test],
      config: PropertyConfig,
      seed: Long,
      manager: Manager[A]
    ): Unit =
    try {
      beforeAll()
      super.runTests(className, tests, config, seed, manager)
    } finally {
      afterAll()
    }

  /**
    * This is executed once before all tests in a suite.
    */
  @SuppressWarnings(Array("EmptyMethod"))
  protected def beforeAll(): Unit = ()

  /**
    * This is executed once after all tests in a suite.
    */
  @SuppressWarnings(Array("EmptyMethod"))
  protected def afterAll(): Unit = ()
}
