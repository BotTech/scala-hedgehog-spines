package com.lightbend.hedgehog.sbt

import hedgehog.sbt.Runner
import sbt.testing.{SubclassFingerprint, Task, TaskDef}

// Adapted from hedgehog.sbt.Runner.

class HedgehogRunner(args: Array[String], remoteArgs: Array[String], testClassLoader: ClassLoader)
    extends Runner(args, remoteArgs, testClassLoader) {

  @SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf", "scalafix:DisableSyntax.asInstanceOf", "AsInstanceOf"))
  override def tasks(taskDefs: Array[TaskDef]): Array[Task] =
    taskDefs.map { td =>
      try {
        Some(new HedgehogTask(td, td.fingerprint.asInstanceOf[SubclassFingerprint], testClassLoader))
      } catch {
        case _: ClassCastException => None
      }
    }.flatMap(_.toList)
}
