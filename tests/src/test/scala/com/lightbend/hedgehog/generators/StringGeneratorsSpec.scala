package com.lightbend.hedgehog.generators

import com.lightbend.hedgehog.Results._
import com.lightbend.hedgehog.generators.CharGenerators._
import com.lightbend.hedgehog.generators.StringGenerators._
import com.lightbend.hedgehog.generators.core.RangeGenerators._
import com.lightbend.hedgehog.implicits.ArbitraryImplicits._
import com.lightbend.hedgehog.implicits.CollectionImplicits._
import com.lightbend.hedgehog.implicits.GeneratorImplicits._
import com.lightbend.hedgehog.implicits.RangeImplicits._
import com.lightbend.hedgehog.scalamock.{GeneratorSpec, TestRunnerMock}
import com.lightbend.hedgehog.testkit.GeneratorTests._
import hedgehog._
import hedgehog.core.Result.Success
import hedgehog.core.Seed
import hedgehog.runner.Test

object StringGeneratorsSpec extends TestRunnerMock with GeneratorSpec {

  private val MaxCharsToConcat = 5

  // TODO: These tests are a bit of a Frankenstein monster. The test syntax needs an overhaul.

  override def tests: List[Test] =
    genStringTests("genString", genStringOfAnyChars, "within the range", propGenStringCharsInRange, 0) ++
      genConcatStringTests ++
      genTests("genUnicodeString", genUnicodeString, "all unicode", testAllUnicode, 0) ++
      test("genAnyUnicodeString", genAnyUnicodeString).tests ++
      test("genAnyNonEmptyUnicodeString", genAnyNonEmptyUnicodeString)
        .addGenProp(_ + "generates non empty strings", _.forAll.map(s => Result.assert(s.nonEmpty)))
        .tests ++
      genTests("genWhitespaceString", genWhitespaceString, "all whitespace", testAllWhitespace, 0) ++
      genTests("genBlankString", genBlankString, "blank", testAllBlank, 0) ++
      genTests("genNonWhitespaceString", genNonWhitespaceString, "not all whitespace", testNotAllWhitespace, 0) ++
      genTests("genNonBlankString", genNonBlankString, "not all blank", testNotAllBlank, 1) ++
      genTests("genNotTrimmedString", genNotTrimmedString, "sensible", testNotTrimmed, 1) ++
      genTests("genTrimmedString", genTrimmedString, "trimmed", s => s ==== s.trim, 0) ++
      genTests("genNonsenseString", genNonsenseString, "nonsense", s => (s ==== "").or(testNotTrimmed(s)), 0) ++
      genTests("genSensibleString", genSensibleString, "sensible", s => (s !=== "").and(s ==== s.trim), 1)

  private def genTests(
      name: String,
      gen: Range[Int] => Gen[String],
      description: String,
      result: String => Result,
      min: Int
    ): List[Test] =
    genStringTests(name, gen, description, propString(gen, result), min)

  private def genStringTests(
      name: String,
      gen: Range[Int] => Gen[String],
      description: String,
      property: Property,
      min: Int
    ): List[Test] =
    test(name, genStringRange.flatMap(gen))
      .addProp(_ + " generates strings that are " + description, property)
      .addProp(_ + " generates strings with length of lower bound as minimum", propGenMin(gen, min))
      .addProp(_ + " generates strings with length of upper bound as maximum", propGenMax(gen, min))
      .tests

  private def genConcatStringTests: List[Test] = {
    val gen = genConcatenationOfAnyChars _
    val min = 0
    test("genConcatenatedString", genStringRange.flatMap(gen))
      .addProp(_ + " generates strings that are not truncated from the original", propGenConcatStringNoTruncation)
      .addProp(_ + " generates strings with length of lower bound as minimum", propGenMin(gen, min))
      .addProp(
        _ + " generates strings with length of upper bound as maximum",
        propGenMaxResult(gen, min, {
          // We can get additional chars from the final join.
          case (size, max) => size.inside(max, max + MaxCharsToConcat - 1)
        })
      )
      .tests
  }

  private def genStringOfAnyChars(range: Range[Int]): Gen[String] =
    for {
      charGen <- genGenChar
      s       <- genString(charGen, range)
    } yield s

  private def genConcatenationOfAnyChars(range: Range[Int]): Gen[String] =
    for {
      charGen <- genGenChar
      s       <- Gen.string(charGen, Range.constant(1, MaxCharsToConcat))
      cs      <- genConcatenationOf(s, range)
    } yield cs

  private def genConcatenationOf(s: String, range: Range[Int]): Gen[String] =
    genConcatenatedString(Gen.constant(s), range)

  private def propGenStringCharsInRange: Property = forAll {
    for {
      lo    <- genChar
      hi    <- Gen.char(lo, Char.MaxValue)
      range <- genStringRange
      s     <- genString(Gen.char(lo, hi), range)
    } yield s.toSeq.forAll(_.inside(lo, hi))
  }

  private def propGenConcatStringNoTruncation: Property = forAll {
    for {
      charGen <- genGenChar
      s       <- Gen.string(charGen, Range.constant(1, MaxCharsToConcat))
      range   <- genStringRange
      cs      <- genConcatenationOf(s, range)
    } yield cs.grouped(s.length).foldLeft(Result.success) {
      case (Success, next) => next ==== s
      case (failure, _)    => failure
    }
  }

  private def propString(gen: Range[Int] => Gen[String], result: String => Result): Property = forAll {
    for {
      range <- genStringRange
      s     <- gen(range)
    } yield result(s)
  }

  private def testAllUnicode(s: String): Result = s.toSeq.forAll(testUnicode)

  private def testUnicode(c: Char): Result =
    c.inside('\u0000', '\uD7FF').or(c.inside('\uE000', '\uFFFD'))

  // String.trim.isEmpty would work but there are really more whitespace characters than just the ASCII ones.
  private def testAllWhitespace(s: String): Result =
    s.toSeq.forAll(testWhitespace)

  private def testWhitespace(c: Char): Result =
    c.inside('\u0000', '\u001F').or(c ==== ' ')

  private def testNotAllWhitespace(s: String): Result =
    Result.assert(s.isEmpty).or(not(testAllWhitespace(s), "All Whitespace")).log(s""""$s"""")

  // String.isBlank is insufficient.
  private def testAllBlank(s: String): Result =
    testAllWhitespace(s)

  private def testNotAllBlank(s: String): Result =
    not(testAllBlank(s), "All Blank")

  private def testNotTrimmed(s: String): Result =
    not(s ==== s.trim, "Trimmed")

  // TODO: Generalise the tests for the length of an element (see CollectionGeneratorsSpec).

  private def propGenMin(gen: Range[Int] => Gen[String], min: Int): Property =
    propGenMinWithSeed(gen, min, mockSeeds.minSeed)

  private def propGenMinWithSeed(gen: Range[Int] => Gen[String], min: Int, seed: => Seed): Property =
    for {
      range <- genStringRange.forAllWithLog(logRange)
    } yield gen(range)
      .generates(MinSize, seed) { s =>
        val firstBound = if (range.linear) range.origin + minScaled(range.min) else range.min
        s.length ==== math.max(min, math.min(firstBound, MaxStringLength))
      }

  private def propGenMax(gen: Range[Int] => Gen[String], min: Int) =
    propGenMaxResult(gen, min, _ ==== _)

  private def propGenMaxResult(gen: Range[Int] => Gen[String], min: Int, result: (Int, Int) => Result) =
    propGenMaxWithSeedResult(gen, min, mockSeeds.maxSeed, result)

  private def propGenMaxWithSeedResult(
      gen: Range[Int] => Gen[String],
      min: Int,
      seed: => Seed,
      result: (Int, Int) => Result
    ) =
    for {
      range <- genStringRange.forAllWithLog(logRange)
    } yield gen(range)
      .generates(MaxSize, seed)(x => result(x.length, math.max(min, math.min(range.max, MaxStringLength))))

  // This isn't perfect but it is good enough, we only need valid characters.
  private def genGenChar: Gen[Gen[Char]] =
    for {
      aLo <- Gen.char(0, 0xD7FF)
      aHi <- Gen.char(aLo, 0xD7FF)
      bLo <- Gen.char(0xE000, 0xFFFD)
      bHi <- Gen.char(bLo, 0xFFFD)
    } yield Gen.frequency1(
      charFrequency(aLo, aHi),
      charFrequency(bLo, bHi)
    )

  // Generating Strings is slow. 😢
  // Have a higher percentage of small ranges but still allow some arbitrary ones.
  private def genStringRange: Gen[Range[Int]] =
    Gen
      .frequency1(
        50 -> genRange(Gen.int(Range.linear(0, ReasonableStringLength))),
        50 -> genRange(Gen.int(Range.linear(ReasonableStringLength, 0))),
        1  -> genIntRange
      )
}
