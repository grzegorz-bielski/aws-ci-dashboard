package com.awscidashboard.app.statics

import zio.*
import zio.stream.*
import zhttp.http.*

import com.awscidashboard.app.{*, given}

lazy val staticsController = HttpApp
  .collect {
    case Method.GET -> Root / "scripts" / script =>
      Response.http(
        content = resourceAt(s"scripts/$script"),
        headers = List(
          Header.custom("content-type", "application/javascript")
        )
      )

    case Method.GET -> Root / "styles" / styleSheet =>
      Response.http(
        content = resourceAt(s"styles/$styleSheet"),
        headers = List(
          Header.custom("content-type", "text/css")
        )
      )

    case _ =>
      Response.http(content = resourceAt("index.html"))
  }

private def resourceAt(path: String) =
  HttpData.fromStream(ZStream.fromResource(s"public/$path"))
