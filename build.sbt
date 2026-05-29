val scala3Version = "3.8.3"

ThisBuild / scalaVersion := scala3Version

ThisBuild / libraryDependencies ++= Seq(
    // Source: https://mvnrepository.com/artifact/org.scalatest/scalatest
    "org.scalatest" %% "scalatest" % "3.2.20" % Test,
    // Source: https://mvnrepository.com/artifact/org.typelevel/cats-core
    "org.typelevel" %% "cats-core" % "2.13.0"
)

lazy val root = (project in file("."))
  .settings(
    name := "PPS-25-wizard"
  )
