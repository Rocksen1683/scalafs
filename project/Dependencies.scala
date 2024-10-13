import sbt._

object Dependencies {
  //Akka for streaming and data replication
  lazy val akkaActorTyped = "com.typesafe.akka" %% "akka-actor-typed" % "2.6.19"
  lazy val akkaStream = "com.typesafe.akka" %% "akka-stream" % "2.6.19"
  lazy val akkaHttp = "com.typesafe.akka" %% "akka-http" % "10.2.9"
  lazy val akkaTestKit = "com.typesafe.akka" %% "akka-testkit" % "2.6.19" % Test

  //ZooKeeper for coordination and failover
  lazy val zookeeper = "org.apache.zookeeper" % "zookeeper" % "3.6.3"

  //Logging with Logback
  lazy val logback = "ch.qos.logback" % "logback-classic" % "1.2.11"

  //Testing
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.2.10" % Test
  lazy val munit = "org.scalameta" %% "munit" % "0.7.29" % Test
}
