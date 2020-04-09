package com.lightbend.hedgehog.generators

import com.lightbend.hedgehog.generators.LogGenerators._
import com.lightbend.hedgehog.scalamock.{GeneratorSpec, TestRunnerMock}
import com.lightbend.hedgehog.testkit.Probabilities
import hedgehog._
import hedgehog.core.{Error, ForAll, Info, Log}
import hedgehog.runner.Test

object LogGeneratorsSpec extends TestRunnerMock with GeneratorSpec with Probabilities {

  override def tests: List[Test] =
    test("genName", genName).tests ++
      test("genForAll", genForAll).tests ++
      test("genInfo", genInfo).tests ++
      test("genError", genError).tests ++
      test("genLog", genLog).addGenProbabilities(_ + " generates logs with probabilities", propProbabilities).tests

  @SuppressWarnings(Array("scalafix:DisableSyntax.isInstanceOf"))
  private def propProbabilities(gen: Gen[Log]): Property =
    gen.forAll
      .cover(OneToTwo, "ForAll", _.isInstanceOf[ForAll])
      .cover(OneToTwo, "Info", _.isInstanceOf[Info])
      .cover(OneToTwo, "Error", _.isInstanceOf[Error])
      .map {
        // Get the compiler to tell us if we are not generating a case.
        case _: ForAll | _: Info | _: Error => Result.success
      }
}
