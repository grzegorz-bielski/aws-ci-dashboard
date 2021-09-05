#!/usr/bin/env bash
ACCOUNT=$1
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin "$ACCOUNT".dkr.ecr.us-east-1.amazonaws.com