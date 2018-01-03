import sbt._
import Keys._

object MetricsScalaBuild extends Build {
  lazy val akkaVersion = settingKey[String]("Version of Akka compiled against")

  akkaVersion := "2.4.20"
}
