package com.awscidashboard.app

import org.scalajs.dom
import scala.scalajs.js
import scala.scalajs.js.annotation.*

import com.raquo.laminar.api.L.{*, given}
import com.awscidashboard.app.pipelines.*
object Main:
  def main(args: Array[String]): Unit =
    val Header = h1("Dashboard", cls := "title")

    val App = div(
      Header,
      Pipelines
    )

    render(
      dom.document.querySelector("#app"),
      App
    )
