package com.lightbend.hedgehog.generators.akka.http

import akka.http.scaladsl.model.ContentType
import com.lightbend.hedgehog.implicits.Implicits._
import hedgehog.Gen
import MediaTypeGenerators._
import HttpCharsetGenerators._

object ContentTypeGenerators {

  def genBinaryContentType: Gen[ContentType.Binary] =
    genBinaryMediaType.map(ContentType.Binary)

  def genWithFixedCharsetContentType: Gen[ContentType.WithFixedCharset] =
    genWithFixedCharsetMediaType.map(ContentType.WithFixedCharset)

  def genWithCharsetContentType: Gen[ContentType.WithCharset] =
    for {
      mediaType   <- genWithOpenCharsetMediaType
      httpCharset <- genHttpCharsets
    } yield ContentType.WithCharset(mediaType, httpCharset)

  def genNonBinaryContentType: Gen[ContentType.NonBinary] =
    Gen.choice1(genWithFixedCharsetContentType.widen, genWithCharsetContentType.widen)

  def genWithMissingContentType: Gen[ContentType.WithMissingCharset] =
    genWithOpenCharsetMediaType.map(ContentType.WithMissingCharset)

  def genNonBinaryOrMissingContentType: Gen[ContentType] =
    Gen.choice1(genNonBinaryContentType.widen, genWithMissingContentType.widen)

  def genContentType: Gen[ContentType] =
    Gen.choice1(genBinaryContentType.widen, genNonBinaryContentType.widen, genWithMissingContentType.widen)
}
