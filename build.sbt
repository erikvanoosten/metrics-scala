lazy val commonSettings = Seq(
  organization := "nl.grons",
  scalaVersion := "2.12.8",
  crossVersion := CrossVersion.binary,
  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "3.1.0-SNAP12" % Test,
    "org.mockito" %% "mockito-scala" % "1.5.8" % Test,
    "org.slf4j" % "slf4j-simple" % "1.7.26" % Test
  ),
  fork := true,
  Test / testOptions += {
    // Exclude tests based on scala version:
    if (before212(scalaVersion.value)) Tests.Argument("-l", ">=scala2.12")
    else Tests.Argument("-l", "<scala2.12")
  },
  javacOptions ++= Seq("-target", "1.8", "-J-Xmx512m", "-J-Xms128m", "-J-Xss10m"),
  javaOptions ++= Seq("-Xmx512m", "-Djava.awt.headless=true"),
  scalacOptions ++= Seq("-target:jvm-1.8", "-deprecation", "-unchecked"),
  credentials += Credentials(Path.userHome / ".sbt" / "sonatype.credentials"),
  publishTo := Some(
    if (isSnapshot.value) Opts.resolver.sonatypeSnapshots
    else Opts.resolver.sonatypeStaging
  ),
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

lazy val root = (project in file("."))
  .aggregate(metricsScala, metricsScalaHdr, metricsAkka24, metricsAkka25)
  .settings(
    publishArtifact := false,
    publish / skip := true,
    publish := {},
    publishLocal := {},
    name := "metrics4-scala-root"
  )

lazy val metricsScala = (project in file("metrics-scala"))
  .settings(
    commonSettings,
    crossScalaVersions := Seq("2.11.12", "2.12.8", "2.13.0-RC3"),
    name := "metrics4-scala",
    description := "metrics-scala for Scala " + CrossVersion.binaryScalaVersion(scalaVersion.value),
    libraryDependencies ++= Seq(
      "io.dropwizard.metrics" % "metrics-core" % "4.1.0",
      "io.dropwizard.metrics" % "metrics-healthchecks" % "4.1.0"
    ),
    mimaPreviousArtifacts := Set("nl.grons" %% "metrics4-scala" % "4.0.1")
  )

lazy val metricsScalaHdr = (project in file("metrics-scala-hdr"))
  .dependsOn(metricsScala)
  .settings(
    commonSettings,
    crossScalaVersions := Seq("2.11.12", "2.12.8", "2.13.0-RC3"),
    name := "metrics4-scala-hdr",
    description := "metrics-scala-hdr for Scala " + CrossVersion.binaryScalaVersion(scalaVersion.value),
    libraryDependencies ++= Seq(
      "org.mpierce.metrics.reservoir" % "hdrhistogram-metrics-reservoir" % "1.1.0",
      // Override version that hdrhistogram-metrics-reservoir depends on:
      "org.hdrhistogram" % "HdrHistogram" % "2.1.11"
    ),
    mimaPreviousArtifacts := Set("nl.grons" %% "metrics4-scala-hdr" % "4.0.1")
  )

lazy val metricsAkka25 = (project in file("metrics-akka-25"))
  .dependsOn(metricsScala)
  .settings(
    commonSettings,
    crossScalaVersions := Seq("2.12.8", "2.13.0-RC3"),
    name := "metrics4-akka_a25",
    description := "metrics-scala for Akka 2.5 and Scala " + CrossVersion.binaryScalaVersion(scalaVersion.value),
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % "2.5.23",
      "com.typesafe.akka" %% "akka-testkit" % "2.5.23" % Test
    ),
    sourceDirectory := baseDirectory.value.getParentFile / "metrics-akka" / "src",
    mimaPreviousArtifacts := Set("nl.grons" %% "metrics4-akka_a25" % "4.0.1")
  )

lazy val metricsAkka24 = (project in file("metrics-akka-24"))
  .dependsOn(metricsScala)
  .settings(
    commonSettings,
    crossScalaVersions := Seq("2.11.12", "2.12.8"),
    name := "metrics4-akka_a24",
    description := "metrics-scala for Akka 2.4 and 2.5 and Scala " + CrossVersion.binaryScalaVersion(scalaVersion.value),
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % "2.4.20",
      "com.typesafe.akka" %% "akka-testkit" % "2.4.20" % Test
    ),
    sourceDirectory := baseDirectory.value.getParentFile / "metrics-akka" / "src",
    mimaPreviousArtifacts := Set("nl.grons" %% "metrics4-akka_a24" % "4.0.1")
  )

// 2.11.x are the only pre-2.12 scala versions that are used in this build
def before212(scalaVersion: String): Boolean = scalaVersion.startsWith("2.11.")
