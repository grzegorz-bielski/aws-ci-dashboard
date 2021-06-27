package com.awscidashboard.models

import io.circe.{Codec, Decoder, Encoder, Json, HCursor}
import io.circe.syntax.given
import java.time.Instant

object CodePipelineModels:
  type PipelineExecStatus = "Cancelled" | "InProgress" | "Stopped" | "Stopping" | "Succeeded" | "Superseded" |
    "Failed" | "UNKNOWN_TO_SDK_VERSION"

  given Codec[PipelineExecStatus] = stringUnionCodec

  type StageExecStatus = "Cancelled" | "InProgress" | "Stopped" | "Stopping" | "Succeeded" | "Failed" |
    "UNKNOWN_TO_SDK_VERSION"

  given Codec[StageExecStatus] = stringUnionCodec

  case class PipelineDetailsModel(
      name: String,
      version: Option[Int],
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

  case class StageExecutionModel(
      executionId: String,
      status: StageExecStatus
  ) derives Codec.AsObject

  enum RevisionSummaryModel derives Codec.AsObject:
    case GitHub(msg: String)

  // I'd prefer not to need that but
  // Circe cannot handle codecs like `String | String`
  def stringUnionCodec[T <: String] =
    Codec.from(
      Decoder.decodeString.map(_.asInstanceOf[T]),
      Encoder(a => Json.fromString(a.asInstanceOf[String]))
    )
