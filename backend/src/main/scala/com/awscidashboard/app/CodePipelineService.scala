package com.awscidashboard.app

import zio.console.*
import zio.stream.*
import zio.*

import io.github.vigoo.zioaws.core.AwsError
import io.github.vigoo.zioaws.codepipeline.CodePipeline
import io.github.vigoo.zioaws.codepipeline.model.*
import io.circe.Codec
import cats.syntax.functorFilter.given

trait CodePipelineService:
  def getPipelinesDetails(): IO[AwsError, Vector[PipelineDetailsModel]]

object CodePipelineService:
  def getPipelinesDetails(): ZIO[Has[CodePipelineService], AwsError, Vector[PipelineDetailsModel]] =
    ZIO.serviceWith[CodePipelineService](_.getPipelinesDetails())

final class CodePipelineServiceImpl(console: Console.Service, codepipeline: CodePipeline.Service)
    extends CodePipelineService:

  def getPipelinesDetails(): IO[AwsError, Vector[PipelineDetailsModel]] =
    getPipelines().flatMap { p =>
      ZIO.collectAll(
        p.mapFilter(_.name.map(getPipelineDetails))
      )
    }

  private def getPipelineDetails(pipelineName: String) =
    for
      stages <- getPipelineStages(pipelineName)
      execId <- getLatestPipelineExecutionId(pipelineName)
      revision <- execId.map(getLatestRevision(pipelineName, _)) getOrElse ZIO.succeed(None)
    yield PipelineDetailsModel(
      name = pipelineName,
      revision = revision,
      stages = stages.map { s =>
        PipelineStageModel(
          s.stageName,
          s.latestExecution.map(e => StageExecutionModel(e.pipelineExecutionId, e.status.toString))
        )
      }
    )

  private def getPipelines(): IO[AwsError, Vector[PipelineSummary]] =
    codepipeline
      .listPipelines(ListPipelinesRequest())
      .map(_.editable)
      .run(Sink.foldLeft(Vector.empty[PipelineSummary])(_ :+ _))
      .flatMap { r =>
        console
          .putStrLn(r.toString)
          .map(_ => r)
          .mapError(AwsError.fromThrowable(_))
      }

  private def getPipelineStages(name: String) =
    codepipeline
      .getPipelineState(GetPipelineStateRequest(name))
      .map {
        _.editable.stageStates.map(_.toVector) getOrElse Vector.empty
      }

  private def getLatestPipelineExecutionId(pipelineName: String) =
    codepipeline
      .listPipelineExecutions(ListPipelineExecutionsRequest(pipelineName, maxResults = Some(1)))
      .run(Sink.head)
      .map(_.flatMap(_.editable.pipelineExecutionId))

  private def getLatestRevision(pipelineName: String, execId: String) =
    codepipeline
      .getPipelineExecution(
        GetPipelineExecutionRequest(pipelineName, execId)
      )
      .map(
        _.editable.pipelineExecution
          .flatMap(_.artifactRevisions)
          .flatMap(_.headOption)
          .flatMap(_.revisionSummary)
      )

object CodePipelineServiceImpl:
  lazy val layer: URLayer[Has[Console.Service] with Has[CodePipeline.Service], Has[CodePipelineService]] =
    (CodePipelineServiceImpl(_, _)).toLayer
case class PipelineDetailsModel(
    name: String,
    revision: Option[String],
    stages: Vector[PipelineStageModel]
) derives Codec.AsObject

case class PipelineStageModel(
    name: Option[String],
    latestExecution: Option[StageExecutionModel]
) derives Codec.AsObject

case class StageExecutionModel(
    executionId: String,
    status: String // todo: refine
) derives Codec.AsObject
