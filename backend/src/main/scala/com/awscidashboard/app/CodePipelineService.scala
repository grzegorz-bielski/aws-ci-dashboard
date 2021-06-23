package com.awscidashboard.app

import zio.console.*
import zio.stream.*
import zio.*

import io.github.vigoo.zioaws.core.AwsError
import io.github.vigoo.zioaws.codepipeline.CodePipeline
import io.github.vigoo.zioaws.codepipeline.model.*
import io.circe.Codec

trait CodePipelineService:
  def getPipelines(): IO[AwsError, Vector[PipelineSummaryModel]]

object CodePipelineService:
  def getPipelines(): ZIO[Has[CodePipelineService], AwsError, Vector[PipelineSummaryModel]] =
    ZIO.serviceWith[CodePipelineService](_.getPipelines())

final class CodePipelineServiceImpl(console: Console.Service, codepipeline: CodePipeline.Service)
    extends CodePipelineService:

  def getPipelines(): IO[AwsError, Vector[PipelineSummaryModel]] =
    codepipeline
      .listPipelines(ListPipelinesRequest())
      .map(_.editable)
      .map { r => PipelineSummaryModel(r.name, r.version) }
      .run(Sink.foldLeft(Vector.empty[PipelineSummaryModel])(_ :+ _))
      .flatMap { r =>
        console
          .putStrLn(r.toString)
          .map(_ => r)
          .mapError(AwsError.fromThrowable(_))
      }

  def getPipelineState(name: String): IO[AwsError, GetPipelineStateResponse] =
    codepipeline
      .getPipelineState(GetPipelineStateRequest(name))
      .map(_.editable)

object CodePipelineServiceImpl:
  lazy val layer: URLayer[Has[Console.Service] with Has[CodePipeline.Service], Has[CodePipelineService]] =
    (CodePipelineServiceImpl(_, _)).toLayer

case class PipelineSummaryModel(
    name: Option[String],
    version: Option[Int]
) derives Codec.AsObject
