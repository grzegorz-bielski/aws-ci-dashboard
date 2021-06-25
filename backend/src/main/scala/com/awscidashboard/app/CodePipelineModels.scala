package com.awscidashboard.app

import io.circe.{Codec, Decoder, Encoder}
import io.circe.syntax.given
import io.github.vigoo.zioaws.codepipeline.model.{StageExecutionStatus, PipelineExecutionStatus}
import java.time.Instant

object CodePipelineModels:
  case class PipelineDetailsModel(
      name: String,
      version: Option[Int],
      revision: Option[RevisionSummaryModel],
      created: Option[Instant],
      updated: Option[Instant],
      latestExecution: Option[PipelineExecutionModel]
  ) derives Codec.AsObject

  case class PipelineStageModel(
      name: Option[String],
      latestExecution: Option[StageExecutionModel]
  ) derives Codec.AsObject

  case class PipelineExecutionModel(
      id: String,
      name: String,
      version: Int,
      status: PipelineExecutionStatus,
      latestRevision: RevisionSummaryModel
  ) derives Codec.AsObject

  given Encoder[PipelineExecutionStatus] = Encoder(_.toString.asJson)

  case class StageExecutionModel(
      executionId: String,
      status: StageExecutionStatus
  ) derives Codec.AsObject

  given Encoder[StageExecutionStatus] = Encoder(_.toString.asJson)

  enum RevisionSummaryModel derives Encoder.AsObject:
    case GitHub(msg: String)

  object RevisionSummaryModel:
    given Decoder[RevisionSummaryModel] = Decoder { c =>
      c.get[String]("ProviderType").flatMap { case "GitHub" =>
        c.get[String]("CommitMessage").map(RevisionSummaryModel.GitHub(_))
      }
    }
