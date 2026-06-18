val scala3Version = "3.8.3"

val scalatestVersion     = "3.2.20"
val catsVersion          = "2.13.0"
val scalafxVersion       = "26.0.0-R38"
val twelvemonkeysVersion = "3.13.1"
val vertxVersion = "5.1.2"

ThisBuild / name         := "PPS-25-wizard"
ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := scala3Version
ThisBuild / scalacOptions := Seq("-Wunused:all", "-Wunused:imports")

ThisBuild / libraryDependencies ++= Seq(
  "org.typelevel"             %% "cats-core"               % catsVersion,
  "org.scalafx"               %% "scalafx"                 % scalafxVersion,
  "com.twelvemonkeys.imageio"  % "imageio-webp"            % twelvemonkeysVersion,
  "org.scalatest"             %% "scalatest"               % scalatestVersion % Test,
  "io.vertx"                  % "vertx-core"              % vertxVersion,
)

lazy val root = (project in file("."))
  .settings(
//    assembly / mainClass := Some("it.unibo.pps.wizard.main")
  )
