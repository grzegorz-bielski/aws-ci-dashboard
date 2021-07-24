package com.awscidashboard.app.pipelines

import org.scalajs.dom
import scala.scalajs.js
import scala.scalajs.js.annotation.*

import com.raquo.laminar.api.L.{given, *}

import com.awscidashboard.models.CodePipelineModels.*

import com.awscidashboard.app.Remote
import com.awscidashboard.app.HttpService
import com.awscidashboard.app.LaminarOps.{given, *}


lazy val Pipelines = div(
  cls("pipelines"),
  h2(
    cls("pipelines__heading"),
    "pipelines"
  ),
  // todo: use split operator, extract service to param
  child <-- HttpService.GET[Vector[PipelineSummaryModel]]("/api/pipelines").map {
    case Remote.Initial => span("not started")
    case Remote.Pending => span("loading")
    case Remote.Failure(_) => span("error")
    case Remote.Success(pipelines) =>
      ul(
        cls("pipelines__list"),
        pipelines.map(Pipeline)
      )
  }
)

private lazy val Pipeline = (pipeline: PipelineSummaryModel) =>
  import pipeline.{latestExecution, name}

  li(
    a(
      href := s"/pipelines/$name",
      article(
        cls("message", "card", "pipeline"),
        cls :?= latestExecution.map(_.status).collect {
          case "Failed"     => "is-danger"
          case "Succeeded"  => "is-success"
          case "InProgress" => "is-info"
        },
        header(
          cls("message-header"),
          h3(
            cls("has-text-weight-bold", "is-size-6"),
            name
          ),
          span(
            latestExecution.map(_.status).mkString
          )
        ),
        div(
          cls("message-body", "pipeline__body"),
          code(
            latestExecution
              .map(_.latestRevision)
              .map { case RevisionSummaryModel.GitHub(msg) => s"GitHub: $msg" }
              .mkString
          )
        )
      )
    )
  )
