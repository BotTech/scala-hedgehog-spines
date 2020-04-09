package com.lightbend.hedgehog.scalamock

import com.lightbend.hedgehog.runner.TestRunnerLike
import hedgehog.core._
import hedgehog.runner.{Properties, Test}

trait TestRunnerMockLike extends TestRunnerLike with AbstractMockFactory with TestExpectations {
  this: Properties =>

  override def runTest(test: Test, config: PropertyConfig, seed: Long): Report =
    enhanceReport(super.runTest(test, config, seed))

  private def enhanceReport(report: Report): Report =
    report.status match {
      case Failed(shrinks, log) =>
        val enhancedLog = log.flatMap {
          case error @ Error(e: MatchError) if errorInHedgehog(e) => List[Log](matchErrorInfo, error)
          case other                                              => List(other)
        }
        report.copy(status = Failed(shrinks, enhancedLog))
      case _ => report
    }

  private def errorInHedgehog(e: Throwable): Boolean =
    e.getStackTrace.headOption.exists(_.getClassName.startsWith("hedgehog."))

  private def matchErrorInfo: Info =
    Info(
      """A MatchError occurred running the test. This is often caused by mocks/stubs returning null.
        |  Check the stack trace to see what method was called and debug it to ensure the arguments are what were expected.
        |  It may also be from reusing the same mock/stub between tests.
        |  Ensure that any mocks are recreated for each test.""".stripMargin
    )
}
