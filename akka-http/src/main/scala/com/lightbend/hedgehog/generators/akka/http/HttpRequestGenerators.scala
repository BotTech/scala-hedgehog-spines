package com.lightbend.hedgehog.generators.akka.http

import akka.http.scaladsl.model.{HttpRequest, RequestEntity}
import com.lightbend.hedgehog.generators.akka.http.HttpHeaderGenerators._
import com.lightbend.hedgehog.generators.akka.http.HttpMethodGenerators._
import com.lightbend.hedgehog.generators.akka.http.HttpProtocolGenerators._
import com.lightbend.hedgehog.generators.akka.http.RequestEntityGenerators._
import com.lightbend.hedgehog.generators.akka.http.UriGenerators._
import hedgehog.Gen

object HttpRequestGenerators {

  def genHttpRequest: Gen[HttpRequest] =
    for {
      method <- genHttpMethod
      uri    <- genHttpUri
      // TODO: A chunked entity may have a last chunk which also has headers. What should be keeping these in sync?
      headers  <- genHttpHeaders
      protocol <- genHttpProtocol
      entity   <- genRequestEntityFor(method, protocol)
    } yield HttpRequest(method, uri, headers, entity, protocol)

  def genHttpRequestWithEntity(entity: RequestEntity): Gen[HttpRequest] =
    for {
      uri <- genHttpUri
      // TODO: A chunked entity may have a last chunk which also has headers. What should be keeping these in sync?
      headers  <- genHttpHeaders
      method   <- genMethodFor(entity)
      protocol <- genHttpProtocolFor(entity)
    } yield HttpRequest(method, uri, headers, entity, protocol)
}
