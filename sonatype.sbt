// Your profile name of the sonatype account. The default is the same with the organization value
sonatypeProfileName := "com.opencagedata"

// To sync with Maven central, you need to supply the following information:
publishMavenStyle := true

// License of your choice
licenses := Seq("MIT" -> url("https://opensource.org/licenses/MIT"))

// Where is the source code hosted
import xerial.sbt.Sonatype._

sonatypeProjectHosting := Some(GitHubHosting("OpenCageData  ", "scala-opencage-geocoder", "support@opencagedata.com"))

