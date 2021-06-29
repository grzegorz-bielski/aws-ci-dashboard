package com.awscidashboard.app

import org.scalajs.dom
import scala.scalajs.js
import scala.scalajs.js.annotation.*

import com.raquo.laminar.api.L.*
import com.awscidashboard.app.AppRouter

object Main:
  def main(args: Array[String]): Unit =
    // val App = Pipelines
    render(
      dom.document.querySelector("#app"),
      div(
        child <-- AppRouter()
      )
    )
