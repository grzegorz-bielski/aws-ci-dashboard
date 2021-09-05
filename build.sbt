import org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv
import scala.sys.process._
import NativePackagerHelper._

val zioAwsVersion = "3.16.79.3"
val circeVersion = "0.14.1"

ThisBuild / scalaVersion := "3.0.0"
ThisBuild / organization := "com.awscidashboard"
ThisBuild / name := "aws-ci-dashboard"
ThisBuild / version := "0.0.1-SNAPSHOT"

lazy val build = taskKey[Unit]("Builds & packages the app")

lazy val root = project
  .in(file("."))
  .aggregate(
    // shared,
    backend,
    frontend
  )
  .settings(
    build := {
     (frontend / build).value
     (backend / build).value
    }
  )

lazy val shared = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("shared"))
  .settings(
    libraryDependencies ++= Seq(
      "io.circe" %%% "circe-core" % circeVersion,
      "io.circe" %%% "circe-parser" % circeVersion
    )
  )

lazy val frontend =
  project
    .in(file("frontend"))
    .enablePlugins(ScalaJSPlugin)
    .settings(
      libraryDependencies ++= Seq(
        "com.raquo" %%% "laminar" % "0.13.0",
        "io.frontroute" %%% "frontroute" % "0.13.3",
        ("org.scala-js" %%% "scalajs-dom" % "1.1.0")
          .cross(CrossVersion.for3Use2_13),
        ("io.github.cquiroz" %%% "scala-java-time" % "2.2.2") // maybe we could somehow get rid of this...
          .cross(CrossVersion.for3Use2_13)
      ),
      jsEnv := new JSDOMNodeJSEnv(),
      scalaJSUseMainModuleInitializer := true,
      scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.ESModule) },     
      build := {
        (Compile / fullLinkJS).value
        "npm run snowpack -- build" !
      },
    )
    .dependsOn(shared.js)

lazy val backend = project
  .in(file("backend"))
  .enablePlugins(JavaAppPackaging, AshScriptPlugin, DockerPlugin)
  .settings(
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % "1.0.9",
      "dev.zio" %% "zio-streams" % "1.0.9",
      "io.github.vigoo" %% "zio-aws-netty" % zioAwsVersion,
      "io.github.vigoo" %% "zio-aws-codepipeline" % zioAwsVersion,
      "io.github.vigoo" %% "zio-aws-codebuild" % zioAwsVersion,
      "io.github.vigoo" %% "zio-aws-sts" % zioAwsVersion,
      "org.typelevel" %% "cats-core" % "2.6.1",
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "io.d11" %% "zhttp" % "1.0.0.0-RC17"
    ),

    // packaging
    packageName := "aws-ci-dashboard",
    dockerBaseImage := "openjdk:8-jre-alpine",
    dockerEnvVars :=
      Map(
        // todo: this should not be done like that, only for testing
        "AWS_ACCESS_KEY_ID" -> scala.sys.env("AWS_ACCESS_KEY_ID"),
        "AWS_SECRET_ACCESS_KEY" -> scala.sys.env("AWS_SECRET_ACCESS_KEY"),
        "APP_PORT" -> scala.sys.env("APP_PORT")
      ),
    dockerExposedPorts ++= Seq(
      scala.sys.env("APP_PORT").toInt
    ),
    Universal / mappings ++= directory("backend/src/main/resources/public"),

    build := {
      (Docker / stage).value
    }
  )
  .dependsOn(shared.jvm)
