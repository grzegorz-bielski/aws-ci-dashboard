import org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv

val zioAwsVersion = "3.16.79.3"
val circeVersion = "0.14.1"

ThisBuild / scalaVersion := "3.0.0"
ThisBuild / organization := "com.awscidashboard"
ThisBuild / name := "aws-ci-dashboard"
ThisBuild / version := "0.0.1-SNAPSHOT"

lazy val root = project
  .in(file("."))
  .aggregate(
    // shared,
    backend,
    frontend
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
        ("org.scala-js" %%% "scalajs-dom" % "1.1.0")
          .cross(CrossVersion.for3Use2_13),
        "com.raquo" %%% "laminar" % "0.13.0",
        ("io.github.cquiroz" %%% "scala-java-time" % "2.2.2") // maybe we could somehow get rid of this...
          .cross(CrossVersion.for3Use2_13)
      ),
      jsEnv := new JSDOMNodeJSEnv(),
      scalaJSUseMainModuleInitializer := true,
      scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.ESModule) },
      scalaJSLinkerConfig ~= { _.withSourceMap(false) }
    )
    .dependsOn(shared.js)

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
      "org.typelevel" %% "cats-core" % "2.6.1",
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "io.d11" %% "zhttp" % "1.0.0.0-RC17"
    )
  )
  .dependsOn(shared.jvm)
