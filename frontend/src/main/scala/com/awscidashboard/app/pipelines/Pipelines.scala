package com.awscidashboard.app.pipelines

import org.scalajs.dom
import scala.scalajs.js
import scala.scalajs.js.annotation.*

import com.raquo.laminar.api.L.{given, *}

import com.awscidashboard.models.CodePipelineModels.*

import com.awscidashboard.app.HttpService
import com.awscidashboard.app.LaminarOps.{given, *}

lazy val Pipelines = div(
  cls("container", "is-fluid", "pipelines"),
  h2(
    cls("pipelines__header", "title"),
    "Pipelines"
  ),
  // todo: use split operator, extract service to param
  child <-- HttpService.GET[Vector[PipelineDetailsModel]]("/api/pipelines").map {
    case None => span("Nothing yet")
    case Some(pipelines) =>
      ul(
        cls("pipelines__list"),
        pipelines.map(Pipeline)
      )
  }
)

private lazy val Pipeline = (pipeline: PipelineDetailsModel) =>
  import pipeline.{latestExecution, version, name}

  li(
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
          name ++ version.map(v => s" #$v").mkString
        ),
        span(
          latestExecution.map(_.status).mkString
        )
      ),
      div(
        cls("message-body", "pipeline__body"),
        // pipeline.version.map(_.toString).getOrElse(""),
        // pipeline.created.map(_.toString).getOrElse(""),
        // pipeline.updated.map(_.toString).getOrElse(""),
        code(
          latestExecution
            .map(_.latestRevision)
            .map { case RevisionSummaryModel.GitHub(msg) => s"GitHub: $msg" }
            .mkString
        )
      )
    )
  )
