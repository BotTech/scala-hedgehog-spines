package com.lightbend.hedgehog.implicits

import com.lightbend.hedgehog.Logs
import com.lightbend.hedgehog.Results._
import com.lightbend.hedgehog.generators.IntGenerators._
import com.lightbend.hedgehog.runner.TestRunner
import hedgehog._
import hedgehog.runner.{example, property, Test}
import org.scalactic.TripleEquals._

// scalastyle:off number.of.methods

object ExtendedSyntaxSpec extends TestRunner with Logs with ArbitraryImplicits {

  override def tests: List[Test] = List(
    property("!=== passes if not equal", propNotEqualsPassesIfNotEqual),
    property("!=== fails if equal", propNotEqualsFailsIfEqual),
    example("asA passes if condition passes", testAsAPassesIfConditionPasses),
    example("asA fails if condition fails", testAsAFailsIfConditionFails),
    example("asA fails if not an instance of", testAsAFailsIfNotInstanceOf),
    example("isA passes if same type", testIsAPassesIfSameType),
    example("isA fails if not same type", testIsAFailsIfNotSameType),
    property("lessThan passes if less than", propLessThanPassesIfLessThan),
    property("lessThan fails if equal", propLessThanFailsIfEqual),
    property("lessThan fails if greater than", propLessThanFailsIfGreaterThan),
    property("lessThanOrEqual passes if less than", propLessThanOrEqualPassesIfLessThan),
    property("lessThanOrEqual passes if equal", propLessThanOrEqualPassesIfEqual),
    property("lessThanOrEqual fails if greater than", propLessThanOrEqualFailsIfGreaterThan),
    property("greaterThan passes if greater than", propGreaterThanPassesIfGreaterThan),
    property("greaterThan fails if equal", propGreaterThanFailsIfEqual),
    property("greaterThan fails if less than", propGreaterThanFailsIfLessThan),
    property("greaterThanOrEqual passes if greater than", propGreaterThanOrEqualPassesIfGreaterThan),
    property("greaterThanOrEqual passes if equal", propGreaterThanOrEqualPassesIfEqual),
    property("greaterThanOrEqual fails if less than", propGreaterThanOrEqualFailsIfLessThan),
    property("within passes if between (exclusive)", propWithinPassesIfBetween),
    property("within fails if equal", propWithinFailsIfEqual),
    property("within fails if not between (exclusive)", propWithinFailsIfNotBetween),
    property("without fails if between (exclusive)", propWithoutFailsIfBetween),
    property("without fails if equal", propWithoutFailsIfEqual),
    property("without passes if not between (exclusive)", propWithoutPassesIfNotBetween),
    property("inside passes if between (inclusive)", propInsidePassesIfBetween),
    property("inside passes if equal", propInsidePassesIfEqual),
    property("inside fails if not between (inclusive)", propInsideFailsIfNotBetween),
    property("outside fails if between (inclusive)", propOutsideFailsIfBetween),
    property("outside passes if equal", propOutsidePassesIfEqual),
    property("outside passes if not between (inclusive)", propOutsidePassesIfNotBetween)
  )

  private def propNotEqualsPassesIfNotEqual: Property = forAll {
    for {
      a <- genInt
      b <- genInt
      if a !== b
    } yield a !=== b
  }

  private def propNotEqualsFailsIfEqual: Property = forAll {
    for {
      a <- genInt
    } yield not(a !=== a, "Expected failure")
  }

  private def testAsAPassesIfConditionPasses: Result = None.asA[Option[Boolean]](_ => Result.success)

  private def testAsAFailsIfConditionFails: Result =
    not(None.asA[Option[Boolean]](_ => Result.failure), "Expected failure")

  private def testAsAFailsIfNotInstanceOf: Result =
    not(None.asA[Some[Boolean]](_ => Result.success), "Expected failure")

  private def testIsAPassesIfSameType: Result = 123.isA[Integer]

  private def testIsAFailsIfNotSameType: Result = not(123.isA[Boolean], "Expected failure")

  private def propLessThanPassesIfLessThan: Property = forAll {
    for {
      (a, b) <- genOrderedStrict2
    } yield a.lessThan(b)
  }

  private def propLessThanFailsIfEqual: Property = forAll {
    for {
      a <- genInt
    } yield not(a.lessThan(a), "Expected failure")
  }

  private def propLessThanFailsIfGreaterThan: Property = forAll {
    for {
      (a, b) <- genOrderedStrict2
    } yield not(b.lessThan(a), "Expected failure")
  }

  private def propLessThanOrEqualPassesIfLessThan: Property = forAll {
    for {
      (a, b) <- genOrderedStrict2
    } yield a.lessThanOrEqual(b)
  }

  private def propLessThanOrEqualPassesIfEqual: Property = forAll {
    for {
      a <- genInt
    } yield a.lessThanOrEqual(a)
  }

  private def propLessThanOrEqualFailsIfGreaterThan: Property = forAll {
    for {
      (a, b) <- genOrderedStrict2
    } yield not(b.lessThanOrEqual(a), "Expected failure")
  }

  private def propGreaterThanPassesIfGreaterThan: Property = forAll {
    for {
      (a, b) <- genOrderedStrict2
    } yield b.greaterThan(a)
  }

  private def propGreaterThanFailsIfEqual: Property = forAll {
    for {
      a <- genInt
    } yield not(a.greaterThan(a), "Expected failure")
  }

  private def propGreaterThanFailsIfLessThan: Property = forAll {
    for {
      (a, b) <- genOrderedStrict2
    } yield not(a.greaterThan(b), "Expected failure")
  }

  private def propGreaterThanOrEqualPassesIfGreaterThan: Property = forAll {
    for {
      (a, b) <- genOrderedStrict2
    } yield b.greaterThanOrEqual(a)
  }

  private def propGreaterThanOrEqualPassesIfEqual: Property = forAll {
    for {
      a <- genInt
    } yield a.greaterThanOrEqual(a)
  }

  private def propGreaterThanOrEqualFailsIfLessThan: Property = forAll {
    for {
      (a, b) <- genOrderedStrict2
    } yield not(a.greaterThanOrEqual(b), "Expected failure")
  }

  private def propWithinPassesIfBetween: Property = forAll {
    for {
      (a, b, c) <- genOrderedStrict3
    } yield b.within(a, c).and(b.within(c, a))
  }

  private def propWithinFailsIfEqual: Property = forAll {
    for {
      a <- genInt
      b <- genInt
    } yield not(a.within(a, b), "Expected failure").or(a.within(b, a))
  }

  private def propWithinFailsIfNotBetween: Property = forAll {
    for {
      (a, b, c) <- genOrderedStrict3
    } yield not(
      Result.all(
        List(
          a.within(b, c),
          a.within(c, b),
          c.within(a, b),
          c.within(b, a)
        )
      ),
      "Expected failure"
    )
  }

  private def propWithoutFailsIfBetween: Property = forAll {
    for {
      (a, b, c) <- genOrderedStrict3
    } yield not(b.without(a, c).or(b.without(c, a)), "Expected failure")
  }

  private def propWithoutFailsIfEqual: Property = forAll {
    for {
      a <- genInt
      b <- genInt
    } yield not(a.without(a, b), "Expected failure").or(a.without(b, a))
  }

  private def propWithoutPassesIfNotBetween: Property = forAll {
    for {
      (a, b, c) <- genOrderedStrict3
    } yield Result.all(
      List(
        a.without(b, c),
        a.without(c, b),
        c.without(a, b),
        c.without(b, a)
      )
    )
  }

  private def propInsidePassesIfBetween: Property = forAll {
    for {
      (a, b, c) <- genOrderedStrict3
    } yield b.inside(a, c).and(b.inside(c, a))
  }

  private def propInsidePassesIfEqual: Property = forAll {
    for {
      a <- genInt
      b <- genInt
    } yield a.inside(a, b).and(a.inside(b, a))
  }

  private def propInsideFailsIfNotBetween: Property = forAll {
    for {
      (a, b, c) <- genOrderedStrict3
    } yield not(
      Result.all(
        List(
          a.inside(b, c),
          a.inside(c, b),
          c.inside(a, b),
          c.inside(b, a)
        )
      ),
      "Expected failure"
    )
  }

  private def propOutsideFailsIfBetween: Property = forAll {
    for {
      (a, b, c) <- genOrderedStrict3
    } yield not(b.outside(a, c).or(b.outside(c, a)), "Expected failure")
  }

  private def propOutsidePassesIfEqual: Property = forAll {
    for {
      a <- genInt
      b <- genInt
    } yield a.outside(a, b).and(a.outside(b, a))
  }

  private def propOutsidePassesIfNotBetween: Property = forAll {
    for {
      (a, b, c) <- genOrderedStrict3
    } yield Result.all(
      List(
        a.outside(b, c),
        a.outside(c, b),
        c.outside(a, b),
        c.outside(b, a)
      )
    )
  }

  // TODO: Make a generic version of these.
  private def genOrderedStrict2: Gen[(Int, Int)] =
    for {
      a <- Gen.int(Range.linearFrom(0, Int.MinValue, Int.MaxValue - 1))
      b <- Gen.int(Range.linear(a, Int.MaxValue))
    } yield (a, b)

  private def genOrderedStrict3: Gen[(Int, Int, Int)] =
    for {
      a <- Gen.int(Range.linearFrom(0, Int.MinValue, Int.MaxValue - 2))
      b <- Gen.int(Range.linear(a, Int.MaxValue - 1))
      c <- Gen.int(Range.linear(b, Int.MaxValue))
    } yield (a, b, c)
}
