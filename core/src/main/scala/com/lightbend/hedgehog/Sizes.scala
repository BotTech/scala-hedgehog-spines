package com.lightbend.hedgehog

import hedgehog.Size

trait Sizes {

  val MinSize: Size = Size(1)

  val MaxSize: Size = Size(Size.max)

  implicit object SizeOrdering extends Ordering[Size] {
    override def compare(x: Size, y: Size): Int = x.value.compare(y.value)
  }
}

object Sizes extends Sizes
