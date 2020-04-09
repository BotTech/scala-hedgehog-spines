package com.lightbend.hedgehog.generators.akka.http

import akka.http.scaladsl.model.HttpHeader
import akka.http.scaladsl.model.headers.RawHeader
import com.lightbend.hedgehog.generators.CollectionGenerators.genCollection
import com.lightbend.hedgehog.generators.StringGenerators._
import hedgehog.{Gen, Range}

object HttpHeaderGenerators {

  private val MaxHeaders = 100

  // TODO: Come up with a way of generating real headers.

  def genHttpHeader: Gen[HttpHeader] =
    for {
      name  <- genAnyUnicodeString
      value <- genAnyUnicodeString
    } yield RawHeader(name, value)

  def genHttpHeaders: Gen[Seq[HttpHeader]] =
    genCollection[HttpHeader, Seq](genHttpHeader, Range.linear(0, MaxHeaders))
}
