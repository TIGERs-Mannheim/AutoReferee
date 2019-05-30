#!/usr/bin/env bash

args="${@}"
./mvnw clean install -Pfast -Dmaven.repo.local=repository ${args}
