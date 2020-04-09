package com.lightbend.hedgehog.generators

import com.lightbend.hedgehog.generators.ExceptionGenerators._
import com.lightbend.hedgehog.scalamock.{GeneratorSpec, TestRunnerMock}
import com.lightbend.hedgehog.testkit.Probabilities
import hedgehog._
import hedgehog.runner.Test
import org.scalactic.TripleEquals._

// scalastyle:off null

@SuppressWarnings(Array("scalafix:DisableSyntax.null"))
object ExceptionGeneratorsSpec extends TestRunnerMock with GeneratorSpec with Probabilities {

  override def tests: List[Test] =
    test("genException", genException)
      .addProbabilities(_ + " generates exception with probabilities", propGenExceptionProbabilities)
      .addProbabilities(
        _ + " generates exception with probabilities with size 5",
        propGenExceptionProbabilitiesSizeFive
      )
      .tests

  private def propGenExceptionProbabilities: Property =
    genException
      .resize(MinSize)
      .map { e =>
        e.addSuppressed(new NullPointerException())
        e
      }
      .forAll
      .cover(OneToOne, "message not null", _.getMessage !== null)
      .cover(OneToOne, "message null", _.getMessage === null)
      .cover(OneToTwo, "cause not null", _.getCause !== null)
      .cover(TwoToOne, "cause null", _.getCause === null)
      .cover(OneToOne, "suppression enabled", _.getSuppressed.nonEmpty)
      .cover(OneToOne, "suppression disabled", _.getSuppressed.isEmpty)
      .cover(OneToOne, "stack trace is writable", _.getStackTrace.nonEmpty)
      .cover(OneToOne, "stack trace is not writable", _.getStackTrace.isEmpty)
      .map(_ => Result.success)

  private def propGenExceptionProbabilitiesSizeFive: Property =
    genException
      .resize(Size(5))
      .map { e =>
        e.addSuppressed(new NullPointerException())
        e
      }
      .forAll
      .cover(ThreeToOne, "message not null", _.getMessage !== null)
      .cover(OneToThree, "message null", _.getMessage === null)
      .cover(ThreeToTwo, "cause not null", _.getCause !== null)
      .cover(TwoToThree, "cause null", _.getCause === null)
      .cover(OneToOne, "suppression enabled", _.getSuppressed.nonEmpty)
      .cover(OneToOne, "suppression disabled", _.getSuppressed.isEmpty)
      .cover(OneToOne, "stack trace is writable", _.getStackTrace.nonEmpty)
      .cover(OneToOne, "stack trace is not writable", _.getStackTrace.isEmpty)
      .map(_ => Result.success)
}
