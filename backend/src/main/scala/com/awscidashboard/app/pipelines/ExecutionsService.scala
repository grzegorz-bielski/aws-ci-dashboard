package com.awscidashboard.app.pipelines

import zio.console.*
import zio.stream.*
import zio.*

import io.github.vigoo.zioaws.core.AwsError
import io.github.vigoo.zioaws.codepipeline.CodePipeline
import io.github.vigoo.zioaws.codepipeline.model.*
import io.circe.syntax.given
import io.circe.Decoder
import io.circe.parser.parse
import cats.syntax.functorFilter.given

import com.awscidashboard.models.PipelineModels.*

trait ExecutionsService:
  def getLatestPipelineExecution(pipelineName: String): IO[AwsError, Option[PipelineExecutionModel]]
    def retryExecution(pipelineName: String, stageName: String, pipelineExecutionId: String): IO[AwsError, Unit]

object ExecutionsService:
  def getLatestPipelineExecution(pipelineName: String): ZIO[Has[ExecutionsService], AwsError, Option[PipelineExecutionModel]] =
    ZIO.serviceWith(_.getLatestPipelineExecution(pipelineName))

  def retryExecution(
      pipelineName: String,
      stageName: String,
      pipelineExecutionId: String
  ): ZIO[Has[ExecutionsService], AwsError, Unit] =
    ZIO.serviceWith(_.retryExecution(pipelineName, stageName, pipelineExecutionId))


final class ExecutionsServiceImpl(codepipeline: CodePipeline.Service) extends ExecutionsService:
  def retryExecution(pipelineName: String, stageName: String, pipelineExecutionId: String) =
    codepipeline
      .retryStageExecution(
        RetryStageExecutionRequest(
          pipelineName,
          stageName,
          pipelineExecutionId,
          StageRetryMode.FAILED_ACTIONS
        )
      )
      .map(_ => ())

  def getLatestPipelineExecution(pipelineName: String): IO[AwsError, Option[PipelineExecutionModel]] =
    codepipeline
      .listPipelineExecutions(ListPipelineExecutionsRequest(pipelineName, maxResults = Some(1)))
      .run(Sink.head)
      .map(_.flatMap(_.editable.pipelineExecutionId))
      .flatMap(_.map(getPipelineExecution(pipelineName, _)).getOrElse(ZIO.succeed(None))) // todo: use traverse

  private def getPipelineExecution(pipelineName: String, executionId: String) =
    given decodeRevisionSummaryFromAWS: Decoder[RevisionSummaryModel] = Decoder { c =>
      c.get[String]("ProviderType").flatMap { case "GitHub" =>
        c.get[String]("CommitMessage").map(RevisionSummaryModel.GitHub(_))
      }
    }

    codepipeline
      .getPipelineExecution(GetPipelineExecutionRequest(pipelineName, executionId))
      .map(_.editable.pipelineExecution)
      .map {
        _.flatMap { exec =>
          // todo: use par tuple from cats
          for
            id <- exec.pipelineExecutionId
            name <- exec.pipelineName
            version <- exec.pipelineVersion
            status <- exec.status
            latestRevision <- exec.artifactRevisions
              .flatMap(_.headOption)
              .flatMap(
                _.revisionSummary.flatMap(
                  parse(_).flatMap(_.as[RevisionSummaryModel]).toOption
                )
              )
          yield PipelineExecutionModel(
            id,
            name,
            version,
            status.toString.asInstanceOf[PipelineExecStatus],
            latestRevision
          )
        }
      }

object ExecutionsServiceImpl:
  lazy val layer: URLayer[Has[CodePipeline.Service], Has[ExecutionsService]] =
    (ExecutionsServiceImpl(_)).toLayer
