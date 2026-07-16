name := "PPS-25-wizard"
version := "0.1"

val scala3Version = "3.8.3"

val scalatestVersion      = "3.2.20"
val catsVersion           = "2.13.0"
val scalafxVersion        = "26.0.0-R38"
val twelvemonkeysVersion  = "3.13.1"
val vertxVersion          = "5.1.3"
val tuPrologVersion       = "4.1.1"

ThisBuild / scalaVersion := scala3Version
ThisBuild / scalacOptions := Seq("-Wunused:all", "-Wunused:imports", "-Werror", "-language:implicitConversions")

ThisBuild / scalafixDependencies += "org.typelevel" %% "typelevel-scalafix" % "0.2.0"

ThisBuild / libraryDependencies ++= Seq(
  "org.typelevel"             %%    "cats-core"               % catsVersion,
  "org.scalafx"               %%    "scalafx"                 % scalafxVersion,
  "com.twelvemonkeys.imageio"  %    "imageio-webp"            % twelvemonkeysVersion,
  "org.scalatest"             %%    "scalatest"               % scalatestVersion % Test,
  "io.vertx"                   %    "vertx-core"              % vertxVersion,
  "it.unibo.alice.tuprolog"    % "2p-core"                   % tuPrologVersion
)

lazy val root = (project in file("."))
  .enablePlugins(WartRemover)
  .settings(
    coverageExcludedPackages :=
      "<empty>;" +
        "it\\.unibo\\.pps\\.wizard\\.Main;" +
        "it\\.unibo\\.pps\\.wizard\\.application\\..*;" +
        "it\\.unibo\\.pps\\.wizard\\.engine\\.adapters\\..*;" +
        "it\\.unibo\\.pps\\.wizard\\.engine\\.events\\..*",

    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision,
//    wartremoverErrors ++= Warts.unsafe,
  )
