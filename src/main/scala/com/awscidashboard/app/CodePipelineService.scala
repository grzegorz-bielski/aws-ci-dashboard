package com.awscidashboard.app

import zio.console.*
import zio.stream.*
import zio.*

import io.github.vigoo.zioaws.core.AwsError
import io.github.vigoo.zioaws.codepipeline.CodePipeline
import io.github.vigoo.zioaws.codepipeline.model.*

trait CodePipelineService:
  def getPipelines(): IO[AwsError, Vector[PipelineSummary]]

object CodePipelineService:
  def getPipelines(): ZIO[Has[CodePipelineService], AwsError, Vector[PipelineSummary]] =
    ZIO.serviceWith[CodePipelineService](_.getPipelines())

final class CodePipelineServiceImpl(console: Console.Service, codepipeline: CodePipeline.Service)
    extends CodePipelineService:

  def getPipelines(): IO[AwsError, Vector[PipelineSummary]] =
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

object CodePipelineServiceImpl:
  lazy val layer: URLayer[Has[Console.Service] with Has[CodePipeline.Service], Has[CodePipelineService]] =
    (CodePipelineServiceImpl(_, _)).toLayer
