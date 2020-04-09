package com.lightbend.hedgehog.sbt

import com.lightbend.hedgehog.runner.TestRunnerLike
import hedgehog.sbt.{Framework, Runner}
import sbt.testing.{Fingerprint, SubclassFingerprint}

// Adapted from hedgehog.sbt.Framework.

class HedgehogFramework extends Framework {

  override def fingerprints(): Array[Fingerprint] = {
    def mkFP(mod: Boolean, cname: String): SubclassFingerprint =
      new SubclassFingerprint {
        def superclassName(): String           = cname
        val isModule: Boolean                  = mod
        def requireNoArgConstructor(): Boolean = true
      }

    Array(
      mkFP(mod = false, classOf[TestRunnerLike].getName),
      mkFP(mod = true, classOf[TestRunnerLike].getName)
    )
  }

  override def runner(args: Array[String], remoteArgs: Array[String], testClassLoader: ClassLoader): Runner =
    new HedgehogRunner(args, remoteArgs, testClassLoader)
}
