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
      zookeeper,
      logback,
      scalaTest,
      munit 
    ),

    logLevel := Level.Info
  )
