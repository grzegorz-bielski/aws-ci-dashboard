package com.awscidashboard.app

import org.scalajs.dom
import scala.scalajs.js.annotation.*

import com.raquo.laminar.api.L.{*, given}

import com.awscidashboard.models.CodePipelineModels.*

import com.awscidashboard.app.LaminarOps.{*, given}

object Main:
  def main(args: Array[String]): Unit =

    val Header = h1("Dashboard", cls := "title")

    val Pipeline = (pipeline: PipelineDetailsModel) =>
      import pipeline.{latestExecution, version, name}

      li(
        cls("is-clickable"),
        // width := "320px",
        article(
          cls("message", "card"),
          // cls := "card",
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
            cls("message-body"),
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

    val Pipelines = div(
      cls("container", "is-fluid"),
      h2(
        cls("mb-5"),
        "Pipelines"
      ),
      child <-- HttpService.GET[Vector[PipelineDetailsModel]]("/api/pipelines").map {
        case None => span("Nothing yet")
        case Some(pipelines) =>
          ul(
            // cls := "columns",
            // cls("is-flex", "is-flex-wrap-wrap"),
            pipelines.map(Pipeline)
          )
      }
    )

    val App = div(
      Header,
      Pipelines
    )

    render(
      dom.document.querySelector("#app"),
      App
    )
