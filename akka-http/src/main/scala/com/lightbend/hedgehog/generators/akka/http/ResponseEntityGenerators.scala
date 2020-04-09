package com.lightbend.hedgehog.generators.akka.http

import akka.NotUsed
import akka.http.scaladsl.model.HttpEntity.CloseDelimited
import akka.http.scaladsl.model.{HttpProtocol, HttpProtocols, ResponseEntity, StatusCode}
import akka.stream.scaladsl.Source
import akka.util.ByteString
import com.lightbend.hedgehog.generators.CollectionGenerators.genList
import com.lightbend.hedgehog.generators.akka.ByteStringGenerators._
import com.lightbend.hedgehog.generators.akka.http.ContentTypeGenerators._
import com.lightbend.hedgehog.generators.akka.http.RequestEntityGenerators._
import com.lightbend.hedgehog.implicits.GeneratorImplicits._
import hedgehog.{Gen, Range}
import org.scalactic.TripleEquals._

object ResponseEntityGenerators {

  private val MaxByteStrings = 100

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  def genCloseDelimitedResponseEntity: Gen[CloseDelimited] =
    genByteStringSource.flatMap(genCloseDelimitedResponseEntityWithData)

  def genCloseDelimitedResponseEntityWithData(data: Source[ByteString, Any]): Gen[CloseDelimited] =
    genContentType.map(CloseDelimited(_, data))

  def genEmptyCloseDelimitedResponseEntity: Gen[CloseDelimited] =
    for {
      contentType <- genContentType
    } yield CloseDelimited(contentType, Source.empty)

  def genResponseEntity: Gen[ResponseEntity] =
    Gen.choice1(genCloseDelimitedResponseEntity.widen, genRequestEntity.widen)

  def genResponseEntityWithData(data: Seq[ByteString]): Gen[ResponseEntity] =
    Gen.choice1(
      genCloseDelimitedResponseEntityWithData(Source(data)).widen,
      genRequestEntityWithData(data).widen
    )

  def genEmptyResponseEntity: Gen[ResponseEntity] =
    Gen.choice1(genEmptyCloseDelimitedResponseEntity.widen, genEmptyRequestEntity.widen)

  def genResponseEntityFor(status: StatusCode, protocol: HttpProtocol): Gen[ResponseEntity] =
    if (!status.allowsEntity) genEmptyResponseEntity.widen
    else if (protocol === HttpProtocols.`HTTP/1.0`) genUniversalEntity.widen
    else genResponseEntity

  def genResponseEntityWithDataFor(data: Seq[ByteString], protocol: HttpProtocol): Gen[ResponseEntity] =
    if (data.isEmpty) genEmptyResponseEntity.widen
    else if (protocol === HttpProtocols.`HTTP/1.0`) genUniversalEntityWithData(data).widen
    else genResponseEntityWithData(data)

  // TODO: Generate infinite sources.
  private def genByteStringSource: Gen[Source[ByteString, NotUsed]] =
    genList(genByteString, Range.linear(0, MaxByteStrings)).map(Source(_))
}
