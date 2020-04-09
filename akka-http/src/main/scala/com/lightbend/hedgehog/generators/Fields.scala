package com.lightbend.hedgehog.generators

import scala.reflect.ClassTag
import scala.reflect.runtime.universe._

// TODO: This should go in the hedgehog projects but I can't be bothered testing it right now.
object Fields {

  @SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf", "scalafix:DisableSyntax.asInstanceOf", "AsInstanceOf"))
  def fieldsOf[A, B](obj: A, fieldClass: Class[B])(implicit objTag: ClassTag[A]): List[B] = {
    val runtimeMirror = scala.reflect.runtime.currentMirror
    val field         = runtimeMirror.classSymbol(fieldClass).toType
    val vals = runtimeMirror.classSymbol(obj.getClass).toType.members.collect {
      case term: TermSymbol if term.isVal && term.typeSignature.resultType <:< field => term
    }
    val instanceMirror = runtimeMirror.reflect(obj)
    val x = vals.map { term =>
      instanceMirror.reflectField(term).get.asInstanceOf[B]
    }.toList
    x
  }
}
