#!/bin/bash

ARGS="$@"


if [[ ! -d autoReferee ]]; then
  ./gradlew run --args="${ARGS}"
else
  # if the font size is too small (e.g. on high resolution screens), you can add following arguments to the command below.
  # 16 is the font size and can be adapted. You can also choose other font types.
  # export AUTO_REFEREE_OPTS="-Dswing.plaf.metal.controlFont='sans-serif-16' -Dswing.plaf.metal.userFont='sans-serif-16'"

  autoReferee/bin/autoReferee ${ARGS}
fi
