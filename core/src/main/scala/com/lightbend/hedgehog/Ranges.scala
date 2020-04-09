package com.lightbend.hedgehog

object Ranges {

  sealed trait ScalingMode
  sealed trait Constant       extends ScalingMode
  final case object Constant  extends ScalingMode
  final case object Singleton extends Constant
  final case object Linear    extends ScalingMode
}
