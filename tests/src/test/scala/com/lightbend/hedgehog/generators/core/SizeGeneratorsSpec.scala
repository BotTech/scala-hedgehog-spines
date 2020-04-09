package com.lightbend.hedgehog.generators.core

import com.lightbend.hedgehog.generators.IntGenerators._
import com.lightbend.hedgehog.generators.core.SizeGenerators._
import com.lightbend.hedgehog.implicits.ArbitraryImplicits._
import com.lightbend.hedgehog.scalamock.{GeneratorSpec, TestRunnerMock}
import hedgehog._
import hedgehog.runner.Test

object SizeGeneratorsSpec extends TestRunnerMock with GeneratorSpec {

  override def tests: List[Test] =
    test("genSize", genSize).addConstantRangeTests(MinSize, MaxSize).tests ++
      test("genSplitSize", genSplitSizeParts).addGenProp(_ + " parts add up to whole", addToWhole).tests ++
      test("genSplitSizeAfter", genSplitSizeAfterParts)
        .addGenProp(_ + " parts add up to whole", addToWhole)
        .addProp(_ + " first part is correct size", propGenSplitSizeAfterFirstPartSize)
        .tests

  private def genSplitSizeParts =
    for {
      size   <- genSplitableSize
      (a, b) <- genSplitSize(size)
    } yield (size, a, b)

  private def genSplitSizeAfterParts =
    for {
      size   <- genSplitableSize
      n      <- genPositiveInt
      (a, b) <- genSplitSizeAfter(size, n)
    } yield (size, a, b)

  private def addToWhole(gen: Gen[(Size, Size, Size)]): Property =
    gen.forAll.map {
      case (size, a, b) => (a.value + b.value) ==== size.value
    }

  private def propGenSplitSizeAfterFirstPartSize: Property =
    for {
      size  <- genSplitableSize.log("size")
      n     <- genPositiveInt.log("n")
      split <- genSplitSizeAfter(size, n).log("a")
    } yield {
      val (a, _) = split
      if (n <= 0) a.value.inside(1, size.value)
      else if (n <= size.value) a.value.inside(n, size.value)
      else a ==== size
    }

  private def genSplitableSize: Gen[Size] = Gen.int(Range.linear(2, Size.max)).map(Size(_))
}
