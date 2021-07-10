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

object Main extends App:
  override def run(_args: List[String]) =
    Server
      .start(8090, app +++ staticApp)
      .provideCustomLayer(appLayer)
      .exitCode

  lazy val appLayer =
    val runtimeLayer = ZEnv.live
    val awsLayer = httpClient >>> awsConfig >>> codepipeline.live

    (runtimeLayer ++ awsLayer) >>> CodePipelineServiceImpl.layer

  def fileAt(path: String) =
    FileSystems.getDefault.getPath(path).normalize.toAbsolutePath

  def resourceAt(path: String) =
    HttpData.fromStream(ZStream.fromFile(fileAt(path)))

  lazy val staticApp = HttpApp
    .collect {
      case Method.GET -> Root / "scripts" / script =>
        Response.http(
          content = resourceAt(s"./build/scripts/$script"),
          headers = List(
            Header.custom("content-type", "application/javascript")
          )
        )

      case Method.GET -> Root / "styles" / styleSheet =>
        Response.http(
          content = resourceAt(s"./build/styles/$styleSheet"),
          headers = List(
            Header.custom("content-type", "text/css")
          )
        )

      case _ =>
        Response.http(content = resourceAt("./build/index.html"))
    }

  lazy val app = HttpApp
    .collectM {
      case Method.GET -> Root / "api" / "pipelines" =>
        CodePipelineService
          .getPipelinesDetails()
          .map(p => Response.jsonString(p.asJson.toString))
          .orElseSucceed(Response.jsonString("""{"error": "500"}"""))

       case Method.GET -> Root / "api" => 
        ZIO.succeed(Response.jsonString("""{"error": "404"}"""))
    }
