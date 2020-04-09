package com.lightbend.hedgehog.implicits

import com.lightbend.hedgehog.Logs
import com.lightbend.hedgehog.Results._
import com.lightbend.hedgehog.generators.core.SeedGenerators._
import com.lightbend.hedgehog.runner.TestRunner
import hedgehog._
import hedgehog.core.{Seed, Tree}
import hedgehog.runner.{property, Test}

object TreeGenResultSyntaxSpec extends TestRunner with Logs with TreeImplicits {

  override def tests: List[Test] = List(
    property("generated passes if tree value passes", propGeneratedPassesIfValuePasses),
    property("generated fails if tree value fails", propGeneratedFailsIfValueFails),
    property("generated fails if tree has no value", propGeneratedFailsIfNoValue)
  )

  private def propGeneratedPassesIfValuePasses: Property = forAll {
    for {
      seed <- genSeed
    } yield {
      val tree: Tree[(Seed, Option[Boolean])] = Tree.TreeApplicative.point((seed, Some(true)))
      tree.generated(_ ==== true)
    }
  }

  private def propGeneratedFailsIfValueFails: Property = forAll {
    for {
      seed <- genSeed
    } yield {
      val tree: Tree[(Seed, Option[Boolean])] = Tree.TreeApplicative.point((seed, Some(false)))
      not(tree.generated(_ ==== true), "Expected failure")
    }
  }

  private def propGeneratedFailsIfNoValue: Property = forAll {
    for {
      seed <- genSeed
    } yield {
      val tree: Tree[(Seed, Option[Boolean])] = Tree.TreeApplicative.point((seed, None))
      not(tree.generated(_ ==== true), "Expected failure")
    }
  }
}
