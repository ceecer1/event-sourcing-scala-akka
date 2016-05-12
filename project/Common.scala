import sbt.Keys._

object Common {
  val commonSettings = Seq(
    scalaVersion := "2.11.7"
    //scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-Xfatal-warnings")
  )

  val akkaVersion = "2.3.12"
  val akkaStreamVersion = "2.0.3"
  val reactiveRabbitVersion = "1.0.3"
  val sprayVersion = "1.3.3"
  val json4sVersion = "3.3.0"
  val logbackVersion = "1.1.2"
}
