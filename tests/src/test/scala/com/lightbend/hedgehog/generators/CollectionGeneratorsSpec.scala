package com.lightbend.hedgehog.generators

import com.lightbend.hedgehog.generators.CharGenerators._
import com.lightbend.hedgehog.generators.CollectionGenerators._
import com.lightbend.hedgehog.generators.IntGenerators._
import com.lightbend.hedgehog.generators.core.RangeGenerators._
import com.lightbend.hedgehog.generators.StringGenerators._
import com.lightbend.hedgehog.implicits.ArbitraryImplicits._
import com.lightbend.hedgehog.implicits.GeneratorImplicits._
import com.lightbend.hedgehog.implicits.RangeImplicits._
import com.lightbend.hedgehog.scalamock.{GeneratorSpec, TestRunnerMock}
import com.lightbend.hedgehog.testkit.GeneratorProperties._
import com.lightbend.hedgehog.testkit.Probabilities
import hedgehog._
import hedgehog.core.{Cover, Seed}
import hedgehog.runner.Test
import org.scalactic.TripleEquals._

object CollectionGeneratorsSpec extends TestRunnerMock with GeneratorSpec with Probabilities {

  private val MaxCollectionSize = 1000
  private val MaxCharsToJoin    = 5

  // TODO: These tests are a bit of a Frankenstein monster. The test syntax needs an overhaul.

  override def tests: List[Test] =
    genCollectionTests("genList", genList(genInt, _)).tests ++
      genCollectionTests("genCollection", genCollection[Int, Seq](genInt, _)).tests ++
      genJoinedTests.tests ++
      test("genLazyList", genLazyList(genInt)).tests ++
      test("genShuffled", genShuffled(genLetters))
        .addProp(_ + " has the same elements", propGenShuffledSameElements)
        .addProbabilities(_ + " shuffles values randomly", propGenShuffledIsShuffledRandomly)
        .tests ++
      test("genPerturbed", genPerturbed[Char, Iterable[Char], LazyList[Int], List](genStrings, genAnyInts))
        .addProp(_ + " has the same elements", propGenPerturbedSameElements)
        .addProbabilities(_ + " perturbs values randomly", propGenPerturbedIsPerturbedRandomly)
        .tests ++
      test("genPerturbedN", genPerturbedN(genStrings, genAnyInts))
        .addProp(_ + " has the same elements", propGenPerturbedNSameElements)
        .addProbabilities(_ + " perturbs values randomly", propGenPerturbedNIsPerturbedRandomly)
        .tests

  private def genCollectionTests[A <: IterableOnce[Any]](name: String, gen: Range[Int] => Gen[A]) =
    test(name, genCollectionSizeRange.flatMap(gen))
      .addProp(_ + " generates collection with length of lower bound as minimum", propGenMin(gen))
      .addProp(_ + " generates collection with length of upper bound as maximum", propGenMax(gen))

  private def genJoinedTests =
    test("genJoined", genCollectionSizeRange.flatMap(genJoinedChars))
      .addProp(_ + " generates collection with length of lower bound as minimum", propGenMin(genJoinedChars))
      .addProp(
        _ + " generates collection with length of upper bound as maximum",
        propGenMaxResult(genJoinedChars, {
          // We can get additional chars from the final join.
          case (size, max) => size.inside(max, max + MaxCharsToJoin - 1)
        })
      )
      .addProp(_ + " discards if nothing to join", propJoinedDiscardsEmpty)
      .addProp(_ + " discards if inner generator discards", propJoinedDiscardsWhenInnerDiscards)

  // TODO: Generalise the tests for the length of an element (see StringGeneratorsSpec).

  private def propGenMin[A <: IterableOnce[Any]](gen: Range[Int] => Gen[A]): Property =
    propGenMinWithSeed(gen, mockSeeds.minSeed)

  private def propGenMinWithSeed[A <: IterableOnce[Any]](gen: Range[Int] => Gen[A], seed: => Seed): Property =
    for {
      range <- genCollectionSizeRange.forAllWithLog(logRange)
    } yield gen(range)
      .generates(MinSize, seed) { c =>
        val firstBound = if (range.linear) Range.scaleLinear(MinSize, range.origin, range.min) else range.min
        c.iterator.size ==== math.max(0, math.min(firstBound, MaxCollectionSize))
      }

  private def propGenMax[A <: IterableOnce[Any]](gen: Range[Int] => Gen[A]) =
    propGenMaxResult(gen, _ ==== _)

  private def propGenMaxResult[A <: IterableOnce[Any]](gen: Range[Int] => Gen[A], result: (Int, Int) => Result) =
    propGenMaxWithSeedResult(gen, mockSeeds.maxSeed, result)

  private def propGenMaxWithSeedResult[A <: IterableOnce[Any]](
      gen: Range[Int] => Gen[A],
      seed: => Seed,
      result: (Int, Int) => Result
    ) =
    for {
      range <- genCollectionSizeRange.forAllWithLog(logRange)
    } yield gen(range)
      .generates(MaxSize, seed)(x => result(x.iterator.size, math.max(0, math.min(range.max, MaxCollectionSize))))

  private def propGenPerturbedSameElements: Property = forAll {
    for {
      strings   <- genStrings
      perturbed <- genPerturbed[Char, Iterable[Char], LazyList[Int], List](Gen.constant(strings), genAnyInts)
    } yield join(strings) ==== join(perturbed)
  }

  private def propGenPerturbedNSameElements: Property = forAll {
    for {
      strings   <- genStrings
      perturbed <- genPerturbedN(Gen.constant(strings), genAnyInts)
    } yield join(strings) ==== join(perturbed)
  }

  private def propGenPerturbedIsPerturbedRandomly: Property = {
    for {
      strings <- genList(genAnyUnicodeString.map(_.toIterable: Seq[Char]), Range.linear(5, 10))
      perturbed <- genPerturbed[Char, Iterable[Char], LazyList[Int], List](
        Gen.constant(strings),
        genLazyList(Gen.int(Range.linearFrom(0, -5, 5)))
      )
    } yield (strings, perturbed)
  }.forAll
  // This percentage is totally made up but it works well enough for these specific ranges.
    .cover(OneToTwo, "same average lengths", (sameAverageLengths _).tupled)
    .map(_ => Result.success)

  private def propGenPerturbedNIsPerturbedRandomly: Property = {
    for {
      strings   <- genList(genAnyUnicodeString.map(_.toIterable: Seq[Char]), Range.linear(5, 10))
      perturbed <- genPerturbedN(Gen.constant(strings), genLazyList(Gen.int(Range.linearFrom(0, -5, 5))))
    } yield (strings, perturbed)
  }.forAll
  // This percentage is totally made up but it works well enough for these specific ranges.
    .cover(OneToTwo, "same average lengths", (sameAverageLengths _).tupled)
    .map(_ => Result.success)

  private def propJoinedDiscardsEmpty: Property = {
    val gen = genRange(genPositiveInt).flatMap { range =>
      val charGen = genChar.list(Range.singleton(0))
      genJoined[Char, Iterable, Iterable](charGen.widen, range)
    }
    propAllDiscards(gen)
  }

  private def propJoinedDiscardsWhenInnerDiscards: Property = {
    val gen = genRange(genPositiveInt).flatMap { range =>
      genJoined[Char, Iterable, Iterable](Gen.discard, range)
    }
    propAllDiscards(gen)
  }

  private def propGenShuffledSameElements: Property = forAll {
    for {
      letters  <- genLetters
      shuffled <- genShuffled(Gen.constant(letters))
    } yield letters.sorted ==== shuffled.sorted
  }

  private def propGenShuffledIsShuffledRandomly: Property = {
    for {
      list   <- genShuffled(genAlphabet)
      idx    <- Gen.int(Range.constant(0, list.size - 1))
      letter <- genLatinLetterLowerCase
    } yield list(idx) === letter
  }.forAll
    .cover(TwentyFiveToOne, "random letter in random position", identity)
    .map(_ => Result.success)

  private def genLetters: Gen[List[Char]] =
    genList(genLatinLetterLowerCase, Range.linear(0, 100))

  private def genAlphabet: Gen[List[Char]] =
    Gen.constant(('a' to 'z').toList)

  private def genStrings: Gen[List[Iterable[Char]]] =
    genList(genAnyUnicodeString.map(_.toIterable), Range.linear(0, 100))

  private def genAnyInts: Gen[LazyList[Int]] =
    genLazyList(genInt)

  private def genCollectionSizeRange: Gen[Range[Int]] =
    genRange(Gen.int(Range.linearFrom(0, -MaxCollectionSize, MaxCollectionSize)))

  private def join(strings: List[Iterable[Char]]): String =
    strings.flatten.mkString

  private def sameAverageLengths(a: List[Iterable[Char]], b: List[Iterable[Char]]): Cover =
    averageLength(a) === averageLength(b)

  private def averageLength(strings: List[Iterable[Char]]): Long = {
    val withoutEmpty = strings.filter(_.nonEmpty)
    if (withoutEmpty.isEmpty) 0
    else withoutEmpty.foldLeft(0L)(_ + _.size) / withoutEmpty.size
  }

  private def genJoinedChars(range: Range[Int]): Gen[Iterable[Char]] = {
    val gen = genList(genChar, Range.constant(1, MaxCharsToJoin))
    genJoined[Char, Iterable, Iterable](gen.widen, range)
  }
}
