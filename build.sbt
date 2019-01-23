name := "scala-opencage-geocoder"

version := "0.1"
scalaVersion := "2.12.4"

mainClass in(Compile, packageBin) := Some("com.github.nmdguerreiro.opencage.geocoder.OpenCageClientForwardDemoApp")
mainClass in(Compile, run) := Some("com.github.nmdguerreiro.opencage.geocoder.OpenCageClientForwardDemoApp")

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
    "com.github.scopt" %% "scopt" % "3.7.0",

    "org.scalatest" %% "scalatest" % "3.0.5" % Test,
    "com.github.tomakehurst" % "wiremock" % "2.20.0" % Test,
    "org.apache.commons" % "commons-lang3" % "3.7" % Test
  )
}

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
