package com.awscidashboard.app

import zio.*
import zio.stream.*
import zhttp.http.*
import zhttp.service.Server
import scala.sys

import com.awscidashboard.app.statics.*
import com.awscidashboard.app.pipelines.*
object Main extends App:
  lazy val port = sys.env("APP_PORT").toInt // todo: unsafe

  override def run(_args: List[String]) =
    println(s"Starting app at $port port")

    Server
      .start(port, app)
      .provideCustomLayer(Layers.app)
      .exitCode

  lazy val app = pipelinesController +++ staticsController
