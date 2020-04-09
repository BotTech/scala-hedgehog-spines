package com.lightbend.hedgehog.generators.akka.http

import akka.http.scaladsl.model.{HttpEntity, HttpMethod, HttpMethods}
import com.lightbend.hedgehog.generators.Fields
import hedgehog.Gen

object HttpMethodGenerators {

  def genHttpMethod: Gen[HttpMethod] =
    Gen.elementUnsafe(Fields.fieldsOf(HttpMethods, classOf[HttpMethod]))

  def genHttpMethodAcceptingEntity: Gen[HttpMethod] =
    Gen.elementUnsafe(Fields.fieldsOf(HttpMethods, classOf[HttpMethod]).filter(_.isEntityAccepted))

  def genMethodFor(entity: HttpEntity): Gen[HttpMethod] =
    if (entity.isKnownEmpty) genHttpMethod
    else genHttpMethodAcceptingEntity
}
