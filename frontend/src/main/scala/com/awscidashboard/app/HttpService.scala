package com.awscidashboard.app

import scala.concurrent.Future
import scala.util.Try.*
import scala.concurrent.ExecutionContext.Implicits.global // standard JS event loop

import org.scalajs.dom.experimental.Fetch.*
import org.scalajs.dom.experimental.HttpMethod

import com.raquo.laminar.api.L.Signal
import io.circe.{Codec, Decoder, Encoder}
import io.circe.syntax.given
import io.circe.parser.decode
import scala.util.Success
import scala.util.Failure
import com.raquo.airstream.core.EventStream
import com.raquo.airstream.web.AjaxEventStream
import io.circe.Encoder

trait HttpService {
  def GET[A: Decoder](endpoint: String): EventStream[Remote[A]]
  // fire and forget
  def POST[B: Encoder](endpoint: String, data: B): EventStream[Unit]
}

object HttpService:
  lazy val live = new HttpService:
    override def GET[A: Decoder](endpoint: String): EventStream[Remote[A]] =
      AjaxEventStream.get(endpoint)
        .map(_.responseText)
        .map(decode[A](_))
        .recoverToTry
        .map(_.flatMap(_.toTry))
        .map {
          case Success(a) => Remote.Success(a)
          case Failure(e) => Remote.Failure(e)
        }

    override def POST[B: Encoder](endpoint: String, data: B) = 
        AjaxEventStream.post(
          endpoint,
          data.asJson.toString
        )
        .recoverToTry
        .map(_ => ())