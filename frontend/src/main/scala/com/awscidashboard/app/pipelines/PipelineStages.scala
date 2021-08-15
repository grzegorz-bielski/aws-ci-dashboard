package com.awscidashboard.app.pipelines

import org.scalajs.dom
import scala.scalajs.js
import scala.scalajs.js.annotation.*

import com.raquo.laminar.api.L.{given, *}

import com.awscidashboard.models.CodePipelineModels.*
import com.awscidashboard.app.LaminarOps.{given, *}
import java.time.Instant

def PipelineStages(pipeline: PipelineDetailsModel) =
  lazy val revisionDetails = pipeline.latestExecution
    .map(_.latestRevision)
    .collect { case RevisionSummaryModel.GitHub(msg) => s"Github: $msg" }
    .mkString

  ol(
    cls("pipeline-stages"),
    pipeline.stages.zipWithIndex.map { (stage, i) =>
      if stage.actions.size == 1 then 
        CollapsedStage(
          stage,
            if i == 0 
            then
              a(
                revisionDetails,
                href := stage.actions.head.revisionUrl.mkString
              )
            else None
          
        )
      else FullStage(stage)
    }
  )

private def FullStage(stage: PipelineStageModel) =
  // println(("full stage", stage))

  li(
    cls("pipeline-stages__stage", "pipeline-stages--full-stage"),
    header(
      cls("pipeline-stages__full-stage-name"),
           span(
      cls("has-text-weight-semibold"),
      stage.name.mkString
    )
    ),
    ol(
      cls("pipeline-stages__stage-actions"),
      stage.actions.map { action =>
        println((action.name, action.runOrder))
        li(
          cls("pipeline-stages__stage-action", "message"),
          cls :?= getActionStatus(action),
          StageAction(
              action
            )
        )
      }
    )
  )

private def CollapsedStage(stage: PipelineStageModel, actionSlots: Mod[HtmlElement]*) =
  val firstAction = stage.actions.headOption

  li(
    cls("pipeline-stages__stage", "message"),
    cls :?= firstAction.flatMap(getActionStatus),
    firstAction.map { action =>
      StageAction(
        action,
        bodySlots = actionSlots
      )
    }
  )

private def StageAction(action: PipelineStageActionModel, bodySlots: Mod[HtmlElement]*) =
  action.latestExecution
    .map { e =>
      div(
        cls("message-body"),
        StatusHeader(action.name, e.lastStatusChange),
        bodySlots,
        e.errorDetails.map { err =>
          code(
            s"${err.code.mkString} - ${err.message.mkString}"
          )
        }
      )
    }
    .getOrElse(
      div(
        cls("message-body"),
        div(
          StatusHeader(action.name)
        )
      )
    )

private def StatusHeader(name: Option[String], lastStatusChange: Option[Instant] = None) =
  header(
    span(
      cls("has-text-weight-semibold"),
      name.mkString
    ),
    span(
      " | "
    ),
    span(
      lastStatusChange.map(_.toString).getOrElse("Didn't run")
    )
  )

private def getActionStatus(action: PipelineStageActionModel) =
    action.latestExecution.flatMap(_.status).collect {
      case "Succeeded"  => "is-success"
      case "Failed"     => "is-danger"
      case "InProgress" => "is-info"
    }