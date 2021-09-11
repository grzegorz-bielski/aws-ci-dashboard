package com.awscidashboard.app

import zio.*
import io.github.vigoo.zioaws.netty.{default as httpClient}
import io.github.vigoo.zioaws.core.config.{AwsConfig, configured, CommonAwsConfig}
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.auth.credentials.{AwsCredentials, AwsCredentialsProvider}
import io.github.vigoo.zioaws.codepipeline
import scala.sys

import com.awscidashboard.app.statics.*
import com.awscidashboard.app.pipelines.*

object Layers:
  lazy private val awsLayer =
    val credentialsProvider = new AwsCredentialsProvider:
      def resolveCredentials() =
        new AwsCredentials:
          // unsafe
          def accessKeyId() = sys.env("AWS_ACCESS_KEY_ID")
          def secretAccessKey() = sys.env("AWS_SECRET_ACCESS_KEY")

    val commonAwsLayer = ZLayer.succeed(
      CommonAwsConfig(
        region = Some(Region.EU_CENTRAL_1),
        endpointOverride = None,
        commonClientConfig = None,
        credentialsProvider = credentialsProvider
      )
    )

    (httpClient ++ commonAwsLayer) >>> configured() >>> codepipeline.live

  lazy val app =
    val runtimeLayer = ZEnv.live
    val sharedLayer = runtimeLayer ++ awsLayer

    val execLayer = sharedLayer >>> ExecutionsServiceImpl.layer

    ((sharedLayer ++ execLayer) >>> CodePipelineServiceImpl.layer) ++ execLayer
