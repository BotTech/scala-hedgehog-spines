package com.lightbend.hedgehog.sbt

import java.time.Instant
import java.time.temporal.ChronoUnit

import com.lightbend.hedgehog.runner.Manager
import hedgehog.core.Report
import hedgehog.runner.Test
import hedgehog.sbt.Event
import sbt.testing.{EventHandler, Logger, TaskDef, TestSelector}

class TimedManager(taskDef: TaskDef, eventHandler: EventHandler, loggers: Array[Logger]) extends Manager[Instant] {

  override def prepare(test: Test): (Test, Instant) = (test, Instant.now())

  override def log(message: String): Unit = loggers.foreach(_.info(message))

  override def publish(className: String, test: Test, report: Report, start: Instant): Unit = {
    val end      = Instant.now()
    val duration = start.until(end, ChronoUnit.MILLIS)
    eventHandler.handle(Event.fromReport(taskDef, new TestSelector(test.name), report, duration))
    loggers.foreach { logger =>
      logger.info(Test.renderReport(className, test, report, logger.ansiCodesSupported))
    }
  }
}
