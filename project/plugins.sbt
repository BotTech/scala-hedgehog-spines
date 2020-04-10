// Style plugins.
addSbtPlugin("ch.epfl.scala"             % "sbt-scalafix"           % "0.9.12")
addSbtPlugin("com.sksamuel.scapegoat"    %% "sbt-scapegoat"         % "1.1.0")
addSbtPlugin("io.github.davidgregory084" % "sbt-tpolecat"           % "0.1.11")
addSbtPlugin("org.scalameta"             % "sbt-scalafmt"           % "2.3.2")
addSbtPlugin("org.scalastyle"            %% "scalastyle-sbt-plugin" % "1.0.0")
addSbtPlugin("org.wartremover"           % "sbt-wartremover"        % "2.4.5")

// Dependency plugins.
addSbtPlugin("com.github.cb372" % "sbt-explicit-dependencies" % "0.2.13")
addSbtPlugin("net.vonbuchholtz" % "sbt-dependency-check"      % "2.0.0")

// Testing plugins.
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.6.1")

// Distribution plugins.
addSbtPlugin("com.dwijnand"      % "sbt-dynver"   % "4.0.0")
addSbtPlugin("org.foundweekends" % "sbt-bintray"  % "0.5.6")
addSbtPlugin("com.jsuereth"      % "sbt-pgp"      % "2.0.0")
addSbtPlugin("org.xerial.sbt"    % "sbt-sonatype" % "3.9.2")
