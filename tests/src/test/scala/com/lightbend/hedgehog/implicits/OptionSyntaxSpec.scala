package com.lightbend.hedgehog.implicits

import com.lightbend.hedgehog.Logs
import com.lightbend.hedgehog.generators.IntGenerators.genInt
import com.lightbend.hedgehog.generators.OptionGenerators._
import com.lightbend.hedgehog.generators.core.ResultGenerators._
import com.lightbend.hedgehog.runner.TestRunner
import hedgehog._
import hedgehog.runner.{property, Test}

object OptionSyntaxSpec extends TestRunner with Logs with OptionImplicits {

  override def tests: List[Test] = List(
    property("isSome of none fails", propIsSomeOfNoneFails),
    property("isSome of some returns result of f", propIsSomeOfSomeIsResult)
  )

  private def propIsSomeOfNoneFails: Property = forAll {
    for {
      none   <- genNone
      result <- genResult
    } yield none.isSome((_: Any) => result) ==== Result.failure.log("=== None ===").log(None.toString)
  }

  private def propIsSomeOfSomeIsResult: Property = forAll {
    for {
      opt    <- genSome(genInt)
      result <- genResult
    } yield opt.isSome(_ => result) ==== result
  }
}
