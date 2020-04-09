package com.lightbend.hedgehog.macros

import scala.annotation.tailrec
import scala.reflect.macros.blackbox

object CharacterMacros {

  def filterRange(lo: Char, hi: Char)(f: Char => Boolean): List[(Char, Char)] =
    macro filterRangeImpl

  def filterRanges(ranges: List[(Char, Char)])(f: Char => Boolean): List[(Char, Char)] =
    macro filterRangesImpl

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  def filterRangeImpl(
      c: blackbox.Context
    )(
      lo: c.Expr[Char],
      hi: c.Expr[Char]
    )(
      f: c.Expr[Char => Boolean]
    ): c.Expr[List[(Char, Char)]] = {
    import c.universe._
    val loChar = c.eval(lo)
    val hiChar = c.eval(hi)
    if (hiChar < loChar) c.abort(c.enclosingPosition, "lo must be <= hi.")
    val p      = evalUntyped(c)(f)
    val result = filter(loChar, hiChar, p)
    c.Expr(q"$result")
  }

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  def filterRangesImpl(
      c: blackbox.Context
    )(
      ranges: c.Expr[List[(Char, Char)]]
    )(
      f: c.Expr[Char => Boolean]
    ): c.Expr[List[(Char, Char)]] = {
    import c.universe._
    val r = evalUntyped(c)(ranges)
    val p = evalUntyped(c)(f)
    val result = r.flatMap {
      case (lo, hi) => filter(lo, hi, p)
    }
    c.Expr(q"$result")
  }

  private def evalUntyped[A](c: blackbox.Context)(a: c.Expr[A]): A =
    c.eval(cloneUntyped(c)(a))

  @SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
  private def cloneUntyped[A](c: blackbox.Context)(a: c.Expr[A]): c.Expr[A] =
    c.Expr[A](c.untypecheck(a.tree.duplicate))

  private def filter(lo: Char, hi: Char, f: Char => Boolean) = {
    @tailrec
    def loop(next: Char, acc: List[(Char, Char)]): List[(Char, Char)] =
      (next to hi).find(f) match {
        case None => acc
        case Some(start) =>
          (start to hi).find(x => !f(x)) match {
            case None => start -> hi :: acc
            case Some(end) =>
              val nextAcc = start -> (end - 1).toChar :: acc
              if (end == hi) nextAcc
              else loop((end + 1).toChar, nextAcc)
          }
      }
    loop(lo, Nil)
  }
}
