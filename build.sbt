import Dependencies._
import Dependencies.Testing._

lazy val root = (project in file("."))
  .aggregate(
    akka,
    `akka-http`,
    cats,
    core,
    macros,
    runner,
    sbtTestFramework,
    scalamock,
    testkit,
    tests
  )
  .settings(
    coverageMinimum := 60,
    skip in publish := true
  )

lazy val akka = project
  .dependsOn(core, runner)
  .settings(
    libraryDependencies ++= Seq(
      akkaActor,
      akkaTestkit,
      Dependencies.config,
      hedgehogCore,
      scalactic,
      scalaReflect(scalaVersion.value)
    ),
    coverageMinimum := 0
  )

lazy val `akka-http` = project
  .dependsOn(akka, core, runner)
  .settings(
    libraryDependencies ++= Seq(
      akkaActor,
      akkaHttpCore,
      akkaStream,
      hedgehogCore,
      scalactic,
      scalaReflect(scalaVersion.value)
    ),
    coverageMinimum := 0
  )

lazy val cats = project
  .dependsOn(core)
  .settings(
    libraryDependencies ++= Seq(
      catsEffect,
      hedgehogCore
    ),
    coverageMinimum := 0
  )

lazy val core = project
  .dependsOn(macros)
  .settings(
    libraryDependencies ++=
      Seq(
        hedgehogCore,
        scalactic
      ),
    coverageMinimum := 94
  )

lazy val macros = project
  .settings(
    libraryDependencies ++= Seq(
      scalactic,
      scalaReflect(scalaVersion.value)
    )
  )

lazy val runner = project
  .settings(
    libraryDependencies ++=
      Seq(
        hedgehogCore,
        hedgehogRunner,
        scalactic,
        scopt
      ),
    coverageMinimum := 0
  )

lazy val sbtTestFramework = (project in file("sbt"))
  .dependsOn(runner)
  .settings(
    libraryDependencies ++=
      Seq(
        hedgehogCore,
        hedgehogRunner,
        hedgehogSbt,
        sbtTest
      ),
    coverageMinimum := 0
  )

lazy val scalamock = project
  .dependsOn(runner, testkit)
  .settings(
    libraryDependencies ++=
      Seq(
        hedgehogCore,
        hedgehogRunner,
        scalaMock
      ),
    coverageMinimum := 0
  )

lazy val testkit = project
  .dependsOn(core)
  .settings(
    libraryDependencies ++=
      Seq(
        hedgehogCore,
        hedgehogRunner
      ),
    coverageMinimum := 0
  )

lazy val tests = project
  .dependsOn(sbtTestFramework % Test, scalamock % Test)
  .settings(
    Test / javaOptions := Seq.empty
  )
