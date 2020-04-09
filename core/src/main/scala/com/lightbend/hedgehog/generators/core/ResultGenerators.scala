package com.lightbend.hedgehog.generators.core

import com.lightbend.hedgehog.generators.LogGenerators.genLog
import hedgehog.core.Result.{Failure, Success}
import hedgehog.{Gen, Range, Result}

object ResultGenerators {

  private val failureLogsStepSize = 1

  def genSuccessfulResult: Gen[Success.type] = Gen.constant(Success)

  def genFailedResult: Gen[Failure] = Gen.list(genLog, Range.linear(0, failureLogsStepSize)).map(Failure)

  def genResult: Gen[Result] = Gen.choice1(genSuccessfulResult.map(identity), genFailedResult.map(identity))
}
