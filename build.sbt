import sbt.Keys.scalaVersion
import sbt.librarymanagement.{CrossVersion, ModuleID}
import xerial.sbt.Sonatype.sonatypeCentralHost

val scala3 = "3.9.0"
val scala213 = "2.13.18"

lazy val commonSettings = Seq(
  organization := "nl.grons",
  scalaVersion := scala213,
  crossVersion := CrossVersion.binary,
  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "3.2.20" % Test,
    "org.scalamock" %% "scalamock-scalatest" % "7.6.3" % Test,
    "org.slf4j" % "slf4j-simple" % "2.0.20" % Test
  ),
  fork := true,
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

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html
ThisBuild / publishTo := sonatypePublishTo.value
ThisBuild / sonatypeCredentialHost := sonatypeCentralHost
ThisBuild / credentials += Credentials(Path.userHome / ".sbt" / "sonatype_central_credentials")

lazy val root = project
  .in(file("."))
  .aggregate(metricsScala, metricsScalaHdr, metricsAkka26, metricsPekko)
  .settings(
    crossScalaVersions := Nil,
    publishArtifact := false,
    publish / skip := true,
    publish := {},
    publishLocal := {},
    name := "metrics4-scala-root",
    sonatypeProfileName := "nl.grons",
    mimaPreviousArtifacts := Set.empty,
  )

lazy val metricsScala = project
  .in(file("metrics-scala"))
  .settings(
    commonSettings,
    crossScalaVersions := Seq(scala3, scala213),
    name := "metrics4-scala",
    description := "metrics-scala for Scala " + CrossVersion.binaryScalaVersion(scalaVersion.value),
    libraryDependencies ++= Seq(
      "io.dropwizard.metrics" % "metrics-core" % "4.2.40",
      "io.dropwizard.metrics" % "metrics-healthchecks" % "4.2.40"
    ),
    mimaPreviousArtifacts := mimaPrevious(name.value, scalaVersion.value)
)

lazy val metricsScalaHdr = project
  .in(file("metrics-scala-hdr"))
  .dependsOn(metricsScala)
  .settings(
    commonSettings,
    crossScalaVersions := Seq(scala3, scala213),
    name := "metrics4-scala-hdr",
    description := "metrics-scala-hdr for Scala " + CrossVersion.binaryScalaVersion(scalaVersion.value),
    libraryDependencies ++= Seq(
      "org.mpierce.metrics.reservoir" % "hdrhistogram-metrics-reservoir" % "1.1.3",
      // Override version that hdrhistogram-metrics-reservoir depends on:
      "org.hdrhistogram" % "HdrHistogram" % "2.2.2"
    ),
    mimaPreviousArtifacts := mimaPrevious(name.value, scalaVersion.value)
  )

lazy val metricsPekko = project
  .in(file("metrics-pekko"))
  .dependsOn(metricsScala)
  .settings(
    commonSettings,
    crossScalaVersions := Seq(scala3, scala213),
    name := "metrics4-pekko",
    description := "metrics-scala for pekko 1.0 and Scala " + CrossVersion.binaryScalaVersion(scalaVersion.value),
    libraryDependencies ++= Seq(
      "org.apache.pekko" %% "pekko-actor" % "1.7.1",
      "org.apache.pekko" %% "pekko-testkit" % "1.7.1" % Test
    ),
    sourceDirectory := baseDirectory.value.getParentFile / "metrics-pekko" / "src",
    mimaPreviousArtifacts := Set("nl.grons" %% "metrics4-pekko" % "4.3.2")
  )

lazy val metricsAkka26 = project
  .in(file("metrics-akka-26"))
  .dependsOn(metricsScala)
  .settings(
    commonSettings,
    crossScalaVersions := Seq(scala3, scala213),
    name := "metrics4-akka_a26",
    description := "metrics-scala for Akka 2.6 and Scala " + CrossVersion.binaryScalaVersion(scalaVersion.value),
    libraryDependencies ++= Seq(
      // Stay on Akka 2.6, later versions are not open source:
      // scala-steward:off
      "com.typesafe.akka" %% "akka-actor" % "2.6.20",
      "com.typesafe.akka" %% "akka-testkit" % "2.6.20" % Test
      // scala-steward:on
    ),
    mimaPreviousArtifacts := mimaPrevious(name.value, scalaVersion.value)
  )

def mimaPrevious(module: String, scalaVersion: String): Set[ModuleID] =
  Set("nl.grons" %% module % "4.2.8")

def scalacTargets(scalaVersion: String): Seq[String] = {
  if (scalaVersion.startsWith("2.13.")) Seq("-release:11")
  else {
    // Scala 3.9.0+
    Seq("-java-output-version:21")
  }
}

// Config for sbt-github-actions plugin
ThisBuild / crossScalaVersions := Seq(scala3, scala213)
ThisBuild / githubWorkflowPublishTargetBranches := Seq()
ThisBuild / githubWorkflowJavaVersions := Seq(
  JavaSpec.temurin("11"),
  JavaSpec.temurin("21"),
  JavaSpec.temurin("25")
)
ThisBuild / githubWorkflowBuildMatrixExclusions :=
  Seq(MatrixExclude(Map("scala" -> "3.9.0", "java" -> "temurin@11")))
