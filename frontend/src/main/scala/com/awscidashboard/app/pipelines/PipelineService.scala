package com.awscidashboard.app.pipelines

import com.raquo.laminar.api.L.*

import com.awscidashboard.app.HttpService
import com.awscidashboard.models.PipelineModels.*
import com.awscidashboard.app.Remote

trait PipelineService:
  lazy val httpService: HttpService

  def retryPipelineExecution(pipelineName: String, stageName: String, pipelineExecutionId: String) =
    httpService.POST(
      s"/api/pipelines/$pipelineName/retry",
      PipelineExecutionRetryModel(
        stageName,
        pipelineExecutionId
      )
    )


  def pipelineSummaryPoll(): EventStream[Remote[Vector[PipelineSummaryModel]]] =
    poll(pipelineSummary())

  def pipelineSummary() = httpService.GET[Vector[PipelineSummaryModel]]("/api/pipelines")

  def pipelineDetails(pipelineName: String) = httpService.GET[PipelineDetailsModel](s"/api/pipelines/$pipelineName")

  def pipelineDetailsPoll(pipelineName: String): EventStream[Remote[PipelineDetailsModel]] =
    poll(pipelineDetails(pipelineName))

  private def poll[T](request: => EventStream[Remote[T]], delayTime: Int = 3000): EventStream[Remote[T]] =
    // todo: try making it tailrec
    request.flatMap {
      case res @ Remote.Success(_) =>
        val current = EventStream.fromValue(res)

        EventStream.merge(
          current,
          current.delay(delayTime).flatMap(_ => poll(request))
        )
      case a => EventStream.fromValue(a)
    }
