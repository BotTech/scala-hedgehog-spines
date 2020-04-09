package com.lightbend.hedgehog.runner

import hedgehog.core.{GenT, PropertyConfig, PropertyT, Report, Tree}
import hedgehog.predef.LazyList
import hedgehog.runner.Test

trait WrappedTests extends Runner {

  abstract override def runTest(test: Test, config: PropertyConfig, seed: Long): Report = {
    val property = PropertyT {
      GenT { (size, seed) =>
        wrapTree(test.result.run.run(size, seed))
      }
    }
    val wrappedTest = new Test(test.name, test.withConfig, property)
    super.runTest(wrappedTest, config, seed)
  }

  private def wrapTree[A](tree: => Tree[A]): Tree[A] =
    wrapTest {
      tree.copy(children = tree.children.map(wrapChildren))
    }

  private def wrapChildren[A](children: LazyList[Tree[A]]): LazyList[Tree[A]] =
    children match {
      case LazyList.Cons(head, tail) => LazyList.cons(wrapTree(head()), wrapChildren(tail()))
      case LazyList.Nil()            => LazyList.nil
    }

  protected def wrapTest[A](tree: => Tree[A]): Tree[A]
}
