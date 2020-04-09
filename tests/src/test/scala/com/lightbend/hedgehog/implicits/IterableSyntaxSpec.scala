package com.lightbend.hedgehog.implicits

import com.lightbend.hedgehog.Logs
import com.lightbend.hedgehog.Results._
import com.lightbend.hedgehog.generators.CollectionGenerators._
import com.lightbend.hedgehog.runner.TestRunner
import hedgehog._
import hedgehog.runner.{property, Test}

object IterableSyntaxSpec extends TestRunner with Logs with CollectionImplicits {

  override def tests: List[Test] = List(
    property("forAll passes if all elements pass", propForAllPassesIfElementsPass),
    property("forAll fails if any element fails", propForAllFailsIfAnyElementFails)
  )

  private def propForAllPassesIfElementsPass: Property = forAll {
    for {
      elements <- Gen.list(Gen.constant(true), Range.linear(0, 10))
    } yield elements.forAll(_ ==== true)
  }

  private def propForAllFailsIfAnyElementFails: Property = forAll {
    for {
      size     <- Gen.int(Range.linear(1, 10))
      falses   <- Gen.list(Gen.constant(false), Range.singleton(size))
      trues    <- Gen.list(Gen.constant(true), Range.singleton(10 - size))
      elements <- genShuffled(Gen.constant(falses ::: trues))
    } yield not(elements.forAll(_ ==== true), "Expected failure")
  }
}
