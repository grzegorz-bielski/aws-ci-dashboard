#!/usr/bin/env bash

echo "loading local .env file"
if [ -f .env ]
then
  # shellcheck disable=SC2046
  export $(cat .env | grep -v '#' | sed 's/\r$//' | awk '/=/ {print $1}' )
fi

echo "logging to ECR"
aws ecr get-login-password --region "$AWS_REGION" | docker login --username AWS --password-stdin "$AWS_ACCOUNT".dkr.ecr."$AWS_REGION".amazonaws.com


echo "building"
sbt build

echo "deploying"
pulumi up -y --cwd infra