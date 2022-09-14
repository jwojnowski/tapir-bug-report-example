ThisBuild / scalaVersion     := "2.13.8"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "me.wojnowski.tapir"
ThisBuild / organizationName := "example"

lazy val root = (project in file("."))
  .settings(
    name := "tapir-bug-reproduction",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % "2.0.2",
      "com.softwaremill.sttp.tapir" %% "tapir-http4s-server-zio" % "1.1.0",
      "org.http4s" %% "http4s-blaze-server" % "0.23.12"
    )
  )
