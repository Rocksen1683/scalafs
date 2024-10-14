import Dependencies._

ThisBuild / scalaVersion     := "2.13.12"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

lazy val root = (project in file("."))
  .settings(
    name := "scalafs",

    //porting dependenices from Dependencies.scala
    libraryDependencies ++= Seq(
      akkaActorTyped,
      akkaStream,
      akkaHttp,
      akkaTestKit,
      munit,
      logback,
      playJSON,
      scalaTest,
      zookeeper,
    ),

    logLevel := Level.Info
  )
