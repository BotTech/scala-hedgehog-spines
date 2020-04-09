package com.lightbend.hedgehog.generators.akka.http

import akka.http.scaladsl.model.{HttpCharset, HttpCharsets}
import com.lightbend.hedgehog.generators.Fields
import hedgehog.Gen

object HttpCharsetGenerators {

  def genPredefinedHttpCharsets: Gen[HttpCharset] =
    Gen.elementUnsafe(Fields.fieldsOf(HttpCharsets, classOf[HttpCharset]))

  // TODO: Generate other media types.

  def genHttpCharsets: Gen[HttpCharset] =
    Gen.choice1(genPredefinedHttpCharsets)
}
