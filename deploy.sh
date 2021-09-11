#!/usr/bin/env bash

echo "logging to ECR"
aws ecr get-login-password --region eu-central-1 | docker login --username AWS --password-stdin 876580015220.dkr.ecr.eu-central-1.amazonaws.com

echo "loading local .env file"
if [ -f .env ]
then
  # shellcheck disable=SC2046
  export $(cat .env | grep -v '#' | sed 's/\r$//' | awk '/=/ {print $1}' )
fi

echo "building"
sbt build

echo "deploying"
pulumi up -y --cwd infra