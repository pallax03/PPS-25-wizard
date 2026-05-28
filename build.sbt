ThisBuild / scalaVersion := "3.8.3"

lazy val root = (project in file("."))
  .settings(
    name := "PPS-25-wizard",
    libraryDependencies ++= {
      // 1. Determina il sistema operativo per JavaFX
      lazy val osName = System.getProperty("os.name") match {
        case n if n.startsWith("Linux")   => "linux"
        case n if n.startsWith("Mac")     => "mac"
        case n if n.startsWith("Windows") => "win"
        case _ => throw new Exception("Unknown platform!")
      }

      // 2. Crea la lista con TUTTE le dipendenze (ScalaFX + i moduli JavaFX alla versione 26)
      Seq(
        "org.scalafx" %% "scalafx" % "26.0.0-R38"
      ) ++
        Seq("base", "controls", "fxml", "graphics", "media", "swing", "web").map(
          m => "org.openjfx" % s"javafx-$m" % "26" classifier osName
        )
    }
  )
