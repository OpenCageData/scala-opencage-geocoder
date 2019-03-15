name := "scala-opencage-geocoder"
organization := "com.opencagedata"
version := "1.1.1"

lazy val scala_2_12 = "2.12.8"
lazy val scala_2_11 = "2.11.8"
scalaVersion := scala_2_12
crossScalaVersions := List(scala_2_11, scala_2_12)

scalacOptions += "-feature"

IntegrationTest / parallelExecution := false

lazy val root = (project in file("."))
  .configs(IntegrationTest)
  .settings(
    Defaults.itSettings,
    libraryDependencies ++= {
      val circeVersion = "0.11.0"
      val sttpVersion = "1.2.3"
      Seq(
        "com.softwaremill.sttp" %% "core" % sttpVersion,
        "com.softwaremill.sttp" %% "async-http-client-backend-future" % sttpVersion,
        "com.softwaremill.sttp" %% "circe" % sttpVersion,
        "io.circe" %% "circe-core" % circeVersion,
        "io.circe" %% "circe-generic" % circeVersion,
        "io.circe" %% "circe-generic-extras" % circeVersion,

        "org.scalatest" %% "scalatest" % "3.0.5" % "test,it",
        "com.github.tomakehurst" % "wiremock" % "2.20.0" % "test,it",
        "org.apache.commons" % "commons-lang3" % "3.7" % "test"
      )
    }
  )

publishTo := sonatypePublishTo.value

credentials += Credentials(Path.userHome / ".sbt" / "opencage_sonatype_credentials")

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
