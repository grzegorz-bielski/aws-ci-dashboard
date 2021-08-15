package com.awscidashboard.app.pipelines

import zio.*
import zhttp.http.*
import io.circe.syntax.given

// todo: make the error responses saner
lazy val pipelinesController = 
 HttpApp
    .collectM {
      case Method.GET -> Root / "api" / "pipelines" =>
        CodePipelineService
          .getPipelinesSummaries()
          .map(p => Response.jsonString(p.asJson.toString))
          .orElseSucceed(Response.jsonString("""{"error": "500"}"""))

      case Method.GET -> Root / "api" / "pipelines" / pipelineName =>
        CodePipelineService
          .getPipelineDetails(pipelineName)
          .map(p => Response.jsonString(p.asJson.toString))
          .orElseSucceed(Response.jsonString("""{"error": "500"}"""))

       case Method.GET -> Root / "api" => 
        ZIO.succeed(Response.jsonString("""{"error": "404"}"""))
    }
