import sbt._

object Dependencies {

  object Versions {

    val akka       = "2.6.4"
    val akkaHttp   = "10.1.11"
    val cats       = "2.1.1"
    val catsEffect = "2.1.2"
    val hedgehog   = "97854199ef795a5dfba15478fd9abe66035ddea2"
    val sbtTest    = "1.0"
    val scalaMock  = "4.4.0"
    val scalaTest  = "3.1.1"
    val scopt      = "4.0.0-RC2"
  }

  val akkaActor  = "com.typesafe.akka" %% "akka-actor"  % Versions.akka
  val akkaHttp   = "com.typesafe.akka" %% "akka-http"   % Versions.akkaHttp
  val akkaStream = "com.typesafe.akka" %% "akka-stream" % Versions.akka
  val cats       = "org.typelevel"     %% "cats-core"   % Versions.cats
  val catsEffect = "org.typelevel"     %% "cats-effect" % Versions.catsEffect
  val scopt      = "com.github.scopt"  %% "scopt"       % Versions.scopt

  def scalaCompiler(scalaVersion: String): ModuleID = scalaLib("compiler", scalaVersion)

  def scalaReflect(scalaVersion: String): ModuleID = scalaLib("reflect", scalaVersion)

  def scalaLib(component: String, scalaVersion: String): ModuleID =
    "org.scala-lang" % s"scala-$component" % scalaVersion

  object CompilerPlugins {

    object Versions {

      val kindProjector = "0.11.0"
    }

    val kindProjector = ("org.typelevel" %% "kind-projector" % Versions.kindProjector).cross(CrossVersion.full)
  }

  object Testing {

    val akkaActorTestkit = "com.typesafe.akka" %% "akka-actor-testkit-typed" % Versions.akka
    val hedgehogCore     = "qa.hedgehog"       %% "hedgehog-core"            % Versions.hedgehog
    val hedgehogRunner   = "qa.hedgehog"       %% "hedgehog-runner"          % Versions.hedgehog
    val hedgehogSbt      = "qa.hedgehog"       %% "hedgehog-sbt"             % Versions.hedgehog
    val sbtTest          = "org.scala-sbt"     % "test-interface"            % Versions.sbtTest
    val scalactic        = "org.scalactic"     %% "scalactic"                % Versions.scalaTest
    val scalaMock        = "org.scalamock"     %% "scalamock"                % Versions.scalaMock
  }

  object Resolvers {

    val hedgehog = Resolver.bintrayRepo("hedgehogqa", "scala-hedgehog")
  }
}
