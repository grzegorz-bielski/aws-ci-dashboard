package com.awscidashboard.app

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global // standard JS event loop

import org.scalajs.dom.experimental.Fetch.*
import org.scalajs.dom.experimental.HttpMethod

import com.raquo.laminar.api.L.Signal
import io.circe.{Codec, Decoder}
import io.circe.parser.decode

object HttpService:
  extension [A <: Throwable, B](e: Either[A, B])
    def toFuture: Future[B] =
      e.fold(Future.failed(_), Future.successful(_))

  extension [A, B](e: Either[A, B])
    def mapLeft[A1](f: A => A1): Either[A1, B] = e match
      case Left(a) => Left(f(a))
      case _       => e.asInstanceOf[Either[A1, B]]

  def GET[A: Decoder](endpoint: String) =
    val result =
      fetch(
        endpoint,
        new {
          override val method = HttpMethod.GET
        }
      ).toFuture
        .flatMap(_.text.toFuture)
        .flatMap(res => decode[A](res).mapLeft(_.fillInStackTrace).toFuture)

    Signal.fromFuture(result) // todo: use Remote type instead of Option
