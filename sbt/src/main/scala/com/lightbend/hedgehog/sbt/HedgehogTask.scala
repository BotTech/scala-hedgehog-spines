package com.lightbend.hedgehog.sbt

import _root_.sbt.testing.{EventHandler, Logger, SubclassFingerprint, TaskDef, Task => SBTTask}
import com.lightbend.hedgehog.runner.{TestRunner, TestRunnerLike}
import hedgehog.core._
import hedgehog.sbt.Task

// Adapted from hedgehog.sbt.Task.

class HedgehogTask(taskDef: TaskDef, fingerprint: SubclassFingerprint, testClassLoader: ClassLoader)
    extends Task(taskDef, fingerprint, testClassLoader) {

  @SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf", "scalafix:DisableSyntax.asInstanceOf", "AsInstanceOf"))
  override def execute(eventHandler: EventHandler, loggers: Array[Logger]): Array[SBTTask] = {
    val config = PropertyConfig.default
    // TODO: Also allow the seed to be set by an argument.
    val seed      = System.nanoTime()
    val className = taskDef.fullyQualifiedName + (if (fingerprint.isModule) "$" else "")
    val c         = testClassLoader.loadClass(className).asInstanceOf[Class[TestRunner]]
    val testRunner =
      if (fingerprint.isModule)
        // FIX Use scala-reflect to be more future compatible
        c.getField("MODULE$").get(c).asInstanceOf[TestRunnerLike]
      else
        c.getDeclaredConstructor().newInstance()
    val manager = new TimedManager(taskDef, eventHandler, loggers)
    testRunner.runTests(className, testRunner.tests, config, seed, manager)
    Array()
  }
}
