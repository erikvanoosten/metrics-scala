import sbt.Keys._

lazy val baseVersion = "3.5.6-snapshot"

// See crossrelease.sh for valid combinations of akkaVersion and crossScalaVersion.

// Developed against 2.3.* (2.4.* for scala 2.12), see crossrelease.sh for all tested and build versions.
akkaVersion := {
  scalaVersion.value match {
    case v if v.startsWith("2.12") => "2.4.17"
    case _ => "2.3.16"
  }
}

organization := "nl.grons"

name := "metrics-scala"

version := {
  val av = akkaVersion.value
  baseVersion + (if (av.nonEmpty) "_a" + av.split('.').take(2).mkString(".") else "")
}

description := {
  val av = akkaVersion.value
  val akkaDescription = if (av.nonEmpty) "Akka " + av +" and " else ""
  "metrics-scala for " + akkaDescription + "Scala " + sbt.cross.CrossVersionUtil.binaryScalaVersion(scalaVersion.value)
}

// Developed against 2.11, see crossrelease.sh for all tested and build versions.
scalaVersion := "2.11.8"

crossScalaVersions := Seq("2.10.6", "2.11.8", "2.12.1")

crossVersion := CrossVersion.binary

resolvers ++= Seq(
  "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository"
)

libraryDependencies ++= Seq(
  "io.dropwizard.metrics" % "metrics-core" % "3.2.0",
  "io.dropwizard.metrics" % "metrics-healthchecks" % "3.2.0",
  "org.mpierce.metrics.reservoir" % "hdrhistogram-metrics-reservoir" % "1.1.0" % "optional",
  // Override version that hdrhistogram-metrics-reservoir depends on:
  "org.hdrhistogram" % "HdrHistogram" % "2.1.9" % "optional",
  "org.scalatest" %% "scalatest" % "3.0.0" % "test",
  "org.mockito" % "mockito-all" % "1.10.19" % "test",
  "org.slf4j" % "slf4j-simple" % "1.7.22" % "test"
)

libraryDependencies ++= {
  val av = akkaVersion.value
  if (av.nonEmpty)
    Seq(
      "com.typesafe.akka" %% "akka-actor" % av,
      "com.typesafe.akka" %% "akka-testkit" % av % "test"
    )
  else Seq.empty
}

unmanagedSourceDirectories in Compile ++= {
  val av = akkaVersion.value
  val extra = new java.io.File((sourceDirectory in Compile).value, "akka")
  if (av.nonEmpty && extra.exists) Seq(extra) else Seq.empty
}

unmanagedSourceDirectories in Test ++= {
  val av: String = akkaVersion.value
  val extra = new java.io.File((sourceDirectory in Test).value, "akka")
  if (av.nonEmpty && extra.exists) Seq(extra) else Seq.empty
}

javacOptions ++= {
  val javaTarget = scalaVersion.value match {
    case v if v.startsWith("2.12") => Seq.empty[String]
    case _ => Seq("-target", "1.6")
  }
  Seq("-Xmx512m", "-Xms128m", "-Xss10m") ++ javaTarget
}

javaOptions ++= Seq("-Xmx512m", "-Djava.awt.headless=true")

scalacOptions ++= Seq("-deprecation", "-unchecked")

testOptions in Test += {
  scalaVersion.value match {
    case v if v.startsWith("2.12") => Tests.Argument("-l", "<scala2.12")
    case _ => Tests.Argument("-l", ">=scala2.12")
  }
}

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (version.value.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

credentials += Credentials(Path.userHome / ".sbt" / "sonatype.credentials")

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

licenses := Seq("Apache 2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))

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
