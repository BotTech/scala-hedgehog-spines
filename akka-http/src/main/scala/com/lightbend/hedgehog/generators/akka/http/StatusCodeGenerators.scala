package com.lightbend.hedgehog.generators.akka.http

import akka.http.scaladsl.model.{HttpEntity, StatusCode, StatusCodes}
import com.lightbend.hedgehog.generators.Fields
import hedgehog.Gen

object StatusCodeGenerators {

  // TODO: Custom status codes.

  def genStatusCode: Gen[StatusCode] =
    Gen.elementUnsafe(Fields.fieldsOf(StatusCodes, classOf[StatusCode]))

  def genStatusCodeAllowingEntity: Gen[StatusCode] =
    Gen.elementUnsafe(Fields.fieldsOf(StatusCodes, classOf[StatusCode]).filter(_.allowsEntity))

  def genStatusCodeFor(entity: HttpEntity): Gen[StatusCode] =
    if (entity.isKnownEmpty) genStatusCode else genStatusCodeAllowingEntity
}
