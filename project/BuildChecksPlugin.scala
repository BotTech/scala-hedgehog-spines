import com.sksamuel.scapegoat.sbt.ScapegoatSbtPlugin
import com.sksamuel.scapegoat.sbt.ScapegoatSbtPlugin.autoImport._
import explicitdeps.ExplicitDepsPlugin
import explicitdeps.ExplicitDepsPlugin.autoImport._
import io.github.davidgregory084.TpolecatPlugin
import net.vonbuchholtz.sbt.dependencycheck.DependencyCheckPlugin
import net.vonbuchholtz.sbt.dependencycheck.DependencyCheckPlugin.autoImport._
import org.scalafmt.sbt.ScalafmtPlugin
import org.scalafmt.sbt.ScalafmtPlugin.autoImport._
import org.scalastyle.sbt.ScalastylePlugin
import org.scalastyle.sbt.ScalastylePlugin.autoImport._
import sbt.Keys._
import sbt._
import scalafix.sbt.ScalafixPlugin
import scalafix.sbt.ScalafixPlugin.autoImport._
import scoverage.ScoverageSbtPlugin
import scoverage.ScoverageSbtPlugin.autoImport._
import wartremover.WartRemover
import wartremover.WartRemover.autoImport.{wartremoverExcluded, _}

object BuildChecksPlugin extends AutoPlugin {

  override def requires: Plugins =
    DependencyCheckPlugin &&
      ExplicitDepsPlugin &&
      ScalafixPlugin &&
      ScalafmtPlugin &&
      ScalastylePlugin &&
      ScapegoatSbtPlugin &&
      ScoverageSbtPlugin &&
      TpolecatPlugin &&
      WartRemover

  override def trigger: PluginTrigger = allRequirements

  override def globalSettings: Seq[Def.Setting[_]] =
    checkCommands ++
      scalafixGlobalSettings

  private def scalafixGlobalSettings = Seq(
    // TODO: Uncomment this once https://github.com/scalacenter/scalafix/issues/1033 is fixed.
    //scalafixConfig := Some((ThisBuild / baseDirectory).value / "config" / ".scalafix.conf")
  )

  override def buildSettings: Seq[Def.Setting[_]] =
    dependencyCheckBuildSettings ++
      scalafixBuildSettings ++
      scalafmtBuildSettings ++
      scapegoatBuildSettings ++
      scoverageBuildSettings

  private def dependencyCheckBuildSettings = Seq(
    // Add debug logging to dependencyCheck otherwise it is too quiet.
    dependencyCheck / logLevel := Level.Debug,
    dependencyCheckAggregate / logLevel := Level.Debug
  )

  private def scalafixBuildSettings = Seq(
    addCompilerPlugin(scalafixSemanticdb)
  )

  private def scalafmtBuildSettings = Seq(
    scalafmtConfig := (ThisBuild / baseDirectory).value / "config" / ".scalafmt.conf"
  )

  private def scapegoatBuildSettings = Seq(
    scapegoatVersion := "1.4.1"
  )

  private def scoverageBuildSettings = Seq(
    coverageMinimum := 80,
    coverageFailOnMinimum := true
  )

  override def projectSettings: Seq[Def.Setting[_]] =
    dependencyCheckProjectSettings ++
      explicitDependenciesProjectSettings ++
      scalafixProjectSettings ++
      scalastyleProjectSettings ++
      scapegoatProjectSettings ++
      tpolecatProjectSettings ++
      // silencerProjectSettings ++
      wartremoverProjectSettings

  private def dependencyCheckProjectSettings = Seq(
    // JUnit output is used by CircleCI.
    dependencyCheckFormats := Seq("HTML", "JUNIT"),
    dependencyCheckSuppressionFile := Some(
      (ThisBuild / baseDirectory).value / "config" / "dependency-check-suppressions.xml"
    )
  )

  private def explicitDependenciesProjectSettings = Seq(
    // Compiler plugins that use libraryDependencies show up as unused so we need to filter them out.
    // We should be able to just use -= but there is a bug:
    // https://github.com/cb372/sbt-explicit-dependencies/issues/33
    unusedCompileDependenciesFilter := unusedCompileDependenciesFilter.value -
      moduleFilter(ScapegoatSbtPlugin.GroupId, ScapegoatSbtPlugin.ArtifactId),
    // There is a contradiction in the explicit dependencies check.
    // Either it tells us that they are undeclared or unused.
    // https://github.com/cb372/sbt-explicit-dependencies/issues/46
    unusedCompileDependenciesFilter := unusedCompileDependenciesFilter.value -
      moduleFilter("com.lightbend.akka.grpc", "akka-grpc-runtime_2.13") -
      moduleFilter("io.grpc", "grpc-stub"),
    unusedCompileDependenciesFilter := unusedCompileDependenciesFilter.value
  )

  private def scalafixProjectSettings =
    Seq(
      scalacOptions ++= ScalafixOptions,
      Compile / console / scalacOptions := (Compile / console / scalacOptions).value.filterNot {
        ScalafixOptions.contains
      },
      Test / console / scalacOptions := (Test / console / scalacOptions).value.filterNot {
        ScalafixOptions.contains
      }
    )

  private val ScalafixOptions = Seq(
    "-Yrangepos",   // required by SemanticDB compiler plugin.
    "-Ywarn-unused" // required by `RemoveUnused` rule.
  )

  private def scalastyleProjectSettings = Seq(
    scalastyleFailOnWarning := true,
    scalastyleConfig := (ThisBuild / baseDirectory).value / "config" / "scalastyle-config.xml",
    Test / scalastyleConfig := (ThisBuild / baseDirectory).value / "config" / "scalastyle-config-test.xml"
  )

  private def scapegoatProjectSettings = Seq(
    // Add an extra sub-directory here so that CircleCI knows how to group the tests.
    scapegoatOutputPath := scapegoatOutputPath.value + "/scapegoat",
    scapegoatIgnoredFiles += (Compile / sourceManaged).value.getPath + ".*"
  )

  private def tpolecatProjectSettings = Seq(
    // TODO: Remove this once https://github.com/ghik/silencer/issues/43 is fixed.
    Compile / scalacOptions -= "-Xfatal-warnings"
  )

  // TODO: Use silencer once https://github.com/ghik/silencer/issues/43 is fixed.
  //private def silencerProjectSettings = {
  //  import Dependencies.CompilerPlugins.silencerLib
  //  addCompilerPlugin(Dependencies.CompilerPlugins.silencer) ++
  //    Seq(
  //      scalacOptions += "-P:silencer:checkUnused",
  //      libraryDependencies += silencerLib % Provided,
  //      unusedCompileDependenciesFilter -= moduleFilter(
  //        silencerLib.organization,
  //        silencerLib.name,
  //        silencerLib.revision
  //      )
  //    )
  //}

  private def wartremoverProjectSettings =
    Seq(
      Compile / compile / wartremoverErrors ++= Warts.unsafe,
      Test / compile / wartremoverErrors ++= Warts.unsafe,
      // sbt-protoc generates particularly "unsafe" code:
      // https://github.com/thesamet/sbt-protoc/issues/124.
      wartremoverExcluded += (Compile / sourceManaged).value,
      Test / wartremoverExcluded += (Test / sourceManaged).value
    ) ++
      wartremoverTestSettings(Test, compile)

  private def wartremoverTestSettings(config: Configuration, task: Scoped) =
    inConfig(config)(
      inTask(task)(
        Seq(
          wartremoverErrors := (Compile / compile / wartremoverErrors).value.filterNot { error =>
            // This error is really annoying with ScalaMock.
            error == Wart.NonUnitStatements ||
            // This error is also another really annoying one with ScalaMock although
            // I don't know why it happens.
            error == Wart.Any ||
            error == Wart.Null
          }
        )
      )
    )

  private def checkCommands = {
    def scopedCommands(command: String => String) =
      Seq(Compile, Test)
        .map(config => command(config.name))
        .mkString(" ")
    addCommandAlias(
      "allDependencyChecks",
      "; dependencyCheckAggregate; undeclaredCompileDependenciesTest; unusedCompileDependenciesTest"
    ) ++
      addCommandAlias(
        "scalafixChecks",
        scopedCommands(name => s"; all ${name}:scalafix --check")
      ) ++
      addCommandAlias(
        "scalastyleChecks",
        scopedCommands(name => s"; ${name}:scalastyle")
      ) ++
      addCommandAlias(
        "scalafmtChecks",
        "; scalafmtCheckAll; scalafmtSbtCheck"
      ) ++
      addCommandAlias(
        "allStyleChecks",
        "; scalafixChecks; scalastyleChecks; scalafmtChecks; scapegoat;"
      ) ++
      addCommandAlias(
        "allChecks",
        "; allDependencyChecks; allStyleChecks"
      )
  }
}
