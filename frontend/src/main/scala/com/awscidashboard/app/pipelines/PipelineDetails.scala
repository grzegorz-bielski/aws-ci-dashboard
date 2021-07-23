package com.awscidashboard.app.pipelines

import org.scalajs.dom
import scala.scalajs.js
import scala.scalajs.js.annotation.*

import com.raquo.laminar.api.L.{given, *}

import com.awscidashboard.models.CodePipelineModels.*

import com.awscidashboard.app.Remote
import com.awscidashboard.app.HttpService
import com.awscidashboard.app.LaminarOps.{given, *}

lazy val PipelineDetails = (pipelineName: String) =>
  lazy val pipeline$ = HttpService.GET[PipelineDetailsModel](s"/api/pipelines/$pipelineName")

  div(
    header(
      cls("pipeline-details-header"),
      h2(
        cls("title"),
        pipelineName
      ),
      Pills(pipeline$)
    ),
    child <-- pipeline$.map {
      case Remote.Initial => div("nothing yet")
      case Remote.Pending => div("loading")
      case Remote.Failure(e) => div(s"error: ${e.toString}")
      case Remote.Success(pipeline) =>
        div(
          div(s"execution: ${pipeline.latestExecution.map(_.id).mkString}"),
          div(s"revision: ${pipeline.latestExecution.map(_.latestRevision).collect { case RevisionSummaryModel.GitHub(msg) => s"Github: $msg" }.mkString}"),
          Stages(pipeline.stages)
        )
    }
  )


lazy val Pills = (pipeline$: Signal[Remote[PipelineDetailsModel]]) => 
    div(
      cls("field", "is-grouped"),
      child <-- pipeline$.map {
      case Remote.Success(pipeline) => 
          div(
            cls("control"),
            div(
              cls("tags"),  
              pipeline.latestExecution.map(_.status).map { status =>
                span(
                    cls(
                      "tag",
                      "is-large",
                      status match 
                        case "Succeeded" => "is-success"
                        case "Failed" => "is-danger"
                        case _ => "is-info"
                    ),
                    status
                  )
              }
            )
          )
      case _ => ""
    }
    )

lazy val Stages = (stages: Vector[PipelineStageModel]) => 
  div(
  span("stages"),
  ol(
    stages.map { stage => 
      li(
        div(s"name: ${stage.name.mkString}"),
        div(
          s"status: ${stage.latestExecution.map(_.executionId).mkString} - ${stage.latestExecution.map(_.status).mkString}"
        ),
        div(
          span("actions"),
          ol(
            stage.actions.map { action => 
              li(
                div(s"name: ${action.name}"),
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
      )
    }
  )
)