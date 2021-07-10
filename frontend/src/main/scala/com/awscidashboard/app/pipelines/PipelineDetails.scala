package com.awscidashboard.app.pipelines

import org.scalajs.dom
import scala.scalajs.js
import scala.scalajs.js.annotation.*

import com.raquo.laminar.api.L.{given, *}

import com.awscidashboard.models.CodePipelineModels.*

import com.awscidashboard.app.HttpService
import com.awscidashboard.app.LaminarOps.{given, *}

lazy val PipelineDetails = (pipelineName: String) =>
  div(
    // cls("container", "is-fluid"),
    h2(
      cls("title"),
      pipelineName
    ),
    child <-- HttpService.GET[PipelineDetailsModel](s"/api/pipelines/$pipelineName").map {
      case None => span("Nothing yet")
      case Some(pipeline) =>
        span(
          // cls("pipelines__list"),
          // pipelines.map(Pipeline)
          pipeline.toString
        )
    }
  )
