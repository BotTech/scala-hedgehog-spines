package com.lightbend.hedgehog.implicits

import com.lightbend.hedgehog.implicits.GeneratorImplicits.GeneratorSyntax
import hedgehog.{Gen, Size}
import hedgehog.core.{Result, Seed}
import TreeImplicits._

trait GeneratorImplicits {

  implicit def generatorSyntax[A](gen: Gen[A]): GeneratorSyntax[A] = new GeneratorSyntax(gen)
}

object GeneratorImplicits {

  implicit class GeneratorSyntax[A](private val gen: Gen[A]) extends AnyVal {

    def widen[B >: A]: Gen[B] = gen.map(identity)

    def generates(size: Size, seed: Seed)(f: A => Result): Result =
      gen.run(size, seed).generated(f)
  }
}
