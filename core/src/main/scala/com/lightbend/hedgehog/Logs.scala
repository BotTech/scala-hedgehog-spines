package com.lightbend.hedgehog

import com.lightbend.hedgehog.Ranges._
import com.lightbend.hedgehog.implicits.RangeImplicits._
import hedgehog.Range
import hedgehog.core.ForAll
import org.scalactic.TripleEquals._

import scala.reflect.ClassTag

trait Logs {

  // scalastyle:off cyclomatic.complexity
  def logRange[A: Ordering](range: Range[A])(implicit tag: ClassTag[A]): ForAll = {
    val from   = range.origin !== range.x
    val frac   = classOf[Fractional[_]].isAssignableFrom(tag.runtimeClass)
    def origin = range.origin.toString
    def x      = range.x.toString
    def y      = range.y.toString
    val method = range.scalingMode match {
      case Constant if from       => s"constantFrom($origin, $x, $y)"
      case Constant               => s"constant($x, $y)"
      case Singleton              => s"singleton($x)"
      case Linear if frac && from => s"linearFracFrom($origin, $x, $y)"
      case Linear if from         => s"linearFrom($origin, $x, $y)"
      case Linear if frac         => s"linearFrac($x, $y)"
      case Linear                 => s"linear($x, $y)"
    }
    ForAll("range", s"Range.$method")
  }
  // scalastyle:on cyclomatic.complexity
}

object Logs extends Logs
