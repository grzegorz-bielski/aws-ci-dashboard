package com.awscidashboard.app

import org.scalajs.dom
import org.scalajs.dom.document
import org.scalajs.dom.experimental.Fetch.*
import org.scalajs.dom.experimental.HttpMethod
import scala.scalajs.js.annotation.JSExportTopLevel

import scala.concurrent.Future

import com.raquo.laminar.api.L.{*, given}

import io.circe.Codec
import io.circe.parser.decode
import io.circe.syntax.given

object Main:
  extension [A <: Throwable, B](e: Either[A, B])
    def toFuture: Future[B] =
      e.fold(Future.failed(_), Future.successful(_))

  extension [A, B](e: Either[A, B])
    def mapLeft[A1](f: A => A1): Either[A1, B] = e match
      case Left(a) => Left(f(a))
      case _       => e.asInstanceOf[Either[A1, B]]

  case class Todo(
      id: Int,
      userId: Int,
      title: String,
      completed: Boolean
  ) derives Codec.AsObject

  def main(args: Array[String]): Unit =
    println("halko")
    dom.console.log("halko")

    val name$ = Var("world")

    val InputBox = div(
      label("Your name: "),
      input(
        onMountFocus,
        placeholder := "Enter your name here",
        onInput.mapToValue --> name$
      ),
      span(
        "Hello, ",
        child.text <-- name$.signal.map(_.toUpperCase)
      )
    )

    val Stuff = div(
      h2("Stuff below"),
      child.text <-- HttpService.GET("https://jsonplaceholder.typicode.com/todos/1").map {
        case Some(todo) => todo.title
        case None       => "Nothing yet"
      }
    )

    val App = div(
      InputBox,
      Stuff
    )

    dom.window.addEventListener(
      "DOMContentLoaded",
      _ =>
        render(
          dom.document.querySelector("#app"),
          App
        )
    )

  end main
  object HttpService:
    // standard JS event loop
    import scala.concurrent.ExecutionContext.Implicits.global

    def GET(endpoint: String) =
      val result =
        fetch(
          endpoint,
          new {
            override val method = HttpMethod.GET
          }
        ).toFuture
          .flatMap(_.text.toFuture)
          .flatMap(res => decode[Todo](res).mapLeft(_.fillInStackTrace).toFuture)

      Signal.fromFuture(result) // todo: use Remote type instead of Option
