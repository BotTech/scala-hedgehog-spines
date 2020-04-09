package com.lightbend.hedgehog.implicits

import com.lightbend.hedgehog.implicits.OptionImplicits.OptionSyntax
import com.lightbend.hedgehog.implicits.TreeImplicits.TreeGenResultSyntax
import hedgehog.Result
import hedgehog.core.{Seed, Tree}

// TODO: Delete this.
trait TreeImplicits {

  implicit def treeGenResultSyntax[A](tree: Tree[(Seed, Option[A])]): TreeGenResultSyntax[A] =
    new TreeGenResultSyntax(tree)
}

object TreeImplicits {

  implicit class TreeGenResultSyntax[A](private val tree: Tree[(Seed, Option[A])]) extends AnyVal {

    def generated(f: A => Result): Result = {
      val (_, value) = tree.value
      value.isSome(f)
    }
  }
}
