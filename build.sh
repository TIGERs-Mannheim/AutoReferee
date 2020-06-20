#!/usr/bin/env bash

./gradlew build -x test
rm -rf autoReferee
unzip build/distributions/autoReferee.zip
