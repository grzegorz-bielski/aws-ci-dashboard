package com.awscidashboard.app

import zio.*
import zio.stream.*
import zhttp.http.*
import java.nio.file.FileSystems

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
  s"../build/$path"
    |> (p => FileSystems.getDefault.getPath(p).normalize.toAbsolutePath)
    |> (p => ZStream.fromFile(p))
    |> HttpData.fromStream
