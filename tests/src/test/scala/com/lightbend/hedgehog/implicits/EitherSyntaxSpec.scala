package com.lightbend.hedgehog.implicits

import com.lightbend.hedgehog.generators.EitherGenerators._
import com.lightbend.hedgehog.generators.IntGenerators.genInt
import com.lightbend.hedgehog.generators.core.ResultGenerators._
import com.lightbend.hedgehog.runner.TestRunner
import hedgehog.runner.{property, Test}
import hedgehog.{Property, Result}

object EitherSyntaxSpec extends TestRunner with EitherImplicits {

  override def tests: List[Test] = List(
    property("rightResult of left is a failure", propRightResultOfLeftIsAFailure),
    property("rightResult of right is result of f", propRightResultOfLeftIsResult),
    property("leftResult of right is a failure", propLeftResultOfRightIsAFailure),
    property("leftResult of left is result of f", propLeftResultOfLeftIsResult)
  )

  private def propRightResultOfLeftIsAFailure: Property =
    for {
      left <- genLeft(genInt).forAll
    } yield {
      val result = left.rightResult((_: Any) => Result.success)
      result ==== Result.failure.log(s"${left.toString} is not Right")
    }

  private def propRightResultOfLeftIsResult: Property =
    for {
      right  <- genRight(genInt).forAll
      result <- genResult.forAll
    } yield right.rightResult(_ => result) ==== result

  private def propLeftResultOfRightIsAFailure: Property =
    for {
      right <- genRight(genInt).forAll
    } yield {
      val result = right.leftResult((_: Any) => Result.failure)
      result ==== Result.failure.log(s"${right.toString} is not Left")
    }

  private def propLeftResultOfLeftIsResult: Property =
    for {
      left   <- genLeft(genInt).forAll
      result <- genResult.forAll
    } yield left.leftResult(_ => result) ==== result
}
