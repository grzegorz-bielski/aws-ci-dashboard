#!/usr/bin/env bash

echo "loading local .env file"
if [ -f .env ]
then
  # shellcheck disable=SC2046
  export $(cat .env | grep -v '#' | sed 's/\r$//' | awk '/=/ {print $1}' )
fi

sbt $1