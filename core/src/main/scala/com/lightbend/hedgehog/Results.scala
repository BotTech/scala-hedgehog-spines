package com.lightbend.hedgehog

import hedgehog.Result

object Results {

  def not(result: Result, info: String): Result = result match {
    case Result.Success    => Result.failure.log(info)
    case _: Result.Failure => Result.success
  }
}
