package com.awscidashboard.app.pipelines

import com.raquo.laminar.api.L.*

import com.awscidashboard.app.HttpService
import com.awscidashboard.models.CodePipelineModels.*
import com.awscidashboard.app.Remote

// transforming polls to Signals so same results won't cause re-renders
// rel: https://github.com/raquo/Airstream/issues/19
trait PipelineService:
  lazy val httpService: HttpService

  def pipelineSummaryPoll(): Signal[Remote[Vector[PipelineSummaryModel]]] =
    poll(httpService.GET[Vector[PipelineSummaryModel]]("/api/pipelines"))
      .startWith(Remote.Pending)

  def pipelineDetailsPoll(pipelineName: String): Signal[Remote[PipelineDetailsModel]] =
    poll(httpService.GET[PipelineDetailsModel](s"/api/pipelines/$pipelineName"))
      .startWith(Remote.Pending)

  private def poll[T](request: => EventStream[Remote[T]], delayTime: Int = 3000): EventStream[Remote[T]] =
    // TODO: try making it tailrec
    request.flatMap {
      case res @ Remote.Success(_) =>
        val current = EventStream.fromValue(res)

        EventStream.merge(
          current,
          current.delay(delayTime).flatMap(_ => poll(request))
        )
      case a => EventStream.fromValue(a)
    }
