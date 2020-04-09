package com.lightbend.hedgehog.scalamock

import com.lightbend.hedgehog.runner.{Runner, WrappedTests}
import hedgehog.core.Tree
import org.scalamock.MockFactoryBase

trait TestExpectations extends WrappedTests {
  this: Runner with MockFactoryBase =>

  override protected def wrapTest[A](tree: => Tree[A]): Tree[A] =
    withExpectations(tree)
}
