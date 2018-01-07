lazy val commonSettings = Seq(
  organization := "nl.grons",
  scalaVersion := "2.12.4",
  crossVersion := CrossVersion.binary,
  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "3.0.4" % "test",
    "org.mockito" % "mockito-all" % "1.10.19" % "test",
    "org.slf4j" % "slf4j-simple" % "1.7.25" % "test"
  ),
  fork := true,
  testOptions in Test += {
    scalaVersion.value match {
      case v if v.startsWith("2.12") => Tests.Argument("-l", "<scala2.12")
      case _ => Tests.Argument("-l", ">=scala2.12")
    }
  },
  javacOptions ++= Seq("-target", "1.8", "-J-Xmx512m", "-J-Xms128m", "-J-Xss10m"),
  javaOptions ++= Seq("-Xmx512m", "-Djava.awt.headless=true"),
  scalacOptions ++= Seq("-deprecation", "-unchecked"),
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (version.value.trim.endsWith("SNAPSHOT"))
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases"  at nexus + "service/local/staging/deploy/maven2")
  },
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomIncludeRepository := { _ => false },
  licenses := Seq("Apache 2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")),
  pomExtra := (
    <url>https://github.com/erikvanoosten/metrics-scala</url>
    <scm>
      <url>git@github.com:erikvanoosten/metrics-scala.git</url>
      <connection>scm:git:git@github.com:erikvanoosten/metrics-scala.git</connection>
    </scm>
    <developers>
      <developer>
        <name>Erik van Oosten</name>
        <url>http://day-to-day-stuff.blogspot.com/</url>
      </developer>
      <developer>
        <name>Brian Scully</name>
        <url>https://github.com/scullxbones/</url>
      </developer>
    </developers>
  )
)

lazy val root = (project in file("."))
  .aggregate(metricsScala, metricsScalaHdr, metricsAkka24, metricsAkka25)
  .settings(
    publishArtifact := false,
    name := "metrics-scala-root"
  )

lazy val metricsScala = (project in file("metrics-scala"))
  .settings(
    commonSettings,
    crossScalaVersions := Seq("2.11.12", "2.12.4"),
    name := "metrics-scala",
    description := "metrics-scala for Scala " + CrossVersion.binaryScalaVersion(scalaVersion.value),
    libraryDependencies ++= Seq(
      "io.dropwizard.metrics" % "metrics-core" % "4.0.1",
      "io.dropwizard.metrics" % "metrics-healthchecks" % "4.0.1"
    )
  )

lazy val metricsScalaHdr = (project in file("metrics-scala-hdr"))
  .dependsOn(metricsScala)
  .settings(
    commonSettings,
    crossScalaVersions := Seq("2.11.12", "2.12.4"),
    name := "metrics-scala-hdr",
    description := "metrics-scala-hdr for Scala " + CrossVersion.binaryScalaVersion(scalaVersion.value),
    libraryDependencies ++= Seq(
      "org.mpierce.metrics.reservoir" % "hdrhistogram-metrics-reservoir" % "1.1.0",
      // Override version that hdrhistogram-metrics-reservoir depends on:
      "org.hdrhistogram" % "HdrHistogram" % "2.1.9"
    )
  )

lazy val metricsAkka24 = (project in file("metrics-akka-24"))
  .dependsOn(metricsScala)
  .settings(
    commonSettings,
    crossScalaVersions := Seq("2.11.12", "2.12.4"),
    name := "metrics-akka_a24",
    description := "metrics-scala for Akka 2.4 and 2.5 and Scala " + CrossVersion.binaryScalaVersion(scalaVersion.value),
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % "2.4.20",
      "com.typesafe.akka" %% "akka-testkit" % "2.4.20" % "test"
    ),
    sourceDirectory := baseDirectory.value.getParentFile / "metrics-akka" / "src"
  )

lazy val metricsAkka25 = (project in file("metrics-akka-25"))
  .dependsOn(metricsScala)
  .settings(
    commonSettings,
    crossScalaVersions := Seq("2.12.4"),
    name := "metrics-akka_a25",
    description := "metrics-scala for Akka 2.5 and Scala " + CrossVersion.binaryScalaVersion(scalaVersion.value),
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % "2.5.8",
      "com.typesafe.akka" %% "akka-testkit" % "2.5.8" % "test"
    ),
    sourceDirectory := baseDirectory.value.getParentFile / "metrics-akka" / "src"
  )

credentials += Credentials(Path.userHome / ".sbt" / "sonatype.credentials")
