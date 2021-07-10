package com.awscidashboard.app

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

import com.awscidashboard.models.CodePipelineModels.*

trait CodePipelineService:
  def getPipelinesSummaries(): IO[AwsError, Vector[PipelineSummaryModel]]
  def getPipelineDetails(pipelineName: String): IO[AwsError, PipelineDetailsModel]

object CodePipelineService:
  def getPipelinesSummaries(): ZIO[Has[CodePipelineService], AwsError, Vector[PipelineSummaryModel]] =
    ZIO.serviceWith(_.getPipelinesSummaries())

  def getPipelineDetails(pipelineName: String): ZIO[Has[CodePipelineService], AwsError, PipelineDetailsModel] =
    ZIO.serviceWith(_.getPipelineDetails(pipelineName))

final class CodePipelineServiceImpl(console: Console.Service, codepipeline: CodePipeline.Service)
    extends CodePipelineService:

  def getPipelinesSummaries(): IO[AwsError, Vector[PipelineSummaryModel]] =
    getPipelines().flatMap { p =>
      ZIO.collectAll(
        p.mapFilter(_.name.map(getPipelineSummary))
      )
    }

  def getPipelineDetails(pipelineName: String): IO[AwsError, PipelineDetailsModel] = 
    for
      state <- getPipelineState(pipelineName)
      latestExecution <- getLatestPipelineExecution(pipelineName)
      stages = state.stageStates.map(_.toVector) getOrElse Vector.empty
      stagesModels = stages.map { s =>
        PipelineStageModel(
          name = s.stageName,
          latestExecution = s.latestExecution.map { e =>
            StageExecutionModel(
              e.pipelineExecutionId,
              e.status.toString.asInstanceOf[StageExecStatus]
            )
          },
          actions = s.actionStates
            .map {
              _.toVector.map { a =>
                PipelineStageActionModel(
                  name = a.actionName,
                  entityUrl = a.entityUrl,
                  revisionUrl = a.entityUrl,
                  latestExecution = a.latestExecution.map { e =>
                    PipelineStageActionExecutionModel(
                      executionId = e.actionExecutionId,
                      status = e.status.map(_.toString.asInstanceOf[ActionExecStatus]),
                      summary = e.summary,
                      lastStatusChange = e.lastStatusChange,
                      token = e.token,
                      lastUpdatedBy = e.lastUpdatedBy,
                      externalExecutionId = e.externalExecutionId,
                      externalExecutionUrl = e.externalExecutionUrl,
                      percentComplete = e.percentComplete,
                      errorDetails = e.errorDetails.map(er => ErrorDetailsModel(er.code, er.message))
                    )
                  }
                )
              }
            }
            .getOrElse(Vector.empty)
        )
      }

    yield PipelineDetailsModel(
      name = pipelineName,
      version = state.pipelineVersion,
      created = state.created,
      updated = state.updated, 
      latestExecution = latestExecution,
      stages = stagesModels
    )

  private def getPipelineSummary(pipelineName: String) =
    for
      state <- getPipelineState(pipelineName)
      latestExecution <- getLatestPipelineExecution(pipelineName)
    yield PipelineSummaryModel(
      name = pipelineName,
      latestExecution = latestExecution
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
      

  private def getPipelineState(name: String) =
    codepipeline
      .getPipelineState(GetPipelineStateRequest(name))
      .map(_.editable)

  private def getLatestPipelineExecution(pipelineName: String): IO[AwsError, Option[PipelineExecutionModel]] =
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
          println(exec)
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

object CodePipelineServiceImpl:
  lazy val layer: URLayer[Has[Console.Service] with Has[CodePipeline.Service], Has[CodePipelineService]] =
    (CodePipelineServiceImpl(_, _)).toLayer

// extension [R, E, T](op: Option[ZIO[R, E, Option[T]]])
//   def toZIO: ZIO[R, E, Option[T]] = op.getOrElse(ZIO.succeed(Option.empty[T]))
