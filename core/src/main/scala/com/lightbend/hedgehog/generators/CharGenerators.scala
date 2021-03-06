package com.lightbend.hedgehog.generators

import com.lightbend.hedgehog.generators.ArbitraryGenerators._
import com.lightbend.hedgehog.macros.CharacterMacros
import hedgehog.Gen
import org.scalactic.TripleEquals._

import scala.annotation.tailrec

object CharGenerators {

  def genChar: Gen[Char] = Gen.char(Char.MinValue, Char.MaxValue)

  def genControlC0: Gen[Char] = Gen.char('\u0000', '\u001F')

  def genSpace: Gen[Char] = Gen.constant(' ')

  def genLatinLetterLowerCase: Gen[Char] = Gen.char('a', 'z')

  // Even distribution of spaces and control characters.
  // Shrink towards the control characters as the ' ' character is easier to see in the
  // logs than a space.
  def genWhitespaceChar: Gen[Char] = Gen.choice1(genControlC0, genSpace)

  // TODO: Include other control and formatting characters.
  //  Here is an easy to read list http://www.fileformat.info/info/unicode/category/index.htm.
  def genBlankChar: Gen[Char] = genWhitespaceChar

  def genNonWhitespaceChar: Gen[Char] =
    Gen.frequency1(
      charFrequency('\u0021', '\uD7FF'),
      charFrequency('\uE000', '\uFFFD')
    )

  // TODO: Exclude other control and formatting characters.
  //  Here is an easy to read list http://www.fileformat.info/info/unicode/category/index.htm.
  def genNonBlankChar: Gen[Char] = genNonWhitespaceChar

  def genNonLatinDigit: Gen[Char] =
    Gen.frequency1(
      charFrequency('\u0000', '\u002F'),
      charFrequency('\u003A', '\uD7FF'),
      charFrequency('\uE000', '\uFFFD')
    )

  def genWordChar: Gen[Char] =
    Gen.frequency1(
      charFrequency('A', 'Z'),
      charFrequency('a', 'z'),
      charFrequency('_', '_'),
      charFrequency('0', '9')
    )

  def genUnicodeExcept(chars: Char*): Gen[Char] =
    genCharsExcept(List('\u0000' -> '\uD7FF', '\uE000' -> '\uFFFD'), chars: _*)

  // FIXME: The methods below are untested.

  // TODO: Verify if this is the correct definition.
  //  It was taken from java.net.URI.Parser.scanEscape.
  def genVisibleNonUSASCIIChar: Gen[Char] = {
    val ranges = CharacterMacros.filterRanges(List('\u0081' -> '\uD7FF', '\uE000' -> '\uFFFD')) { c =>
      !Character.isSpaceChar(c) && !Character.isISOControl(c)
    }
    Gen.frequencyUnsafe(ranges.map((charFrequency _).tupled))
  }

  def genCharsExcept(ranges: List[(Char, Char)], chars: Char*): Gen[Char] =
    Gen.frequencyUnsafe(charFrequenciesExcept(ranges, chars: _*))

  def charFrequency(lo: Char, hi: Char): (Int, Gen[Char]) =
    genFrequency(lo, hi)(Gen.char)

  def charFrequencyExcept(lo: Char, hi: Char, chars: Char*): List[(Int, Gen[Char])] =
    charFrequenciesExcept(List(lo -> hi), chars: _*)

  def charFrequenciesExcept(ranges: List[(Char, Char)], chars: Char*): List[(Int, Gen[Char])] = {
    val rangesExcept = charRangesExcept(ranges, chars: _*)
    rangesExcept.map((charFrequency _).tupled)
  }

  // TODO: Replace this with a macro.
  def charRangesExcept(ranges: List[(Char, Char)], chars: Char*): List[(Char, Char)] = {
    def canIncrement(char: Char) = char < '\uD7FF' || char < '\uFFFD'
    def canDecrement(char: Char) = char > '\u0000' || char > '\uE000'
    def inc(char: Char)          = (char + 1).toChar
    def dec(char: Char)          = (char - 1).toChar
    def split(char: Char, lo: Char, hi: Char): List[(Char, Char)] =
      if (char === lo && canIncrement(char)) List(inc(char) -> hi)
      else if (char === hi && canDecrement(char)) List(lo -> dec(char))
      else if (lo < char && char < hi) List(lo -> dec(char), inc(char) -> hi)
      else List((lo, hi))
    // This is not optimal but it should be fast enough for most purposes.
    @tailrec
    def filter(chars: List[Char], acc: List[(Char, Char)]): List[(Char, Char)] = chars match {
      case Nil => acc
      case head :: tail =>
        val x = acc.flatMap {
          case (lo, hi) => split(head, lo, hi)
        }
        filter(tail, x)
    }
    filter(chars.toList, ranges)
  }
}
