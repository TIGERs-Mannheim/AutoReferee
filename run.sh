#!/bin/bash

ARGS="$*"

if [[ -d "autoReferee" ]]; then
  echo "Starting prebuild distribution"
  cd autoReferee && bin/autoReferee
else
  echo "Build and run"
  if [[ -z "${ARGS}" ]]; then
    ./gradlew :run
  else
    ./gradlew :run --args="${ARGS}"
  fi
fi
