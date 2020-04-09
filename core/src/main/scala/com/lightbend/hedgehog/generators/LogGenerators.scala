package com.lightbend.hedgehog.generators

import com.lightbend.hedgehog.generators.ExceptionGenerators._
import com.lightbend.hedgehog.generators.StringGenerators.genAnyUnicodeString
import hedgehog.Gen
import hedgehog.core.{Error, ForAll, Info, Log, Name}

object LogGenerators {

  def genName: Gen[Name] = genAnyUnicodeString.map(Name(_))

  def genForAll: Gen[ForAll] =
    for {
      name  <- genName
      value <- genAnyUnicodeString
    } yield ForAll(name, value)

  def genInfo: Gen[Info] = genAnyUnicodeString.map(Info)

  def genError: Gen[Error] = genException.map(Error)

  def genLog: Gen[Log] =
    Gen.choice1[Log](genInfo.map(identity), genError.map(identity), genForAll.map(identity))
}
