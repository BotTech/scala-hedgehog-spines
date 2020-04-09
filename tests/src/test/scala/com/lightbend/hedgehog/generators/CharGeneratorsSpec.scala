package com.lightbend.hedgehog.generators

import com.lightbend.hedgehog.generators.CharGenerators._
import com.lightbend.hedgehog.implicits.ArbitraryImplicits._
import com.lightbend.hedgehog.implicits.TreeImplicits._
import com.lightbend.hedgehog.scalamock.{GeneratorSpec, TestRunnerMock}
import com.lightbend.hedgehog.testkit.{GeneratorTests, Probabilities}
import hedgehog._
import hedgehog.core.Seed
import hedgehog.runner.Test
import org.scalactic.TripleEquals._

import scala.Ordering._

object CharGeneratorsSpec extends TestRunnerMock with GeneratorSpec with Probabilities {

  override def tests: List[Test] =
    genCharTests ++
      genLatinLetterLowerCaseTests ++
      genSpaceTests ++
      genWhitespaceCharTests ++
      genCharFrequenciesTests ++
      genBlankCharTests ++
      genNonBlankCharTests ++
      genNonLatinDigitTests ++
      genWordCharTests

  private def genCharTests =
    test("genChar", genChar).addConstantRangeTests('\u0000', '\uFFFF').tests ++
      test("genControlC0", genControlC0)
        .addConstantRangeTests('\u0000', '\u001F')
        .addGenProp(_ + s" generates only C0 control characters", propControlC0Chars)
        .tests

  private def genLatinLetterLowerCaseTests =
    test("genLatinLetterLowerCase", genLatinLetterLowerCase)
      .addGenProp(_ + s" generates only latin lower case letters", propLatinLetterLowerCaseChars)
      .addConstantRangeTests('a', 'z')
      .tests

  private def genSpaceTests =
    test("genSpace", genSpace).addSingletonTests(' ').tests

  private def genWhitespaceCharTests =
    charChoicesTests(
      "genWhitespaceChar",
      genWhitespaceChar,
      "whitespace",
      propWhitespaceChars,
      '\u0000' -> '\u001F',
      ' '      -> ' '
    ).addGenProbabilities(_ + " generates characters with probabilities", propWhitespaceCharProbabilities).tests

  private def genCharFrequenciesTests =
    charFrequenciesTests(
      "genNonWhitespaceChar",
      genNonWhitespaceChar,
      "non-whitespace",
      propNonWhitespaceChars,
      '!'      -> '\uD7FF',
      '\uE000' -> '\uFFFD'
    ).tests

  private def genBlankCharTests =
    charChoicesTests(
      "genBlankChar",
      genBlankChar,
      "blank",
      propBlankChars,
      '\u0000' -> '\u001F',
      ' '      -> ' '
    ).addGenProbabilities(_ + " generates characters with probabilities", propWhitespaceCharProbabilities).tests

  private def genNonBlankCharTests =
    charFrequenciesTests(
      "genNonBlankChar",
      genNonBlankChar,
      "non-blank",
      propNonBlankChars,
      '!'      -> '\uD7FF',
      '\uE000' -> '\uFFFD'
    ).tests

  private def genNonLatinDigitTests =
    charFrequenciesTests(
      "genNonLatinDigit",
      genNonLatinDigit,
      "non-latin-digit",
      propNonLatinDigit,
      '\u0000' -> '\u002F',
      '\u003A' -> '\uD7FF',
      '\uE000' -> '\uFFFD'
    ).tests

  private def genWordCharTests =
    charFrequenciesTests(
      "genWordChar",
      genWordChar,
      "non-word-char",
      propWordChar,
      'A' -> 'Z',
      'a' -> 'z',
      '_' -> '_',
      '0' -> '9'
    ).tests

  private def charChoicesTests(
      name: String,
      gen: Gen[Char],
      description: String,
      property: Gen[Char] => Property,
      ranges: (Char, Char)*
    ): GeneratorTests[Char] = {
    def nextAlternative(alternative: Long, from: Char, to: Char) = {
      { val _ = from }
      { val _ = to }
      alternative + 1
    }
    charAlternativesTests(name, gen, description, property, 0, nextAlternative, ranges: _*)
  }

  private def charFrequenciesTests(
      name: String,
      gen: Gen[Char],
      description: String,
      property: Gen[Char] => Property,
      ranges: (Char, Char)*
    ): GeneratorTests[Char] = {
    def nextAlternative(alternative: Long, from: Char, to: Char) =
      alternative + (to - from) + 1
    charAlternativesTests(name, gen, description, property, 1, nextAlternative, ranges: _*)
  }

  private def charAlternativesTests(
      name: String,
      gen: Gen[Char],
      description: String,
      property: Gen[Char] => Property,
      zero: Long,
      nextAlternative: (Long, Char, Char) => Long,
      ranges: (Char, Char)*
    ): GeneratorTests[Char] = {
    val tests = test(name, gen)
      .addGenProp(_ + s" generates only $description characters", property)
    val (result, _) = ranges.foldLeft((tests, zero)) {
      case ((tests, alternative), (from, to)) =>
        val nextTests = addCharRangeExamples(tests, alternative, from, to)
        (nextTests, nextAlternative(alternative, from, to))
    }
    result
  }

  private def addCharRangeExamples(
      tests: GeneratorTests[Char],
      alternative: Long,
      from: Char,
      to: Char
    ): GeneratorTests[Char] =
    tests
      .addGenExample(
        _ + s" generates '${from.toString}' as a minimum",
        testAlternative(_, MinSize, alternative, from, to, from)
      )
      .addGenExample(
        _ + s" generates '${to.toString}' as a maximum",
        testAlternative(_, MaxSize, alternative, from, to, to)
      )

  private def validUnicodeChar(c: Char): Result =
    c.inside('\u0000', '\uD7FF').or(c.inside('\uE000', '\uFFFD'))

  private def propControlC0Chars(gen: Gen[Char]): Property =
    gen.map(_.inside('\u0000', '\u001F')).forAll

  private def propLatinLetterLowerCaseChars(gen: Gen[Char]): Property =
    gen.map(_.inside('a', 'z')).forAll

  private def propWhitespaceChars(gen: Gen[Char]): Property =
    gen.map(_.inside('\u0000', ' ')).forAll

  private def propBlankChars(gen: Gen[Char]): Property =
    gen.map(_.inside('\u0000', ' ')).forAll

  private def propNonWhitespaceChars(gen: Gen[Char]): Property =
    gen.map { c =>
      validUnicodeChar(c).and(c.without('\u0000', '\u0020'))
    }.forAll

  private def propNonBlankChars(gen: Gen[Char]): Property =
    gen.map { c =>
      validUnicodeChar(c).and(c.without('\u0000', '\u0020'))
    }.forAll

  private def propNonLatinDigit(gen: Gen[Char]): Property =
    gen.map { c =>
      validUnicodeChar(c).and(c.without('0', '9'))
    }.forAll

  private def propWordChar(gen: Gen[Char]): Property =
    gen.map { c =>
      Result.assert(c.toString.matches("\\w"))
    }.forAll

  private def testAlternative(
      gen: Gen[Char],
      size: Size,
      alternative: Long,
      from: Char,
      to: Char,
      value: Char
    ): Result = {
    val seed = stub[Seed]
    // choices go from 0, frequencies go from 1.
    (seed.chooseLong _).when(*, *).returns((seed, alternative)).noMoreThanOnce()
    (seed.chooseLong _).when(from.toLong, to.toLong).returns((seed, value.toLong))
    gen.run(size, seed).generated(_ ==== value)
  }

  private def propWhitespaceCharProbabilities(gen: Gen[Char]): Property =
    gen.forAll
      .cover(OneToOne, "spaces", _ === ' ')
      .cover(OneToOne, "not spaces", _ !== ' ')
      .map(_ => Result.success)
}
