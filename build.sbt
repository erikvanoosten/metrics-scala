import sbt.Keys.scalaVersion
import sbt.librarymanagement.{CrossVersion, ModuleID}

lazy val commonSettings = Seq(
  organization := "nl.grons",
  scalaVersion := "2.13.10",
  crossVersion := CrossVersion.binary,
  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "3.2.18" % Test,
    "org.mockito" % "mockito-core" % "5.10.0" % Test,
    "org.slf4j" % "slf4j-simple" % "2.0.12" % Test
  ),
  fork := true,
  Test / testOptions += {
    // Exclude tests based on scala version:
    if (before212(scalaVersion.value)) Tests.Argument("-l", ">=scala2.12")
    else Tests.Argument("-l", "<scala2.12")
  },
  javacOptions ++= Seq("--release", "11", "-J-Xmx512m", "-J-Xms128m", "-J-Xss10m"),
  javaOptions ++= Seq("-Xmx512m", "-Djava.awt.headless=true"),
  scalacOptions ++= scalacTargets(scalaVersion.value) ++ Seq("-deprecation", "-unchecked"),
  publishTo := sonatypePublishToBundle.value,
  publishMavenStyle := true,
  Test / publishArtifact := false,
  pomIncludeRepository := { _ => false },
  licenses := Seq("Apache 2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")),
  homepage := Some(url("https://github.com/erikvanoosten/metrics-scala")),
  scmInfo := Some(ScmInfo(
    url("https://github.com/erikvanoosten/metrics-scala"),
    "scm:git:git@github.com:erikvanoosten/metrics-scala.git"
  )),
  developers := List(
    Developer(id="erikvanoosten", name="Erik van Oosten", email="", url=url("https://github.com/erikvanoosten")),
    Developer(id="scullxbones", name="Brian Scully", email="", url=url("https://github.com/scullxbones"))
  )
)

ThisBuild / publishTo := sonatypePublishTo.value

lazy val root = (project in file("."))
  .aggregate(metricsScala, metricsScalaHdr, metricsAkka25, metricsAkka26, metricsPekko)
  .settings(
    crossScalaVersions := Nil,
    publishArtifact := false,
    publish / skip := true,
    publish := {},
    publishLocal := {},
    name := "metrics4-scala-root",
    sonatypeProfileName := "nl.grons"
  )

lazy val metricsScala = (project in file("metrics-scala"))
  .settings(
    commonSettings,
    crossScalaVersions := Seq("3.3.1", "2.13.13", "2.12.18", "2.11.12"),
    name := "metrics4-scala",
    description := "metrics-scala for Scala " + CrossVersion.binaryScalaVersion(scalaVersion.value),
    libraryDependencies ++= Seq(
      "io.dropwizard.metrics" % "metrics-core" % "4.2.25",
      "io.dropwizard.metrics" % "metrics-healthchecks" % "4.2.25"
    ),
    mimaPreviousArtifacts := mimaPrevious(scalaVersion.value)
)

lazy val metricsScalaHdr = (project in file("metrics-scala-hdr"))
  .dependsOn(metricsScala)
  .settings(
    commonSettings,
    crossScalaVersions := Seq("3.3.1", "2.13.13", "2.12.18", "2.11.12"),
    name := "metrics4-scala-hdr",
    description := "metrics-scala-hdr for Scala " + CrossVersion.binaryScalaVersion(scalaVersion.value),
    libraryDependencies ++= Seq(
      "org.mpierce.metrics.reservoir" % "hdrhistogram-metrics-reservoir" % "1.1.3",
      // Override version that hdrhistogram-metrics-reservoir depends on:
      "org.hdrhistogram" % "HdrHistogram" % "2.1.12"
    ),
    mimaPreviousArtifacts := mimaPrevious(scalaVersion.value)
  )

lazy val metricsPekko = (project in file("metrics-pekko"))
  .dependsOn(metricsScala)
  .settings(
    commonSettings,
    crossScalaVersions := Seq("3.3.1", "2.13.13", "2.12.18"),
    name := "metrics4-pekko",
    description := "metrics-scala for pekko 1.0.1 and Scala " + CrossVersion.binaryScalaVersion(scalaVersion.value),
    libraryDependencies ++= Seq(
      "org.apache.pekko" %% "pekko-actor" % "1.0.2",
      "org.apache.pekko" %% "pekko-testkit" % "1.0.2" % Test
    ),
    sourceDirectory := baseDirectory.value.getParentFile / "metrics-pekko" / "src",
    mimaPreviousArtifacts := mimaPrevious(scalaVersion.value)
  )

lazy val metricsAkka26 = (project in file("metrics-akka-26"))
  .dependsOn(metricsScala)
  .settings(
    commonSettings,
    crossScalaVersions := Seq("3.3.1", "2.13.13", "2.12.18"),
    name := "metrics4-akka_a26",
    description := "metrics-scala for Akka 2.6 and Scala " + CrossVersion.binaryScalaVersion(scalaVersion.value),
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % "2.6.20",
      "com.typesafe.akka" %% "akka-testkit" % "2.6.20" % Test
    ),
    sourceDirectory := baseDirectory.value.getParentFile / "metrics-akka" / "src",
    mimaPreviousArtifacts := mimaPrevious(scalaVersion.value)
  )

lazy val metricsAkka25 = (project in file("metrics-akka-25"))
  .dependsOn(metricsScala)
  .settings(
    commonSettings,
    crossScalaVersions := Seq("2.13.13", "2.12.18"),
    name := "metrics4-akka_a25",
    description := "metrics-scala for Akka 2.5 and 2.6 and Scala " + CrossVersion.binaryScalaVersion(scalaVersion.value),
    libraryDependencies ++= Seq(
      // Stay on Akka 2.5 to guarantee backward compatibility:
      // scala-steward:off
      "com.typesafe.akka" %% "akka-actor" % "2.5.31",
      "com.typesafe.akka" %% "akka-testkit" % "2.5.31" % Test
      // scala-steward:on
    ),
    sourceDirectory := baseDirectory.value.getParentFile / "metrics-akka" / "src",
    mimaPreviousArtifacts := mimaPrevious(scalaVersion.value)
  )

// 2.11.x are the only pre-2.12 scala versions that are used in this build
def before212(scalaVersion: String): Boolean = scalaVersion.startsWith("2.11.")

def mimaPrevious(scalaVersion: String): Set[ModuleID] = {
  if (scalaVersion.startsWith("3."))
    Set("nl.grons" %% "metrics4-scala" % "4.2.8")
  else if (scalaVersion.startsWith("2.13."))
    Set("nl.grons" %% "metrics4-scala" % "4.0.7")
  else
    Set("nl.grons" %% "metrics4-scala" % "4.0.1")
}

def scalacTargets(scalaVersion: String): Seq[String] = {
  if (scalaVersion.startsWith("2.11.") || scalaVersion.startsWith("2.12.")) Seq("-target:jvm-1.8")
  else if (scalaVersion.startsWith("2.13.")) Seq("-release:11")
  else {
    // Scala 3.3.0+
    Seq("-java-output-version:11")
  }
}

// Config for sbt-github-actions plugin
ThisBuild / crossScalaVersions := Seq("2.13.10")
ThisBuild / githubWorkflowPublishTargetBranches := Seq()
ThisBuild / githubWorkflowJavaVersions := Seq(
  JavaSpec.temurin("11"),
  JavaSpec.temurin("17"),
  JavaSpec.temurin("21")
)

