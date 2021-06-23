package com.awscidashboard.app

import zio.*
import zhttp.http.*
import zhttp.service.Server

import io.github.vigoo.zioaws.netty.{default as httpClient}
import io.github.vigoo.zioaws.core.config.{AwsConfig, default as awsConfig}
import io.github.vigoo.zioaws.codepipeline
import io.circe.syntax.given

object Main extends App:
  override def run(_args: List[String]) =
    Server
      .start(8090, app)
      .provideCustomLayer(appLayer)
      .exitCode

  lazy val appLayer =
    val runtimeLayer = ZEnv.live
    val awsLayer = httpClient >>> awsConfig >>> codepipeline.live

    (runtimeLayer ++ awsLayer) >>> CodePipelineServiceImpl.layer

  lazy val app = HttpApp
    .collectM {
      case Method.GET -> Root / "pipelines" =>
        CodePipelineService
          .getPipelines()
          .map(p => Response.jsonString(p.asJson.toString))
          .orElseSucceed(Response.jsonString("""{"error": "500"}"""))

      case _ => ZIO.succeed(Response.jsonString("""{"error": "404"}"""))
    }
