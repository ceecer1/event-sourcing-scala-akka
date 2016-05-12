import Common._
import play.PlayImport.PlayKeys._

name := "socioevsrcng"

version := "1.0"

lazy val writeside = project.in(file("writeside"))
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= {
      Seq(
        "com.typesafe.akka"       %%  "akka-actor"      % akkaVersion,
        "com.typesafe.akka"       %%  "akka-slf4j"      % akkaVersion,
        "com.typesafe.akka"       %%  "akka-testkit"    % akkaVersion % "test",
        "com.typesafe.akka"       %%  "akka-kernel"     % akkaVersion,
        "com.typesafe.akka"       %%  "akka-persistence-experimental" % akkaVersion,
        "com.typesafe.akka"       %% "akka-stream-experimental" % akkaStreamVersion,
        "io.scalac"               %% "reactive-rabbit" % reactiveRabbitVersion,
        "io.spray"                %%  "spray-can"       % sprayVersion,
        "io.spray"                %%  "spray-routing"   % sprayVersion,
        "io.spray"                %%  "spray-testkit"   % sprayVersion,
        "io.spray"                %%  "spray-httpx"     % sprayVersion,
        "io.spray"                %%  "spray-client"    % sprayVersion,
        "org.json4s"              %%  "json4s-native"   % json4sVersion,
        "joda-time"               %   "joda-time"       % "2.8.2",
        "org.joda"                %   "joda-convert"    % "1.7",
        "ch.qos.logback"          %   "logback-classic" % logbackVersion,
        "org.scalaz"              %%   "scalaz-core"    % "7.1.0" % "compile",
        "com.github.ironfish" %% "akka-persistence-mongo-casbah"  % "0.7.6" % "compile"
      )
    },
    fork := true
  )

lazy val queryside = project.in(file("queryside"))
  .settings(commonSettings: _*)
  .settings(routesImport ++= Seq("scala.language.reflectiveCalls"))
  .settings(
    libraryDependencies ++= Seq(
      cache,
      "com.typesafe.akka" %% "akka-actor" % akkaVersion,
      "com.typesafe.akka"       %% "akka-stream-experimental" % akkaStreamVersion,
      "io.scalac" %% "reactive-rabbit" % reactiveRabbitVersion,
      "org.reactivemongo" %% "play2-reactivemongo" % "0.10.5.0.akka23",
      "com.typesafe.play" %% "play-mailer" % "2.4.1",
      "com.typesafe.play" %% "play-ws" % "2.3.8",
      "com.typesafe.play" %% "play-json" % "2.4.6"

    )
  )
  .enablePlugins(PlayScala)

/*
val starth2 = taskKey[Unit]("")

starth2 := {
  val logger = streams.value.log
  Process("java -version") ! logger match {
    case 0 => logger.info("Process has been started")
    case n => sys.error(s"Could not restart the process, exit code: $n")
  }
}
*/
