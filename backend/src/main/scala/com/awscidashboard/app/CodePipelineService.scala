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
      state <- getPipelineState(pipelineName)
      latestExecution <- getLatestPipelineExecution(pipelineName)
    yield PipelineDetailsModel(
      name = pipelineName,
      version = state.pipelineVersion,
      created = state.created,
      updated = state.updated,
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

  // private def getPipelineStages(state: GetPipelineStateResponse) =
  //   state.stageStates.map(_.toVector) getOrElse Vector.empty

  private def getLatestPipelineExecution(pipelineName: String): IO[AwsError, Option[PipelineExecutionModel]] =
    codepipeline
      .listPipelineExecutions(ListPipelineExecutionsRequest(pipelineName, maxResults = Some(1)))
      .run(Sink.head)
      .map(_.flatMap(_.editable.pipelineExecutionId))
      .flatMap(_.map(getPipelineExecution(pipelineName, _)).getOrElse(ZIO.succeed(None))) //todo: use traverse

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
