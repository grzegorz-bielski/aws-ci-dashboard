import org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv

val zioAwsVersion = "3.16.79.3"

ThisBuild / scalaVersion := "3.0.0"
ThisBuild / organization := "com.awscidashboard"
ThisBuild / name := "aws-ci-dashboard"
ThisBuild / version := "0.0.1-SNAPSHOT"

lazy val root = project
  .in(file("."))
  .aggregate(
    backend,
    frontend
  )

lazy val frontend =
  project
    .in(file("frontend"))
    .enablePlugins(ScalaJSPlugin)
    .settings(
      libraryDependencies ++= Seq(
        ("org.scala-js" %%% "scalajs-dom" % "1.1.0")
          .cross(CrossVersion.for3Use2_13)
      ),
      jsEnv := new JSDOMNodeJSEnv(),
      scalaJSUseMainModuleInitializer := true,
      scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.ESModule) },
      scalaJSLinkerConfig ~= { _.withSourceMap(false) }
    )

lazy val backend = project
  .in(file("backend"))
  .settings(
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % "1.0.9",
      "dev.zio" %% "zio-streams" % "1.0.9",
      "io.github.vigoo" %% "zio-aws-netty" % zioAwsVersion,
      "io.github.vigoo" %% "zio-aws-codepipeline" % zioAwsVersion,
      "io.github.vigoo" %% "zio-aws-codebuild" % zioAwsVersion,
      "io.github.vigoo" %% "zio-aws-sts" % zioAwsVersion,
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "io.d11" %% "zhttp" % "1.0.0.0-RC17"
    )
  )
