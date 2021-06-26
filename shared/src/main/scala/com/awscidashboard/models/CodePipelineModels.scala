package com.awscidashboard.models

import io.circe.{Codec, Decoder, Encoder, Json}
import io.circe.syntax.given
import java.time.Instant

object CodePipelineModels:
  type PipelineExecStatus = "Cancelled" | "InProgress" | "Stopped" | "Stopping" | "Succeeded" | "Superseded" |
    "Failed" | "UNKNOWN_TO_SDK_VERSION"

  type StageExecStatus = "Cancelled" | "InProgress" | "Stopped" | "Stopping" | "Succeeded" | "Failed" |
    "UNKNOWN_TO_SDK_VERSION"

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
      status: PipelineExecStatus,
      latestRevision: RevisionSummaryModel
  ) derives Codec.AsObject

  given Decoder[PipelineExecStatus] = Decoder.decodeString.map(_.asInstanceOf[PipelineExecStatus])
  given Encoder[PipelineExecStatus] = Encoder(a => Json.fromString(a.asInstanceOf[String]))

  case class StageExecutionModel(
      executionId: String,
      status: StageExecStatus
  ) derives Codec.AsObject

  given Decoder[StageExecStatus] = Decoder.decodeString.map(_.asInstanceOf[StageExecStatus])
  given Encoder[StageExecStatus] = Encoder(a => Json.fromString(a.asInstanceOf[String]))

  enum RevisionSummaryModel derives Codec.AsObject:
    case GitHub(msg: String)
