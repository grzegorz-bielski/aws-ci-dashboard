package com.awscidashboard.app

import zio.*
import zhttp.http.*
import zhttp.service.Server

import io.github.vigoo.zioaws.netty.{default as httpClient}
import io.github.vigoo.zioaws.core.config.{AwsConfig, default as awsConfig}
import io.github.vigoo.zioaws.codepipeline

object Main extends App:
  // def run(args: List[String]) =
  //   program
  //     .provideCustomLayer(appLayer)
  //     .exitCode

  // lazy val program =
  //   CodePipelineService.getPipelines()

  // lazy val appLayer =
  //   val runtimeLayer = ZEnv.live
  //   val awsLayer = httpClient >>> awsConfig >>> codepipeline.live

  //   (runtimeLayer ++ awsLayer) >>> CodePipelineServiceImpl.layer

  // Create HTTP route
  val app: HttpApp[Any, Nothing] = HttpApp.collect {
    case Method.GET -> Root / "text" => Response.text("Hello World!")
    case Method.GET -> Root / "json" => Response.jsonString("""{"greetings": "Hello World!"}""")
  }

  // Run it like any simple app
  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    Server.start(8090, app.silent).exitCode
