package com.awscidashboard.app

import zio.*

import io.github.vigoo.zioaws.netty.{default as httpClient}
import io.github.vigoo.zioaws.core.config.{AwsConfig, default as awsConfig}
import io.github.vigoo.zioaws.codepipeline

object Main extends zio.App:
  def run(args: List[String]) =
    program
      .provideCustomLayer(appLayer)
      .exitCode

  lazy val program =
    CodePipelineService.getPipelines()

  lazy val appLayer =
    val runtimeLayer = ZEnv.live
    val awsLayer = httpClient >>> awsConfig >>> codepipeline.live

    (runtimeLayer ++ awsLayer) >>> CodePipelineServiceImpl.layer
