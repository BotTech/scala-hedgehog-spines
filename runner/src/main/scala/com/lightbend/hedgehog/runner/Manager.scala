package com.lightbend.hedgehog.runner

import hedgehog.core.Report
import hedgehog.runner.Test

trait Manager[A] {

  def prepare(test: Test): (Test, A)

  def log(message: String): Unit

  def publish(className: String, test: Test, report: Report, context: A): Unit

}
