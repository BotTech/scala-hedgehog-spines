package com.lightbend.hedgehog.implicits

import com.lightbend.hedgehog.implicits.OptionImplicits.OptionSyntax
import hedgehog.Result

trait OptionImplicits {

  implicit def optionSyntax[A](opt: Option[A]): OptionSyntax[A] = new OptionSyntax(opt)
}

object OptionImplicits {

  implicit class OptionSyntax[A](private val opt: Option[A]) extends AnyVal {

    def isSome(f: A => Result): Result = {
      val defined = Result
        .assert(opt.isDefined)
        .log("=== None ===")
        .log(opt.toString)
      opt match {
        case Some(value) => defined.and(f(value))
        case None        => defined
      }
    }
  }
}
