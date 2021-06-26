package com.awscidashboard.app

import org.scalajs.dom
import scala.scalajs.js.annotation.*

import com.raquo.laminar.api.L.{*, given}

import com.awscidashboard.models.CodePipelineModels.*

object Main:
  // @JSExportTopLevel(name = "xd", moduleID = "XD")
  // def xd(): Unit = println("Halko")

  def main(args: Array[String]): Unit =
    // println("halko")
    // dom.console.log("halko")

    // val name$ = Var("world")

    // val InputBox = div(
    //   label("Your name: "),
    //   input(
    //     onMountFocus,
    //     placeholder := "Enter your name here",
    //     onInput.mapToValue --> name$
    //   ),
    //   span(
    //     "Hello, ",
    //     child.text <-- name$.signal.map(_.toUpperCase)
    //   )
    // )

    val Header = h1("Dashboard", cls := "title")

    val Pipeline = (pipeline: PipelineDetailsModel) =>
      li(
        cls("column"),
        article(
          header(
            cls := "card-header",
            h3(
              pipeline.name,
              cls := "card-header-title"
            ),
            p(
              cls := "content",
              pipeline.version.map(_.toString).getOrElse(""),
              pipeline.created.map(_.toString).getOrElse(""),
              pipeline.updated.map(_.toString).getOrElse(""),
              pipeline.revision.map { case RevisionSummaryModel.GitHub(msg) => msg }.getOrElse(""),
              pipeline.latestExecution.map(a => a.toString).getOrElse("")
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
            cls := "columns",
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
