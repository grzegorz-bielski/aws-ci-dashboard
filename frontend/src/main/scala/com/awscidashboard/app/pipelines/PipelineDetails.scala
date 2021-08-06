package com.awscidashboard.app.pipelines

import org.scalajs.dom
import scala.scalajs.js
import scala.scalajs.js.annotation.*

import com.raquo.laminar.api.L.{given, *}

import com.awscidashboard.models.CodePipelineModels.*

import com.awscidashboard.app.Remote
import com.awscidashboard.app.HttpService
import com.awscidashboard.app.LaminarOps.{given, *}

def PipelineDetails(pipelineName: String)(using pipelineService: PipelineService) =
  // it might be worth extracting it to higher order component
  // (can't be in service since we need an Owner instance provided by Laminar, unless we want to implement it :sweat)
  lazy val v = Var(Remote.Initial.asInstanceOf[Remote[PipelineDetailsModel]])
  lazy val pipeline$ = v.signal
  lazy val pipelineObserver = v.updater[Remote[PipelineDetailsModel]] {
    case (Remote.Success(a), Remote.Pending) => Remote.Success(a)
    case (_, next)                           => next 
  }

  div(
    header(
      cls("pipeline-details-header"),
      h2(
        cls("pipeline-details-header__heading"),
        pipelineName
      ),
      Pills(pipeline$)
    ),

    pipelineService.pipelineDetailsPoll(pipelineName) --> pipelineObserver,
    child <-- pipeline$.map {
      case Remote.Initial    => div("nothing yet")
      case Remote.Pending    => div("loading")
      case Remote.Failure(e) => div(s"error: ${e.toString}")
      case Remote.Success(pipeline) =>
       PipelineStages(pipeline)
    }
  )

private def Pills(pipeline$ : Signal[Remote[PipelineDetailsModel]]) =
  div(
    cls("field", "is-grouped"),
    child <-- pipeline$.map {
      case Remote.Success(pipeline) =>
        div(
          cls("control"),
          pipeline.latestExecution.map(_.status).map { status =>
            div(
              cls("tags", "has-addons"),
              span(
                cls(
                  "tag",
                  "is-medium",
                  status match
                    case "Succeeded" => "is-success"
                    case "Failed"    => "is-danger"
                    case _           => "is-info"
                ),
                status
              ),

              status match 
                case "Failed" => 
                  span(cls("tag", "is-medium", "fas", "fa-sync-alt"))
                case "InProgress" => 
                  span(cls("tag", "is-medium", "fas", "fa-pause"))
                case _ => ""
            )
          }
        )
      case _ => ""
    }
  )