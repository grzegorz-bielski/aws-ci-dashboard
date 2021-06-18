package com.awscidashboard.app

import zio.console.*
import zio.stream.*
import zio.ZIO

import io.github.vigoo.zioaws.core
import io.github.vigoo.zioaws.codepipeline
import io.github.vigoo.zioaws.codepipeline.model.*
import io.github.vigoo.zioaws.sts

import java.time.Instant

object Main extends zio.App:
  def run(args: List[String]) =
    val httpClient = io.github.vigoo.zioaws.netty.default
    val awsConfig = httpClient >>> core.config.default
    val aws = awsConfig >>> codepipeline.live

    program
      .provideCustomLayer(aws)
      .exitCode

  val program =
    type RawPipeline = PipelineSummary.ReadOnly
    lazy val streamReducer = (acc: Vector[RawPipeline], a: RawPipeline) => acc :+ a

    putStrLn("started") *>
      codepipeline
        .listPipelines(ListPipelinesRequest())
        .run(Sink.foldLeft(Vector.empty)(streamReducer))
        .map(_.map(_.editable))
        .flatMap(e => putStrLn(e.toString))
        // .foreach(_.name.flatMap(putStrLn(_)))
        // .run {
        //   Sink.foreach(_.name.flatMap(putStrLn(_)))
        // }
        .catchAll { error =>
          putStrLnErr(s"Failed with $error").ignore
        }
      *> putStrLn("stopped")

  val id = putStrLn("checking IAM") *> sts
    .getCallerIdentity(sts.model.GetCallerIdentityRequest())
    .flatMap { r =>
      ZIO.mapN(r.arn, r.account)(_ ++ " " ++ _)
    }
    .flatMap(putStrLn(_))

// case class Pipeline(
//     name: String,
//     version: String,
//     created: Instant,
//     updated: Instant
// )
