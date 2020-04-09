package com.lightbend.hedgehog.generators.akka.http

import akka.NotUsed
import akka.http.scaladsl.model.HttpEntity.ChunkStreamPart
import akka.http.scaladsl.model._
import akka.stream.scaladsl.Source
import akka.util.ByteString
import com.lightbend.hedgehog.generators.ByteGenerators._
import com.lightbend.hedgehog.generators.CollectionGenerators.genList
import com.lightbend.hedgehog.generators.LongGenerators._
import com.lightbend.hedgehog.generators.StringGenerators._
import com.lightbend.hedgehog.generators.akka.ByteStringGenerators._
import com.lightbend.hedgehog.generators.akka.http.ContentTypeGenerators._
import com.lightbend.hedgehog.generators.akka.http.HttpHeaderGenerators._
import com.lightbend.hedgehog.implicits.GeneratorImplicits._
import hedgehog._
import org.scalactic.TripleEquals._

object RequestEntityGenerators {

  private val MaxChunks      = 100
  private val MaxByteStrings = 100

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  def genDefaultRequestEntity: Gen[HttpEntity.Default] =
    genByteStringSource.flatMap(genDefaultRequestEntityWithData)

  // TODO: We might also want another generator that ensures that the content length is correct.
  def genDefaultRequestEntityWithData(data: Source[ByteString, Any]): Gen[HttpEntity.Default] =
    for {
      contentType   <- genContentType
      contentLength <- genPositiveLong
    } yield HttpEntity.Default(contentType, contentLength, data)

  def genEmptyRequestEntity: Gen[HttpEntity.Strict] =
    genContentType.map(HttpEntity.empty)

  def genStrictRequestEntity: Gen[HttpEntity.Strict] =
    genByteString.flatMap(genStrictRequestEntityWithData)

  def genStrictRequestEntityWithData(data: ByteString): Gen[HttpEntity.Strict] =
    genContentType.map(HttpEntity.Strict(_, data))

  def genUniversalEntity: Gen[UniversalEntity] =
    Gen.choice1(genDefaultRequestEntity.widen, genStrictRequestEntity.widen)

  def genUniversalEntityWithData(data: Seq[ByteString]): Gen[UniversalEntity] =
    Gen.choice1(
      genDefaultRequestEntityWithData(Source(data)).widen,
      genStrictRequestEntityWithData(data.foldLeft(ByteString.empty)(_ ++ _)).widen
    )

  def genChunk: Gen[HttpEntity.Chunk] =
    genNonEmptyBytes.map(HttpEntity.Chunk(_))

  def genLastChunk: Gen[HttpEntity.LastChunk] =
    for {
      extension <- genAnyUnicodeString
      headers   <- genHttpHeaders
    } yield HttpEntity.LastChunk(extension, headers)

  def genChunkStreamPart: Gen[ChunkStreamPart] =
    Gen.choice1(genChunk.widen, genLastChunk.widen)

  // TODO: What about a generator that doesn't have a LastChunk as the last chunk?

  def genChunkStreamPartSource: Gen[Source[ChunkStreamPart, NotUsed]] =
    for {
      chunks    <- Gen.list(genChunk.widen[ChunkStreamPart], Range.linear(0, MaxChunks))
      lastChunk <- genLastChunk
    } yield Source(chunks).concat(Source.single(lastChunk))

  def genChunkStreamPartSourceFromData(data: Seq[ByteString]): Gen[Source[ChunkStreamPart, NotUsed]] =
    genLastChunk.map { lastChunk =>
      Source(
        data
          .filter(_.nonEmpty)
          .map[ChunkStreamPart](HttpEntity.Chunk(_))
      ).concat(Source.single(lastChunk))
    }

  def genChunkedRequestEntity: Gen[HttpEntity.Chunked] =
    for {
      contentType <- genContentType
      chunks      <- genChunkStreamPartSource
    } yield HttpEntity.Chunked(contentType, chunks)

  def genChunkedRequestEntityWithData(data: Seq[ByteString]): Gen[HttpEntity.Chunked] =
    for {
      contentType <- genContentType
      chunks      <- genChunkStreamPartSourceFromData(data)
    } yield HttpEntity.Chunked(contentType, chunks)

  def genRequestEntity: Gen[RequestEntity] =
    Gen.choice1(genUniversalEntity.widen, genChunkedRequestEntity.widen)

  def genRequestEntityFor(method: HttpMethod, protocol: HttpProtocol): Gen[RequestEntity] =
    if (!method.isEntityAccepted) genEmptyRequestEntity.widen
    else if (protocol === HttpProtocols.`HTTP/1.0`) genUniversalEntity.widen
    else genRequestEntity

  def genRequestEntityWithData(data: Seq[ByteString]): Gen[RequestEntity] =
    Gen.choice1(genUniversalEntityWithData(data).widen, genChunkedRequestEntityWithData(data).widen)

  // TODO: Generate infinite sources.
  private def genByteStringSource: Gen[Source[ByteString, NotUsed]] =
    genList(genByteString, Range.linear(0, MaxByteStrings)).map(Source(_))
}
