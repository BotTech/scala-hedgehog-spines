package com.lightbend.hedgehog.generators

import hedgehog.Gen

object OptionGenerators {

  def genSome[A](gen: Gen[A]): Gen[Some[A]] =
    gen.map(Some(_))

  def genNone: Gen[None.type] =
    Gen.constant(None)

  def genOption[A](gen: Gen[A]): Gen[Option[A]] =
    gen.option
}
