package com.lightbend.hedgehog.runner

import hedgehog.core.{Coverage, DiscardCount, PropertyConfig, Report, Status, SuccessCount}
import hedgehog.runner.Test

trait Runner {

  // TODO: Ideally these would take a Seed but we can't get a sane long value back out to report on.

  def runTests[A](
      className: String,
      tests: List[Test],
      config: PropertyConfig,
      seed: Long,
      manager: Manager[A]
    ): Unit = ()

  def runTest(test: Test, config: PropertyConfig, seed: Long): Report =
    Report(SuccessCount(0), DiscardCount(0), Coverage.empty, Status.ok)
}
