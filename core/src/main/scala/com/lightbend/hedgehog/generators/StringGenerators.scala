package com.lightbend.hedgehog.generators

import com.lightbend.hedgehog.generators.CharGenerators._
import com.lightbend.hedgehog.implicits.RangeImplicits._
import hedgehog.{Gen, Range}
import org.scalactic.TripleEquals._
import CollectionGenerators._

object StringGenerators {

  // We have to limit the string to approximately 1,500 characters otherwise it overflows the stack,
  // especially when debugging.
  // See https://github.com/hedgehogqa/scala-hedgehog/issues/47.
  private[generators] val MaxStringLength = 1500
  // 99 is chosen to guarantee that as Size(1) we get an empty string.
  // It could be bigger but generating Strings is slow.
  private[generators] val ReasonableStringLength = 99

  /**
    * Generates a String of characters from the given generator.
    * <p>
    * Negative values in the `range` are treated as a length of 0.
    * </p>
    *
    * @param gen generator for the characters in the String
    * @param range the range for the length of the String
    * @return A generator of Strings.
    */
  def genString(gen: Gen[Char], range: Range[Int]): Gen[String] =
    genList(gen, maxRange(range)).map(_.mkString)

  /**
    * Generates a String of characters from the given generator by concatenating the String's together.
    * <p>
    * Negative values in the `range` are treated as a length of 0.
    * </p>
    *
    * @param gen generator for the characters in the String
    * @param range the range for the length of the String
    * @return A generator of Strings.
    */
  def genConcatenatedString(gen: Gen[String], range: Range[Int]): Gen[String] =
    genJoined[Char, Iterable, Iterable](gen.map(_.toIterable), maxRange(range)).map(_.iterator.mkString)

  private def maxRange(range: Range[Int]): Range[Int] =
    range.clamp(0, MaxStringLength)

  /**
    * Generates a String of Unicode characters.
    * <p>
    * Negative values in the `range` are treated as a length of 0.
    * </p>
    *
    * @param range the range for the length of the String
    * @return A generator of Unicode Strings.
    */
  def genUnicodeString(range: Range[Int]): Gen[String] =
    genString(Gen.unicode, range)

  /**
    * Generates a String of Unicode characters.
    * <p>
    * The length of the String scales linearly with the size of the generator.
    * </p>
    *
    * @return A generator of Unicode Strings.
    */
  def genAnyUnicodeString: Gen[String] =
    genUnicodeString(Range.linear(0, ReasonableStringLength))

  /**
    * Generates a non-empty String of Unicode characters.
    * <p>
    * The length of the String scales linearly with the size of the generator.
    * </p>
    *
    * @return A generator of non-empty Unicode Strings.
    */
  def genAnyNonEmptyUnicodeString: Gen[String] =
    genUnicodeString(Range.linear(1, ReasonableStringLength))

  /**
    * Generates a String which contains only whitespace characters.
    * <p>
    * Negative values in the `range` are treated as a length of 0.
    * </p>
    *
    * @param range the range for the length of the String
    * @return A generator of whitespace Strings.
    */
  def genWhitespaceString(range: Range[Int]): Gen[String] =
    genString(genWhitespaceChar, range)

  /**
    * Generates a String which is either empty or contains only blank characters.
    * <p>
    * Negative values in the `range` are treated as a length of 0.
    * </p>
    *
    * @param range the range for the length of the String
    * @return A generator of blank Strings.
    */
  def genBlankString(range: Range[Int]): Gen[String] =
    genString(genBlankChar, range)

  /**
    * Generates a String which is either empty or contains at least one non-whitespace character.
    * <p>
    * Negative values in the `range` are treated as a length of 0.
    * </p>
    *
    * @param range the range for the length of the String
    * @return A generator of non-whitespace Strings.
    */
  def genNonWhitespaceString(range: Range[Int]): Gen[String] =
    genMaybeEmpty(genNonWhitespaceBlankString(genNonWhitespaceChar, _), range)

  /**
    * Generates a String which contains at least one non-blank character.
    * <p>
    * Non-positive values in the `range` are treated as a length of 1.
    * </p>
    *
    * @param range the range for the length of the String
    * @return A generator of non-blank Strings.
    */
  def genNonBlankString(range: Range[Int]): Gen[String] =
    genNonWhitespaceBlankString(genNonBlankChar, range)

  private def genNonWhitespaceBlankString(gen: Gen[Char], range: Range[Int]): Gen[String] =
    genStringWithIndel(genUnicodeString(range), gen, 1)

  // TODO: Add test and docs.
  def genStringWithIndel(base: Gen[String], insert: Gen[Char], min: Int): Gen[String] =
    for {
      str      <- base
      pos      <- Gen.int(Range.linear(1, str.length))
      toInsert <- genString(insert, Range.linear(min, math.max(min, str.length - pos + 1)))
    } yield {
      val prefix = str.take(pos - 1)
      val result = prefix + toInsert + str.drop(prefix.length).drop(toInsert.length)
      assert(result.length === str.length || result.length === 1 && str.length === 0 && min === 1)
      result
    }

  /**
    * Generates a String which contains at least one leading or trailing whitespace character.
    * <p>
    * Non-positive values in the `range` are treated as a length of 1.
    * </p>
    *
    * @param range the range for the length of the String
    * @return A generator of non-trimmed Strings.
    */
  def genNotTrimmedString(range: Range[Int]): Gen[String] =
    for {
      length  <- Gen.int(range.clamp(1, MaxStringLength))
      leading <- genWhitespaceString(Range.linear(0, length))
      trailingMin = if (leading.length === 0) 1 else 0
      trailing <- genWhitespaceString(Range.linear(trailingMin, length - leading.length))
      middle   <- genNonWhitespaceString(Range.singleton(length - leading.length - trailing.length))
    } yield s"$leading$middle$trailing"

  /**
    * Generates a String which does not contain any leading or trailing whitespace characters.
    * <p>
    * Negative values in the `range` are treated as a length of 0.
    * </p>
    *
    * @param range the range for the length of the String
    * @return A generator of trimmed Strings.
    */
  def genTrimmedString(range: Range[Int]): Gen[String] =
    for {
      str   <- genUnicodeString(range)
      first <- genNonWhitespaceChar.map(_.toString)
      last  <- genNonWhitespaceChar.map(_.toString)
    } yield {
      if (str.isEmpty) str
      else if (str.length === 1) first
      else first + str.tail.init + last
    }

  /**
    * Generates a String which empty or contains at least one leading or trailing whitespace character.
    * <p>
    * Negative values in the `range` are treated as a length of 0.
    * </p>
    *
    * @param range the range for the length of the String
    * @return A generator of nonsense Strings.
    */
  def genNonsenseString(range: Range[Int]): Gen[String] =
    genMaybeEmpty(genNotTrimmedString, range)

  /**
    * Generates a String which is not empty and does not contain any leading or trailing whitespace characters.
    * <p>
    * Non-positive values in the `range` are treated as a length of 1.
    * </p>
    *
    * @param range the range for the length of the String
    * @return A generator of sensible Strings.
    */
  def genSensibleString(range: Range[Int]): Gen[String] =
    genTrimmedString(range.clampMin(1))

  // TODO: Test and docs.
  def genMaybeEmpty(gen: Range[Int] => Gen[String], range: Range[Int]): Gen[String] =
    Gen.sized { size =>
      val (min, max) = if (range.linear) {
        val (x, y) = range.bounds(size)
        if (x <= y) (x, y) else (y, x)
      } else (range.min, range.max)
      if (min > 0) gen(range)
      else if (max <= 0) Gen.constant("")
      else gen(range).option.map(_.getOrElse(""))
    }
}
