package com.awscidashboard.app

import scala.concurrent.Future
import scala.util.Try.*
import scala.concurrent.ExecutionContext.Implicits.global // standard JS event loop

import org.scalajs.dom.experimental.Fetch.*
import org.scalajs.dom.experimental.HttpMethod

import com.raquo.laminar.api.L.Signal
import io.circe.{Codec, Decoder}
import io.circe.parser.decode
import scala.util.Success
import scala.util.Failure
import com.raquo.airstream.core.EventStream
import com.raquo.airstream.web.AjaxEventStream

trait HttpService {
  def GET[A: Decoder](endpoint: String): EventStream[Remote[A]]
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


      // // maybe I can just use com.raquo.airstream.web.AjaxEventStream
      // fetch(
      //   endpoint,
      //   new {
      //     override val method = HttpMethod.GET
      //   }
      // ).toFuture
      //   .flatMap(_.text.toFuture)
      //   .flatMap(decode[A](_).mapLeft(_.fillInStackTrace).toFuture)
      //   .toRemoteSignal


        
  // most of these extensions should go to some ops class
  // extension [A <: Throwable, B](e: Either[A, B])
  //   def toFuture: Future[B] =
  //     e.fold(Future.failed(_), Future.successful(_))

  // extension [A](future: Future[A])
  //   def toRemoteSignal: Signal[Remote[A]] =
  //     EventStream
  //       .fromValue((), emitOnce = true)
  //       .flatMap(_ => future)
  //       .recoverToTry
  //       .map { 
  //         case Success(a) => Remote.Success(a)
  //         case Failure(e) => Remote.Failure(e)
  //       }
  //       .startWith(Remote.Pending)

  // extension [A, B](e: Either[A, B])
  //   def mapLeft[A1](f: A => A1): Either[A1, B] = e match
  //     case Left(a) => Left(f(a))
  //     case _       => e.asInstanceOf[Either[A1, B]]
