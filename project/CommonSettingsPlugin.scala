import sbt.Keys._
import sbt._
import sbtdynver.DynVerPlugin.autoImport.{dynver, dynverGitDescribeOutput}

object CommonSettingsPlugin extends AutoPlugin {

  override def trigger: PluginTrigger = allRequirements

  override def buildSettings: Seq[Def.Setting[_]] = Seq(
    organization := "com.lightbend",
    version := dynverGitDescribeOutput.value.mkVersion(versionFmt, "latest"),
    dynver := sbtdynver.DynVer
      .getGitDescribeOutput(new java.util.Date)
      .mkVersion(versionFmt, "latest"),
    organizationName := "Lightbend Inc.",
    organizationHomepage := Some(url("https://github.com/lightbend/scala-hedgehog-spines")),
    //noinspection ScalaStyle
    startYear := Some(2020),
    scmInfo := Some(
      ScmInfo(
        url("https://github.com/lightbend/scala-hedgehog-spines"),
        "scm:git@github.com:lightbend/scala-hedgehog-spines.git"
      )
    ),
    scalaVersion := "2.13.1",
    scalacOptions += "-Ymacro-annotations"
  )

  private def versionFmt(out: sbtdynver.GitDescribeOutput): String = {
    val dirtySuffix = if (out.isDirty()) "-dev" else ""
    if (out.isCleanAfterTag) {
      out.ref.dropV.value
    } else {
      out.ref.dropV.value + out.commitSuffix.mkString("-", "-", "") + dirtySuffix
    }
  }

  override def projectSettings: Seq[Def.Setting[_]] =
    addCompilerPlugin(Dependencies.CompilerPlugins.kindProjector) ++
      testProjectSettings

  private def testProjectSettings =
    Defaults.itSettings ++ Seq(
      resolvers += Dependencies.Resolvers.hedgehog,
      // Remove Specs2, ScalaTest, and JUnit frameworks as we don't use them.
      // ScalaTest is particularly noisy as it shows output even if there are no tests.
      // We use a custom Framework for Hedgehog because the default one doesn't run tests in the
      // same was that running them as a main class does. We need it to call runTests instead.
      testFrameworks := Seq(TestFramework("com.lightbend.hedgehog.sbt.HedgehogFramework"))
    ) ++
      inConfig(Test)(rawTestSettings)

  private def rawTestSettings = Seq(
    fork := true,
    // If we have flaky tests because the timeout is too small or we generated unexpectedly large
    // values, then we should wait longer to see what will happen. Maybe we need to increase the
    // timeout in the test.
    javaOptions += "-Dhedgehog.future.diagnosisDuration=5min",
    parallelExecution := false
  )
}
