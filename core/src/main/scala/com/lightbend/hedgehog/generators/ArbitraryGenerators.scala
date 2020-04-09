package com.lightbend.hedgehog.generators

import hedgehog.Gen
import org.scalactic.TripleEquals._

object ArbitraryGenerators {

  def genFrequency[A, B](a: A, b: A)(gen: (A, A) => Gen[B])(implicit n: Numeric[A]): (Int, Gen[B]) = {
    import n._
    @SuppressWarnings(Array("org.wartremover.warts.Throw", "scalafix:DisableSyntax.throw"))
    def checkForOverflow(a: A, b: A, c: A): Unit =
      if ((sign(a) !== sign(b)) && (sign(a) !== sign(c))) throw new ArithmeticException("integer overflow")
    val lo             = min(a, b)
    val hi             = max(a, b)
    val subtractResult = hi - lo
    checkForOverflow(hi, lo, subtractResult)
    val addResult = subtractResult + one
    checkForOverflow(addResult, subtractResult, one)
    toInt(addResult) -> gen(lo, hi)
  }

  def genElementFrequency[A](x: A, xs: A*): (Int, Gen[A]) =
    xs.length + 1 -> Gen.element1(x, xs: _*)

  def genElementFrequencyUnsafe[A](xs: Iterable[A]): (Int, Gen[A]) =
    xs.size -> Gen.elementUnsafe(xs.toList)
}
