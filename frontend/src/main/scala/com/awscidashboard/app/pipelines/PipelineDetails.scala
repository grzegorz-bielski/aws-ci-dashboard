package com.awscidashboard.app.pipelines

import org.scalajs.dom
import scala.scalajs.js
import scala.scalajs.js.annotation.*

import com.raquo.laminar.api.L.{given, *}

import com.awscidashboard.models.PipelineModels.*

import com.awscidashboard.app.Remote
import com.awscidashboard.app.HttpService
import com.awscidashboard.app.LaminarOps.{given, *}

def PipelineDetails(pipelineName: String)(using pipelineService: PipelineService) =
  val retryBus = EventBus[Unit]()
  val pipeline$ = EventStream.merge(
    pipelineService.pipelineDetailsPoll(pipelineName),
    retryBus.events.flatMap(_ => pipelineService.pipelineDetails(pipelineName))
) .startWith(Remote.Pending)

  div(
    header(
      cls("pipeline-details-header"),
      h2(
        cls("pipeline-details-header__heading"),
        pipelineName
      ),
      Pills(pipeline$, clickObserver = retryBus.writer)
    ),

    // todo: use split operator
    child <-- pipeline$
      .map {
        case Remote.Initial    => div("nothing yet")
        case Remote.Pending    => div("loading")
        case Remote.Failure(e) => div(s"error: ${e.toString}")
        case Remote.Success(pipeline) =>
          PipelineStages(pipeline)
      }
  )

private def Pills(pipeline$ : Signal[Remote[PipelineDetailsModel]], clickObserver: Observer[Unit])(using pipelineService: PipelineService) =
  val clickBus = EventBus[Unit]()

  def getLatestFailedStage(stages: Vector[PipelineStageModel], execId: String) =
    stages.find { _.latestExecution.find(e => e.executionId == execId && e.status == "Failed").isDefined }

  def retryPipelineExec(pipeline: PipelineDetailsModel) = 
      for 
        execId <- pipeline.latestExecution.map(_.id).toEventStream
        stageName <- getLatestFailedStage(pipeline.stages, execId).flatMap(_.name).toEventStream
        _ <- pipelineService.retryPipelineExecution(pipeline.name, stageName, execId)
        // todo: refetch pipeline state
      yield ()


  div(
    cls("field", "is-grouped", "pipeline-details-header__pills"),
    child <-- pipeline$.map {
      case Remote.Success(pipeline) =>
        div(
          cls("control"),
          pipeline.latestExecution.map(_.status).map { status =>
            div(
              onClick.mapTo(()) --> clickBus,
              // todo: this should be really lifted to the PipelineDetails
              clickBus.events.flatMap(_ => retryPipelineExec(pipeline)) --> clickObserver,
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
