package com.lightbend.hedgehog.generators.akka.http

import akka.http.scaladsl.model.{HttpEntity, HttpProtocol, HttpProtocols}
import com.lightbend.hedgehog.generators.Fields
import hedgehog.Gen
import org.scalactic.TripleEquals._

object HttpProtocolGenerators {

  def genHttpProtocol: Gen[HttpProtocol] =
    Gen.elementUnsafe(Fields.fieldsOf(HttpProtocols, classOf[HttpProtocol]))

  def genHttpProtocolSupportingChunked: Gen[HttpProtocol] =
    Gen.elementUnsafe(Fields.fieldsOf(HttpProtocols, classOf[HttpProtocol]).filter(_ !== HttpProtocols.`HTTP/1.0`))

  def genHttpProtocolFor(entity: HttpEntity): Gen[HttpProtocol] =
    entity match {
      case _: HttpEntity.Chunked => genHttpProtocolSupportingChunked
      case _                     => genHttpProtocol
    }
}
