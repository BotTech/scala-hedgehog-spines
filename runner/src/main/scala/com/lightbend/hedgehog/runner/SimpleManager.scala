package com.lightbend.hedgehog.runner
import hedgehog.core.Report
import hedgehog.runner.Test

object SimpleManager extends Manager[Unit] {

  override def prepare(test: Test): (Test, Unit) = (test, ())

  // scalastyle:off regex

  override def log(message: String): Unit = println(message)

  // scalastyle:on regex

  override def publish(className: String, test: Test, report: Report, context: Unit): Unit =
    log(Test.renderReport(className, test, report, ansiCodesSupported = true))
}
