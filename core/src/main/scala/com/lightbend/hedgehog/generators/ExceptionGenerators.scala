package com.lightbend.hedgehog.generators

import com.lightbend.hedgehog.generators.StringGenerators.genAnyUnicodeString
import hedgehog.{Gen, Size}

@SuppressWarnings(Array("org.wartremover.warts.Null"))
object ExceptionGenerators {

  def genException: Gen[Exception] =
    for {
      message            <- genAnyUnicodeString.option
      cause              <- genException.option.scale(size => Size(size.value / 2))
      enableSuppression  <- Gen.boolean
      writableStackTrace <- Gen.boolean
    } yield new Exception(message.orNull, cause.orNull, enableSuppression, writableStackTrace) {}
}
