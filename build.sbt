name := "scala-opencage-geocoder"
organization := "com.opencagedata"
version := "1.0.0"

scalaVersion := "2.12.8"

scalacOptions += "-feature"

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

    "org.scalatest" %% "scalatest" % "3.0.5" % Test,
    "com.github.tomakehurst" % "wiremock" % "2.20.0" % Test,
    "org.apache.commons" % "commons-lang3" % "3.7" % Test
  )
}

publishTo := sonatypePublishTo.value

credentials += Credentials(Path.userHome / ".sbt" / "opencage_sonatype_credentials")

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
