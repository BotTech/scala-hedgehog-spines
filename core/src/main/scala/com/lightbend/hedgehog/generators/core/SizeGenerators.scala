package com.lightbend.hedgehog.generators.core

import hedgehog.{Gen, Range, Size}
import org.scalactic.Requirements._

// Don't use any of our other custom generators in here since we use this to test them and would
// end up with circular reasoning which would be prone to false positives.
// These ought to go into their own project to prevent that but that is too much overhead.
object SizeGenerators {

  def genSize: Gen[Size] = Gen.int(Range.constant(1, Size.max)).map(Size(_))

  /**
    * Creates a generator that splits the given `Size`.
    * <p>
    * The result is a pair of sizes which add up to the original size.
    * <p>
    * <p>
    * The `size` must be at least 2 as it is impossible to split a size that is any smaller.
    * <p>
    * <p>
    * The following property holds:
    * <ul>
    * <li>`left.value + right.value == size.value`</li>
    * </ul>
    * </p>
    *
    * @param size the size to split
    * @return The size split in two.
    * @throws IllegalArgumentException if the size is 1
    */
  def genSplitSize(size: Size): Gen[(Size, Size)] = genSplitSizeAfter(size, 1)

  /**
    * Creates a generator that splits the given `Size` after `n`.
    * <p>
    * The result is a pair of sizes where the first size is at least as big as the smaller
    * of `n` and `size.value`, and the second size is the remainder.
    * <p>
    * <p>
    * The `size` must be at least 2 as it is impossible to split a size that is any smaller.
    * <p>
    * <p>
    * If `n` is non-positive then 1 will be used instead.
    * <p>
    * <p>
    * The following properties hold:
    * <ul>
    * <li>`Math.min(n, size.value) <= left.value`</li>
    * <li>`left.value <= size.value`</li>
    * <li>`left.value + right.value == size.value`</li>
    * </ul>
    * </p>
    *
    * @param size the size to split
    * @param n split after a size that is at least this big
    * @return The size split in two.
    * @throws IllegalArgumentException if the size is 1
    */
  def genSplitSizeAfter(size: Size, n: Int): Gen[(Size, Size)] = {
    require(size.value >= 2)
    for {
      left <- Gen.int(Range.linear(math.max(1, math.min(n, size.value)), size.value))
    } yield (Size(left), Size(size.value - left))
  }
}
