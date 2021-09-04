package com.awscidashboard.app.statics

import zio.*
import zio.stream.*
import zhttp.http.*
import java.nio.file.{ FileSystems, Paths}
import java.io.File

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


// todo: serve built assets from within docker container 
private def resourceAt(path: String) =
  // s"./backend/src/main/resources/public/$path"
  //   |> (p => FileSystems.getDefault.getPath(p).normalize.toAbsolutePath)
  //   |> (p => ZStream.fromFile(p))
  //   |> HttpData.fromStream
  val xd = getClass.getResource(s"public/$path").getPath
  HttpData.fromStream(ZStream.fromFile(Paths.get(xd)))

  // path
  //   |> getClass.getResource
  //   |> (p => new File(p.getPath))
  //   |> (p => ZStream.fromFile(p))
  //   |> HttpData.fromStream

