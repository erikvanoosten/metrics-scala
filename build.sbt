name := "metrics-scala"

description <<= (scalaVersion) { v => "metrics-scala for " + v }

organization := "nl.grons"

version := "2.2.0"

scalaVersion := "2.10.0"

crossScalaVersions := Seq("2.9.1", "2.9.1-1", "2.9.2", "2.10.0")

crossVersion := CrossVersion.binary

resolvers ++= Seq(
  "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository",
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
)

libraryDependencies <+= (version) { v => "com.yammer.metrics" % "metrics-core" % v }

javacOptions ++= Seq("-Xmx512m", "-Xms128m", "-Xss10m")

javaOptions += "-Xmx512m"

scalacOptions ++= Seq("-deprecation", "-unchecked")

publishTo <<= version { v: String =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT")) Some("snapshots" at nexus + "content/repositories/snapshots")
  else                             Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

credentials += Credentials(Path.userHome / ".sbt" / "sonatype.credentials")

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

licenses := Seq("Apache 2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))

pomExtra := (
  <url>http://metrics.codahale.com/</url>
  <scm>
    <url>git@github.com:erikvanoosten/metrics-scala.git</url>
    <connection>scm:git:git@github.com:erikvanoosten/metrics-scala.git</connection>
  </scm>
  <developers>
    <developer>
      <name>Coda Hale</name>
      <email>coda.hale@gmail.com</email>
      <timezone>-8</timezone>
    </developer>
    <developer>
      <name>Erik van Oosten</name>
      <url>http://day-to-day-stuff.blogspot.com/</url>
    </developer>
  </developers>
)

// osgiSettings
//
// OsgiKeys.importPackage := Seq(
//   """com.yammer.metrics;version="[2.1,3)"""",
//   """com.yammer.metrics.core;version="[2.1,3)"""",
//   """com.yammer.metrics.stats;version="[2.1,3)"""",
//   "scala",
//   "scala.reflect")
//
// OsgiKeys.exportPackage := Seq("com.yammer.metrics.scala")
//
// OsgiKeys.privatePackage := Seq()
//
// OsgiKeys.additionalHeaders := Map("-removeheaders" -> "Include-Resource")
