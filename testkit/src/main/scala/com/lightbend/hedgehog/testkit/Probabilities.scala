package com.lightbend.hedgehog.testkit

import hedgehog.core.{CoverPercentage, SuccessCount}
import hedgehog.runner.Test
import hedgehog.{Gen, Property}

/**
  * These are values for binomial confidence intervals using a 99.99999% confidence level that were computed using this
  * <a href="https://statpages.info/confint.html">calculator</a>.
  */
trait Probabilities {

  val TestCount: SuccessCount = 10000

  val OneToOne: CoverPercentage = 47.33

  val OneToTwo: CoverPercentage = 30.85
  val TwoToOne: CoverPercentage = 64.11

  val OneToThree: CoverPercentage = 22.74
  val ThreeToOne: CoverPercentage = 72.64

  val TwoToThree: CoverPercentage = 37.41
  val ThreeToTwo: CoverPercentage = 57.37

  val TwentyFiveToOne: CoverPercentage = 2.91

  // These are oddly specific but that is what the percentages are for Options.
  val TwoToOneHundredAndOne: CoverPercentage = 1.29
  val OneHundredAndOneToTwo: CoverPercentage = 97.21

  val OneToTenThousand: CoverPercentage = 99.80

  val BirthdayDays: Short              = 365
  val BirthdayPeople: Short            = 23
  val BirthdayMatch: CoverPercentage   = 47.38
  val BirthdayNoMatch: CoverPercentage = 47.28

  implicit class GeneratorProbabilityTests[A](tests: GeneratorTests[A]) {

    def addProbabilities(name: String => String, property: Property): GeneratorTests[A] =
      addProbabilitiesWithConfig(name, property, identity)

    def addProbabilitiesWithConfig(
        name: String => String,
        property: Property,
        configure: Test => Test
      ): GeneratorTests[A] =
      tests.addPropWithConfig(name, property, t => configure(t.withTests(TestCount)))

    def addGenProbabilities(name: String => String, property: Gen[A] => Property): GeneratorTests[A] =
      addGenProbabilitiesWithConfig(name, property, identity)

    def addGenProbabilitiesWithConfig(
        name: String => String,
        property: Gen[A] => Property,
        configure: Test => Test
      ): GeneratorTests[A] =
      tests.addGenPropWithConfig(name, property, t => configure(t.withTests(TestCount)))
  }
}
