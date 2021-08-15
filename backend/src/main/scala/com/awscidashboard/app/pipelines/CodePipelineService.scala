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

trait CodePipelineService:
  def getPipelinesSummaries(): IO[AwsError, Vector[PipelineSummaryModel]]
  def getPipelineDetails(pipelineName: String): IO[AwsError, PipelineDetailsModel]

object CodePipelineService:
  def getPipelinesSummaries(): ZIO[Has[CodePipelineService], AwsError, Vector[PipelineSummaryModel]] =
    ZIO.serviceWith(_.getPipelinesSummaries())

  def getPipelineDetails(pipelineName: String): ZIO[Has[CodePipelineService], AwsError, PipelineDetailsModel] =
    ZIO.serviceWith(_.getPipelineDetails(pipelineName))

final class CodePipelineServiceImpl(console: Console.Service, codepipeline: CodePipeline.Service, executions: ExecutionsService)
    extends CodePipelineService:

  def getPipelinesSummaries(): IO[AwsError, Vector[PipelineSummaryModel]] =
    getPipelinesList().flatMap { p =>
      ZIO.collectAll(
        p.mapFilter(_.name.map(getPipelineSummary))
      )
    }

  def getPipelineDetails(pipelineName: String): IO[AwsError, PipelineDetailsModel] =
    for
      state <- getPipelineState(pipelineName)
      pipeline <- getPipeline(pipelineName)
      latestExecution <- executions.getLatestPipelineExecution(pipelineName)

      stageDeclarations = pipeline.map { _.stages.map(s => (s.name, s)).toMap }
      getStageDeclaration = (stageName: Option[String]) => 
          for 
            n <- stageName
            declarations <- stageDeclarations
            res <- declarations.lift(n)
          yield res

      stages = state.stageStates.map(_.toVector) getOrElse Vector.empty
      stagesModels = stages.map { s =>
        val stageDeclaration = getStageDeclaration(s.stageName)
        val actionsDeclarations = stageDeclaration.map(_.actions.map(a => (a.name, a)).toMap)
        val getActionDeclaration = (actionName: Option[String]) => 
            for 
              n <- actionName
              declarations <- actionsDeclarations
              res <- declarations.lift(n)
            yield res

        val actions = s.actionStates.map {
          _.toVector.map { a =>
              val actionDeclaration = getActionDeclaration(a.actionName)

              PipelineStageActionModel(
                name = a.actionName,
                entityUrl = a.entityUrl,
                revisionUrl = a.entityUrl,
                runOrder = actionDeclaration.flatMap(_.runOrder),
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

        val latestExecution = s.latestExecution.map { e =>
          StageExecutionModel(
            e.pipelineExecutionId,
            e.status.toString.asInstanceOf[StageExecStatus]
          )
        }

        PipelineStageModel(
          name = s.stageName,
          latestExecution = latestExecution,
          actions = actions
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
      latestExecution <- executions.getLatestPipelineExecution(pipelineName)
    yield PipelineSummaryModel(
      name = pipelineName,
      latestExecution = latestExecution
    )

  private def getPipeline(pipelineName: String) =
    codepipeline
      .getPipeline(GetPipelineRequest(pipelineName))
      .map(_.editable.pipeline)

  private def getPipelinesList(): IO[AwsError, Vector[PipelineSummary]] =
    // todo: paginate
    codepipeline
      .listPipelines(ListPipelinesRequest())
      .map(_.editable)
      .run(Sink.foldLeft(Vector.empty[PipelineSummary])(_ :+ _))

  private def getPipelineState(name: String) =
    codepipeline
      .getPipelineState(GetPipelineStateRequest(name))
      .map(_.editable)

object CodePipelineServiceImpl:
  lazy val layer: URLayer[Has[Console.Service] with Has[CodePipeline.Service] with Has[ExecutionsService], Has[CodePipelineService]] =
    (CodePipelineServiceImpl(_, _, _)).toLayer
