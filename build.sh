#!/usr/bin/env bash

./gradlew distZip
rm -rf autoReferee
unzip build/distributions/autoReferee.zip
