package com.awscidashboard.app

import zio.*
import zio.stream.*
import zhttp.http.*
import zhttp.service.Server

import io.github.vigoo.zioaws.netty.{default as httpClient}
import io.github.vigoo.zioaws.core.config.{AwsConfig, default as awsConfig}
import io.github.vigoo.zioaws.codepipeline
import io.circe.syntax.given
import java.nio.file.FileSystems

import com.awscidashboard.app.statics.*
import com.awscidashboard.app.pipelines.*

object Main extends App:
  val port = 8090
  
  override def run(_args: List[String]) =
    println(s"Starting app at $port port")

    Server
      .start(port, app)
      .provideCustomLayer(appLayer)
      .exitCode

  lazy val app = pipelinesController +++ staticsController

  lazy val appLayer =
    // this is a mess ... waiting for ZIO 2 inject
    val runtimeLayer = ZEnv.live
    val awsLayer = httpClient >>> awsConfig >>> codepipeline.live
    val sharedLayer = runtimeLayer ++ awsLayer

    val execLayer = sharedLayer >>> ExecutionsServiceImpl.layer

    ((sharedLayer ++ execLayer) >>> CodePipelineServiceImpl.layer) ++ execLayer