import * as awsx from "@pulumi/awsx"

const repoImage = awsx.ecr.buildAndPushImage("aws-ci-dashboard", "../test-app")

const lb = new awsx.lb.NetworkListener("aws-ci-dashboard", { port: 80 })

const appService = new awsx.ecs.FargateService("aws-ci-dashboard", {
  taskDefinitionArgs: {
    container: {
      image: repoImage.image(),
      portMappings: [lb],
    },
  },
})

export const url = lb.endpoint.hostname
