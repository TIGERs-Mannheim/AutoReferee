#!/bin/sh
for url in $(echo "$BUILD_TRIGGERS" | sed "s/,/ /g"); do
  curl -X POST -H "Content-Type: application/json" --data "{ \"build\": true, \"source_name\": \"${GITHUB_REF#refs/*/}\" }" "$url"
done