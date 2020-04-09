package com.lightbend.hedgehog.implicits

import com.lightbend.hedgehog.Logs
import com.lightbend.hedgehog.generators.core.ResultGenerators._
import com.lightbend.hedgehog.generators.StringGenerators._
import com.lightbend.hedgehog.runner.TestRunner
import hedgehog._
import hedgehog.core.{Info, Log}
import hedgehog.runner.{property, Test}

object ResultSyntaxSpec extends TestRunner with Logs with ResultImplicits {

  override def tests: List[Test] = List(
    property("prependLog to result adds to the front", propPrependLogResultAddsToFront),
    property("prependLogs to result adds to the front", propPrependLogsResultAddsToFront)
  )

  private def propPrependLogResultAddsToFront: Property = forAll {
    for {
      result  <- genResult
      message <- genAnyUnicodeString
    } yield {
      val prepended = result.prependLog(message)
      testLogs(result, prepended, Info(message) :: Nil)
    }
  }

  private def propPrependLogsResultAddsToFront: Property = forAll {
    for {
      result   <- genResult
      message  <- genAnyUnicodeString
      messages <- Gen.list(genAnyUnicodeString, Range.linear(0, 4))
    } yield {
      val prepended = result.prependLogs(message, messages: _*)
      testLogs(result, prepended, Info(message) :: messages.map(Info))
    }
  }

  private def testLogs(original: Result, prepended: Result, log: List[Log]): Result =
    original match {
      case Result.Success => prepended ==== Result.success
      case Result.Failure(originalLog) =>
        prepended match {
          case Result.Success               => Result.failure.log("Expected a failure")
          case Result.Failure(prependedLog) => prependedLog ==== log ::: originalLog
        }
    }
}
