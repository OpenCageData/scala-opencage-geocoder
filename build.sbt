name := "scala-opencage-geocoder"

version := "0.1"
scalaVersion := "2.12.4"
coverageEnabled := true

mainClass in (Compile, packageBin) := Some("com.github.nmdguerreiro.opencage.geocoder.OpenCageClientForwardDemoApp")
mainClass in (Compile, run) := Some("com.github.nmdguerreiro.opencage.geocoder.OpenCageClientForwardDemoApp")

libraryDependencies += "com.softwaremill.sttp" %% "core" % "1.1.6"
libraryDependencies += "com.softwaremill.sttp" %% "async-http-client-backend-future" % "1.1.6"
libraryDependencies += "com.softwaremill.sttp" %% "circe" % "1.1.6"
libraryDependencies += "io.circe" %% "circe-core" % "0.9.1"
libraryDependencies += "io.circe" %% "circe-generic" % "0.9.1"
libraryDependencies += "io.circe" %% "circe-generic-extras" % "0.9.1"
libraryDependencies += "com.github.scopt" %% "scopt" % "3.7.0"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % "test"
libraryDependencies += "com.github.tomakehurst" % "wiremock" % "2.15.0" % Test
libraryDependencies += "org.apache.commons" % "commons-lang3" % "3.7" % Test

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)