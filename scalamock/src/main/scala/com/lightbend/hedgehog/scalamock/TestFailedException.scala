package com.lightbend.hedgehog.scalamock

final case class TestFailedException(message: String, cause: Throwable) extends Exception(message, cause)

// scalastyle:off null
@SuppressWarnings(Array("org.wartremover.warts.Null", "scalafix:DisableSyntax.null", "NullParameter"))
object TestFailedException {

  def apply(message: String): TestFailedException = TestFailedException(message, null)
}
// scalastyle:on null
