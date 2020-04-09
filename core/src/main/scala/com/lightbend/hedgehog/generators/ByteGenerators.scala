package com.lightbend.hedgehog.generators

import hedgehog.{Gen, Range}
import CollectionGenerators._

object ByteGenerators {

  private val MaxBytes = 1000

  def genByte: Gen[Byte] =
    Gen.byte(Range.linearFrom(0, Byte.MinValue, Byte.MaxValue))

  def genBytes: Gen[Array[Byte]] =
    genArray(genByte, Range.linear(0, MaxBytes))

  def genNonEmptyBytes: Gen[Array[Byte]] =
    genArray(genByte, Range.linear(1, MaxBytes))
}
