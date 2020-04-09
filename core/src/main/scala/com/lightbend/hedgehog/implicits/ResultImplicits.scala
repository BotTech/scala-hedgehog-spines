package com.lightbend.hedgehog.implicits

import com.lightbend.hedgehog.implicits.ResultImplicits.ResultSyntax
import hedgehog.Result

trait ResultImplicits {

  implicit def resultSyntax(result: Result): ResultSyntax = new ResultSyntax(result)
}

object ResultImplicits {

  implicit class ResultSyntax(private val result: Result) extends AnyVal {

    def prependLogs(message: String, messages: String*): Result =
      (message :: messages.toList).foldRight(result) {
        case (message, result) => result.prependLog(message)
      }

    def prependLog(message: String): Result = result match {
      case Result.Success      => Result.Success
      case Result.Failure(log) => Result.Failure(message :: log)
    }
  }
}
