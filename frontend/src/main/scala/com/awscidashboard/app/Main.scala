package com.awscidashboard.app

import org.scalajs.dom
import scala.scalajs.js
import scala.scalajs.js.annotation.*

import com.raquo.laminar.api.L.*
import com.awscidashboard.app.AppRouter
import com.awscidashboard.app.pipelines.PipelineService

object Main:
  def main(args: Array[String]): Unit =
    given PipelineService = new PipelineService:
      lazy val httpService = HttpService.live

    render(
      dom.document.querySelector("#app"),
      div(
        cls("container", "is-fluid", "mt-4"),
        child <-- AppRouter
      )
    )
