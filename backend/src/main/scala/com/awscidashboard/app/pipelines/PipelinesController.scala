package com.awscidashboard.app.pipelines

import zio.*
import zhttp.http.*
import io.circe.parser.decode
import io.circe.syntax.given

import com.awscidashboard.models.PipelineModels.*

lazy val pipelinesController =
  HttpApp
    .collectM {
      case Method.GET -> Root / "api" / "pipelines" =>
        CodePipelineService
          .getPipelinesSummaries()
          .map(p => Response.jsonString(p.asJson.toString))
          .orElseSucceed(
            Response.status(Status.INTERNAL_SERVER_ERROR)
          )

      case Method.GET -> Root / "api" / "pipelines" / pipelineName =>
        CodePipelineService
          .getPipelineDetails(pipelineName)
          .map(p => Response.jsonString(p.asJson.toString))
          .orElseSucceed(Response.status(Status.INTERNAL_SERVER_ERROR))

      case req @ Method.POST -> Root / "api" / "pipelines" / pipelineName / "retry" =>
        req.getBodyAsString
          .flatMap(decode[PipelineExecutionRetryModel](_).toOption) match
          case None => ZIO.succeed(Response.status(Status.BAD_REQUEST))
          case Some(form) =>
            ExecutionsService
              .retryExecution(
                pipelineName,
                form.stageName,
                form.pipelineExecutionId
              )
              .map(_ => Response.ok)
              .catchAll {
                case e => 
                  println(("error", e)) // todo: use zio logging lib
                  ZIO.succeed(Response.status(Status.INTERNAL_SERVER_ERROR))
              }

      case Method.GET -> Root / "api" =>
        ZIO.succeed(Response.status(Status.NOT_FOUND))
    }
