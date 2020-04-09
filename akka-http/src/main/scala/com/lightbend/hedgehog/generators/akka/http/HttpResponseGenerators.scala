package com.lightbend.hedgehog.generators.akka.http

import akka.http.scaladsl.model._
import akka.stream.scaladsl.Source
import akka.util.ByteString
import com.lightbend.hedgehog.generators.StringGenerators.genAnyUnicodeString
import com.lightbend.hedgehog.generators.akka.ByteStringGenerators._
import com.lightbend.hedgehog.generators.akka.http.ContentTypeGenerators._
import com.lightbend.hedgehog.generators.akka.http.HttpHeaderGenerators._
import com.lightbend.hedgehog.generators.akka.http.HttpProtocolGenerators._
import com.lightbend.hedgehog.generators.akka.http.ResponseEntityGenerators._
import com.lightbend.hedgehog.generators.akka.http.StatusCodeGenerators._
import hedgehog.{Gen, Range}

object HttpResponseGenerators {

  private val MaxTextChunks = 100

  def genChunkedBinaryHttpResponse: Gen[HttpResponse] =
    for {
      chunks   <- genByteStrings
      response <- genHttpResponseWithBinaryChunks(chunks)
    } yield response

  def genChunkedTextHttpResponse: Gen[HttpResponse] =
    for {
      chunks   <- Gen.list(genAnyUnicodeString, Range.linear(0, MaxTextChunks))
      response <- genHttpResponseWithTextChunks(chunks)
    } yield response

  def genHttpResponseWithBinaryChunks(chunks: Seq[ByteString]): Gen[HttpResponse] =
    for {
      contentType <- genBinaryContentType
      response    <- genHttpResponseWithChunks(contentType, chunks)
    } yield response

  def genHttpResponseWithTextChunks(chunks: Seq[String]): Gen[HttpResponse] =
    for {
      contentType <- genNonBinaryOrMissingContentType
      response <- {
        val charset      = contentType.charsetOption.getOrElse(HttpCharsets.`UTF-8`).nioCharset
        val binaryChunks = chunks.map(ByteString(_, charset))
        genHttpResponseWithChunks(contentType, binaryChunks)
      }
    } yield response

  def genHttpResponseWithChunks(contentType: ContentType, chunks: Seq[ByteString]): Gen[HttpResponse] = {
    val source = Source(chunks)
    val entity = HttpEntity.Chunked.fromData(contentType, source)
    for {
      status <- if (entity.isKnownEmpty) genStatusCode else genStatusCodeFor(entity)
      // TODO: A chunked entity may have a last chunk which also has headers. What should be keeping these in sync?
      headers  <- genHttpHeaders
      protocol <- genHttpProtocolSupportingChunked
    } yield HttpResponse(status, headers, entity, protocol)
  }

  def genHttpResponse: Gen[HttpResponse] =
    for {
      status <- genStatusCode
      // TODO: A chunked entity may have a last chunk which also has headers. What should be keeping these in sync?
      headers  <- genHttpHeaders
      protocol <- genHttpProtocol
      entity   <- genResponseEntityFor(status, protocol)
    } yield HttpResponse(status, headers, entity, protocol)

  def genHttpResponseWithData(data: Seq[ByteString]): Gen[HttpResponse] =
    for {
      // TODO: A chunked entity may have a last chunk which also has headers. What should be keeping these in sync?
      headers  <- genHttpHeaders
      protocol <- genHttpProtocol
      entity   <- genResponseEntityWithDataFor(data, protocol)
      status   <- genStatusCodeFor(entity)
    } yield HttpResponse(status, headers, entity, protocol)

  def genHttpResponseWithEntity(entity: ResponseEntity): Gen[HttpResponse] =
    for {
      // TODO: A chunked entity may have a last chunk which also has headers. What should be keeping these in sync?
      headers  <- genHttpHeaders
      protocol <- genHttpProtocolFor(entity)
      status   <- genStatusCodeFor(entity)
    } yield HttpResponse(status, headers, entity, protocol)
}
