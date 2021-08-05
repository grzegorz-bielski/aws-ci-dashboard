package com.awscidashboard.app.pipelines

import com.raquo.laminar.api.L.*

import com.awscidashboard.app.HttpService
import com.awscidashboard.models.CodePipelineModels.*
import com.awscidashboard.app.Remote

trait PipelineService:
    lazy val httpService: HttpService

    def pipelineSummaryPoll() =
        poll().flatMap(_ => httpService.GET[Vector[PipelineSummaryModel]]("/api/pipelines"))

    def pipelineDetailsPoll(pipelineName: String) = 
        poll().flatMap(_ => httpService.GET[PipelineDetailsModel](s"/api/pipelines/$pipelineName"))

    private def poll(period: Int = 3000) =
        EventStream.periodic(period).toSignal(0)