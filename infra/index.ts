import * as awsx from "@pulumi/awsx"
import { config as dotEnvConfig } from "dotenv"
import * as path from "path"

dotEnvConfig({
  path: path.resolve(__dirname, "../.env"),
})

const cluster = new awsx.ecs.Cluster("aws-ci-dashboard-cluster")

const alb = new awsx.elasticloadbalancingv2.ApplicationLoadBalancer(
  "aws-ci-dashboard-lb",
  { securityGroups: cluster.securityGroups }
)

const atg = alb.createTargetGroup("aws-ci-dashboard-tg", {
  port: Number.parseInt(process.env.APP_PORT ?? ""),
  protocol: "HTTP",
})

const al = atg.createListener("aws-ci-dashboard-al", { port: 80 })

const image = awsx.ecs.Image.fromPath(
  "aws-ci-dashboard",
  path.resolve(__dirname, "../backend/target/docker/stage")
)

const appService = new awsx.ecs.FargateService("aws-ci-dashboard", {
  cluster,
  taskDefinitionArgs: {
    container: {
      image,
      portMappings: [al],
    },
  },
})

export const url = al.endpoint.hostname
