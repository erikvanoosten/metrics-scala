lazy val baseVersion = "3.0.5"

akkaVersion := "2.3.0"

name := "metrics-scala"

// Akka versions: 2.1.4, 2.2.3, 2.3.0

description <<= (scalaVersion,akkaVersion) { (sv,av) =>
  val akkaVersion = if (sv.startsWith("2.10")) "Akka " + av +" and " else ""
  "metrics-scala for " + akkaVersion + "Scala " + sbt.cross.CrossVersionUtil.binaryScalaVersion(sv)
}

organization := "nl.grons"

version <<= (scalaVersion,akkaVersion) { (sv,av) => 
  if (sv.startsWith("2.10")) baseVersion + "_a" + av.substring(0,3) 
  else baseVersion
}

scalaVersion := "2.10.0"

crossScalaVersions := Seq("2.9.1", "2.9.1-1", "2.9.2", "2.9.3", "2.10.0")

crossVersion := CrossVersion.binary

resolvers ++= Seq(
  "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository"
)

libraryDependencies ++= Seq(
  "com.codahale.metrics" % "metrics-core" % "3.0.2",
  "com.codahale.metrics" % "metrics-healthchecks" % "3.0.2",
  "junit" % "junit" % "4.11" % "test",
  "org.scalatest" %% "scalatest" % "1.9.1" % "test",
  "org.mockito" % "mockito-all" % "1.9.5" % "test"
)

libraryDependencies <++= (scalaVersion,akkaVersion) { (sv,av) =>
  if (sv.startsWith("2.10"))
    Seq(
      "com.typesafe.akka" %% "akka-actor" % av,
      "com.typesafe.akka" %% "akka-testkit" % av % "test"
    )
  else
    Seq()
}

unmanagedSourceDirectories in Compile <<= (unmanagedSourceDirectories in Compile, sourceDirectory in Compile, scalaVersion) { (sds: Seq[java.io.File], sd: java.io.File, v: String) =>
  val mainVersion = v.split("""\.""").take(2).mkString(".")
  val extra = new java.io.File(sd, "scala_" + mainVersion)
  (if (extra.exists) Seq(extra) else Seq()) ++ sds
}

unmanagedSourceDirectories in Test <<= (unmanagedSourceDirectories in Test, sourceDirectory in Test, scalaVersion) { (sds: Seq[java.io.File], sd: java.io.File, v: String) =>
  val mainVersion = v.split("""\.""").take(2).mkString(".")
  val extra = new java.io.File(sd, "scala_" + mainVersion)
  (if (extra.exists) Seq(extra) else Seq()) ++ sds
}

javacOptions ++= Seq("-Xmx512m", "-Xms128m", "-Xss10m")

javaOptions ++= Seq("-Xmx512m", "-Djava.awt.headless=true")

scalacOptions ++= Seq("-deprecation", "-unchecked")

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
