package com.lightbend.hedgehog.runner

import akka.testkit.{TestKit, TestKitBase}
import com.lightbend.hedgehog.implicits.FutureImplicits
import com.typesafe.config
import com.typesafe.config.ConfigFactory

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration

abstract class AkkaTestRunner extends TestRunner with TestKitBase with BeforeAndAfterAll with FutureImplicits {

  // Prevent the application.conf being as it has variables that may not be resolved.
  protected lazy val NoAppConfig: config.Config = ConfigFactory.load(ConfigFactory.empty())

  // TODO: Add some sort of scaling like with ScalaTest.
  protected val ShortTimeout: Duration  = 150.milliseconds
  protected val MediumTimeout: Duration = 2.seconds
  protected val LongTimeout: Duration   = 15.seconds

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  protected def awaitStartup(future: Future[Any]): Unit = {
    val _ = Await.ready(future, ShortTimeout)
  }

  override protected def afterAll(): Unit =
    TestKit.shutdownActorSystem(system)
}
