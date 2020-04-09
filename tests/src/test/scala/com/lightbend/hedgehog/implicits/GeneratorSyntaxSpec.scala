package com.lightbend.hedgehog.implicits

import com.lightbend.hedgehog.Logs
import com.lightbend.hedgehog.Results._
import com.lightbend.hedgehog.generators.core.SeedGenerators._
import com.lightbend.hedgehog.generators.core.SizeGenerators._
import com.lightbend.hedgehog.runner.TestRunner
import hedgehog._
import hedgehog.core.{GenT, Tree}
import hedgehog.predef.{Identity, LazyList}
import hedgehog.runner.{property, Test}

object GeneratorSyntaxSpec extends TestRunner with Logs with GeneratorImplicits {

  override def tests: List[Test] = List(
    property("generates passes if tree value passes", propGeneratesPassesIfValuePasses),
    property("generates fails if tree value fails", propGeneratesFailsIfValueFails),
    property("generates fails if tree has no value", propGeneratesFailsIfNoValue)
  )

  private def propGeneratesPassesIfValuePasses: Property = forAll {
    for {
      size <- genSize
      seed <- genSeed
    } yield {
      val gen = generator(Some(true))
      gen.generates(size, seed)(_ ==== true)
    }
  }

  private def propGeneratesFailsIfValueFails: Property = forAll {
    for {
      size <- genSize
      seed <- genSeed
    } yield {
      val gen = generator(Some(false))
      not(gen.generates(size, seed)(_ ==== true), "Expected failure")
    }
  }

  private def propGeneratesFailsIfNoValue: Property = forAll {
    for {
      size <- genSize
      seed <- genSeed
    } yield {
      val gen = generator[Boolean](None)
      not(gen.generates(size, seed)(_ ==== true), "Expected failure")
    }
  }

  private def generator[A](opt: Option[A]): Gen[A] =
    GenT { (_, seed) =>
      Tree((seed, opt), Identity(LazyList()))
    }
}
