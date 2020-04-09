package com.lightbend.hedgehog.generators.akka

import akka.util.{ByteString, ByteStringBuilder}
import com.lightbend.hedgehog.generators.ByteGenerators._
import com.lightbend.hedgehog.generators.CollectionGenerators._
import com.lightbend.hedgehog.generators.core.SizeGenerators
import hedgehog._
import org.scalactic.TripleEquals._

import scala.collection.{mutable, Factory}

object ByteStringGenerators {

  private val MaxByteStringLength = 100
  private val MaxByteStrings      = 100

  // See https://typesafe.slack.com/archives/C1ETEPM36/p1580331136068100
  implicit object ByteStringFactory extends Factory[Byte, ByteString] {

    override def fromSpecific(it: IterableOnce[Byte]): ByteString =
      newBuilder.addAll(it).result()

    override def newBuilder: mutable.Builder[Byte, ByteString] =
      new ByteStringBuilder
  }

  def genCompactedUnfragmentedByteString: Gen[ByteString] =
    Gen.list(genByte, Range.linear(0, MaxByteStringLength)).map(ByteString(_))

  def genUncompactedUnfragmentedByteString: Gen[ByteString] =
    for {
      byteString <- Gen.list(genByte, Range.linear(2, MaxByteStringLength + 1)).map(ByteString(_))
      from       <- Gen.int(Range.linear(0, byteString.length))
      untilMax = if (from === 0) byteString.length - 1 else byteString.length
      until <- Gen.int(Range.linear(from + 1, untilMax))
    } yield byteString.slice(from, until)

  def genFragmentedByteString: Gen[ByteString] = Gen.sized { size =>
    for {
      (headSize, tailSize) <- {
        if (size.value === 1) Gen.constant((size, size))
        else SizeGenerators.genSplitSize(size)
      }
      head <- genByteString.resize(headSize)
      tail <- if (tailSize.value === 0) genEmptyByteString else genByteString.resize(tailSize)
    } yield head ++ tail
  }

  def genEmptyByteString: Gen[ByteString] =
    Gen.constant(ByteString.empty)

  def genByteString: Gen[ByteString] =
    Gen.choice1(
      genCompactedUnfragmentedByteString,
      genUncompactedUnfragmentedByteString,
      genFragmentedByteString
    )

  def genByteStrings: Gen[Seq[ByteString]] =
    genCollection[ByteString, Seq](genByteString, Range.linear(0, MaxByteStrings))
}
