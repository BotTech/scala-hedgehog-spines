package com.lightbend.hedgehog.implicits

import com.lightbend.hedgehog.implicits.CollectionImplicits.IterableOnceSyntax
import com.lightbend.hedgehog.implicits.ResultImplicits.ResultSyntax
import hedgehog.Result

import scala.annotation.tailrec

trait CollectionImplicits {

  implicit def iterableOnceSyntax[A](it: IterableOnce[A]): IterableOnceSyntax[A] = new IterableOnceSyntax(it)
}

object CollectionImplicits {

  implicit class IterableOnceSyntax[A](private val it: IterableOnce[A]) extends AnyVal {

    def forAll(f: A => Result): Result = {
      @tailrec
      def loop(list: List[A], result: Result): Result = list match {
        case Nil => result
        case head :: tail =>
          f(head) match {
            case Result.Success          => loop(tail, result)
            case failure: Result.Failure => failure
          }
      }
      val list   = it.iterator.toList
      val result = Result.success
      loop(list, result)
        .prependLogs(
          "=== For All ===",
          "--- in ---",
          list.mkString(",")
        )
    }
  }
}
