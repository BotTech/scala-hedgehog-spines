package com.lightbend.hedgehog.implicits

import com.lightbend.hedgehog.implicits.ArbitraryImplicits.ExtendedSyntax
import com.lightbend.hedgehog.implicits.ResultImplicits.ResultSyntax
import hedgehog.Result
import org.scalactic.TripleEquals._

import scala.reflect.ClassTag

trait ArbitraryImplicits {

  implicit def extendedSyntax[A](a: A): ExtendedSyntax[A] = new ExtendedSyntax(a)
}

object ArbitraryImplicits {

  implicit class ExtendedSyntax[A](private val a1: A) extends AnyVal {

    @SuppressWarnings(Array("AvoidOperatorOverload"))
    def !===(a2: A): Result = // scalastyle:ignore
      Result.diffNamed("=== Equal ===", a1, a2)(_ !== _)

    @SuppressWarnings(
      Array("org.wartremover.warts.AsInstanceOf", "scalafix:DisableSyntax.asInstanceOf", "AsInstanceOf")
    )
    def isA[B](implicit tag: ClassTag[B]): Result = {
      val clazz: Class[B] = tag.runtimeClass.asInstanceOf[Class[B]]
      Result.diffNamed("=== Not A ===", a1.getClass, clazz) {
        case (aClazz, bClazz) => bClazz.isAssignableFrom(aClazz)
      }
    }

    @SuppressWarnings(
      Array("org.wartremover.warts.AsInstanceOf", "scalafix:DisableSyntax.asInstanceOf", "AsInstanceOf")
    )
    def asA[B](result: B => Result)(implicit tag: ClassTag[B]): Result =
      isA[B] match {
        case Result.Success => result(a1.asInstanceOf[B])
        case failure        => failure
      }

    def lessThan(a2: A)(implicit ordering: Ordering[A]): Result =
      Result.diffNamed("=== Not Less Than ===", a1, a2)(ordering.lt)

    def lessThanOrEqual(a2: A)(implicit ordering: Ordering[A]): Result =
      Result.diffNamed("=== Not Less Than Or Equal ===", a1, a2)(ordering.lteq)

    def greaterThan(a2: A)(implicit ordering: Ordering[A]): Result =
      Result.diffNamed("=== Not Greater Than ===", a1, a2)(ordering.gt)

    def greaterThanOrEqual(a2: A)(implicit ordering: Ordering[A]): Result =
      Result.diffNamed("=== Not Greater Than Or Equal ===", a1, a2)(ordering.gteq)

    def within(a2: A, a3: A)(implicit ordering: Ordering[A]): Result = {
      val (min, max) = sort(a2, a3)
      greaterThan(min)
        .and(lessThan(max))
        .prependLogs(
          "=== Not Within ===",
          "--- bounds ---",
          s"${a2.toString},${a3.toString}"
        )
    }

    def without(a2: A, a3: A)(implicit ordering: Ordering[A]): Result = {
      val (min, max) = sort(a2, a3)
      lessThan(min)
        .or(greaterThan(max))
        .prependLogs(
          "=== Not Without ===",
          "--- bounds ---",
          s"${a2.toString},${a3.toString}"
        )
    }

    def inside(a2: A, a3: A)(implicit ordering: Ordering[A]): Result = {
      val (min, max) = sort(a2, a3)
      greaterThanOrEqual(min)
        .and(lessThanOrEqual(max))
        .prependLogs(
          "=== Not Inside ===",
          "--- bounds ---",
          s"${a2.toString},${a3.toString}"
        )
    }

    def outside(a2: A, a3: A)(implicit ordering: Ordering[A]): Result = {
      val (min, max) = sort(a2, a3)
      lessThanOrEqual(min)
        .or(greaterThanOrEqual(max))
        .prependLogs(
          "=== Not Outside ===",
          "--- bounds ---",
          s"${a2.toString},${a3.toString}"
        )
    }
  }

  private def sort[A](a: A, b: A)(implicit ordering: Ordering[A]): (A, A) = {
    import ordering._
    if (a <= b) (a, b) else (b, a)
  }
}
