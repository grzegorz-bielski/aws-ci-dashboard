package com.awscidashboard.app.pipelines

import org.scalajs.dom
import scala.scalajs.js
import scala.scalajs.js.annotation.*

import com.raquo.laminar.api.L.{given, *}

import com.awscidashboard.models.CodePipelineModels.*
import com.awscidashboard.app.LaminarOps.{given, *}

lazy val PipelineStages = (stages: Vector[PipelineStageModel]) =>
    ol(
      cls("pipeline-stages"),
      stages.map { stage =>
        li(
          cls("pipeline-stages__stage"),
          div(s"stage name: ${stage.name.mkString}"),
          div(
            s"status: ${stage.latestExecution.map(_.executionId).mkString} - ${stage.latestExecution.map(_.status).mkString}"
          ),
            ol(
              stage.actions.map { action =>
                li(
                  cls("pipeline-stages__stage-action"),
                  div(s"action name: ${action.name}"),
                  a("entity", href := action.entityUrl.mkString),
                  a("revision", href := action.revisionUrl.mkString),
                  action.latestExecution.map { e =>
                    div(
                      div(s"status: ${e.executionId.mkString} - ${e.status.mkString}"),
                      div(s"summary: ${e.summary.mkString}"),
                      div(s"lastStatusChange: ${e.lastStatusChange.mkString}"),
                      div(s"token: ${e.token.mkString}"),
                      div(s"lastUpdatedBy: ${e.lastUpdatedBy.mkString}"),
                      div(s"externalExecutionId: ${e.externalExecutionId.mkString}"),
                      div(s"externalExecutionUrl: ${e.externalExecutionUrl.mkString}"),
                      div(s"percentComplete: ${e.percentComplete.mkString}"),
                      div(s"errorDetails: ${e.errorDetails.mkString}")
                    )
                  }
                )
              }
            )
        )
      }
    )

