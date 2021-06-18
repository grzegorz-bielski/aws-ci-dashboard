val zioAwsVersion = "3.16.79.3"

lazy val root = (project in file("."))
  .settings(
    organization := "com.awscidashboard",
    name := "aws-ci-dashboard",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "3.0.0",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % "1.0.9",
      "dev.zio" %% "zio-streams" % "1.0.9",
      "io.github.vigoo" %% "zio-aws-netty" % zioAwsVersion,
      "io.github.vigoo" %% "zio-aws-codepipeline" % zioAwsVersion,
      "io.github.vigoo" %% "zio-aws-codebuild" % zioAwsVersion,
      "io.github.vigoo" %% "zio-aws-sts" % zioAwsVersion,
      "ch.qos.logback" % "logback-classic" % "1.2.3"
    )
  )
