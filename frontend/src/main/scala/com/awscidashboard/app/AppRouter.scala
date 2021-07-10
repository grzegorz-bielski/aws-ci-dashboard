package com.awscidashboard.app

import org.scalajs.dom
import com.raquo.laminar.api.L.*
import io.frontroute.*

import com.awscidashboard.app.pipelines.*

object AppRouter:
  lazy val (renders, route) = makeRoute[HtmlElement] { render =>
    concat(
      pathEnd {
        render(Pipelines)
      },
      path("pipelines") {
        render(Pipelines)
      },
      path("pipelines" / segment) { id =>
        render(PipelineDetails(id))
      }
    )
  }

  def apply() =
    runRoute(
      route,
      LocationProvider.browser(windowEvents.onPopState)
    )(unsafeWindowOwner)

    BrowserNavigation.emitPopStateEvent()
    LinkHandler.install()

    renders.map(_.getOrElse(span("Loading...")))
