package com.lightbend.hedgehog.runner

import hedgehog.core.{PropertyConfig, Report, Seed}
import hedgehog.runner.{Properties, Test}
import hedgehog.{Gen, Property, Result}
import org.scalactic.TripleEquals._
import scopt.OParser

// This has to extend Properties otherwise it doesn't work with the sbt test framework.
trait TestRunnerLike extends Properties with Runner {

  // scalastyle:off regex

  override def main(args: Array[String]): Unit =
    OParser.parse(optionParser, args, Config.default) match {
      case Some(config) =>
        val tests = filteredTests(config)
        if (tests.isEmpty) {
          println("No tests to run.")
        } else {
          val className = this.getClass.getName
          runTests(className, tests, config.propertyConfig, config.seed, SimpleManager)
        }
      case _ =>
        println()
        sys.error("Failed to parse test arguments.")
    }

  private def optionParser: OParser[_, Config] = {
    val builder = OParser.builder[Config]
    import builder._
    OParser.sequence(
      programName(this.getClass.getSimpleName.replaceAll("""\$$""", "")),
      opt[String]('n', "name")
        .action((name, config) => config.copy(testNames = name :: config.testNames))
        .text("Only runs tests with the given name. Specify multiple times to run multiple tests.")
        .unbounded(),
      opt[Long]('s', "seed")
        .action((seed, config) => config.copy(seed = seed))
        .text("Sets the seed value to use."),
      help('h', "help")
    )
  }

  // scalastyle:on regex

  private def filteredTests(config: Config): List[Test] =
    config.testNames match {
      case Nil => tests
      case names =>
        for {
          test <- tests
          name <- names
          if test.name === name
        } yield test
    }

  override def runTests[A](
      className: String,
      tests: List[Test],
      config: PropertyConfig,
      seed: Long,
      manager: Manager[A]
    ): Unit = {
    manager.log(s"Running tests (seed = ${seed.toString}):")
    tests.foreach { test =>
      val (prepared, context) = manager.prepare(test)
      val report              = runTest(prepared, config, seed)
      manager.publish(className, test, report, context)
    }
  }

  override def runTest(test: Test, config: PropertyConfig, seed: Long): Report =
    // TODO: Use a different seed for each test.
    Property.check(test.withConfig(config), test.result, Seed.fromLong(seed))

  protected def forAll(gen: Gen[Result]): Property = gen.forAll
}
