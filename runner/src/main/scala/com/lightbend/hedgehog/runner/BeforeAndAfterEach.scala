package com.lightbend.hedgehog.runner

import hedgehog.core.Tree

trait BeforeAndAfterEach extends WrappedTests {
  this: Runner =>

  abstract override protected def wrapTest[A](tree: => Tree[A]): Tree[A] =
    try {
      beforeEach()
      super.wrapTest(tree)
    } finally {
      afterEach()
    }

  /**
    * This is executed once before each test, including reruns with shrunken values..
    */
  @SuppressWarnings(Array("EmptyMethod"))
  protected def beforeEach(): Unit = ()

  /**
    * This is executed once after each test, including reruns with shrunken values.
    */
  @SuppressWarnings(Array("EmptyMethod"))
  protected def afterEach(): Unit = ()
}
