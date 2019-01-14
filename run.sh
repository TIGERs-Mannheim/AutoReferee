#!/bin/bash

if [[ ! -d "modules/autoreferee-main/target" ]]; then
    echo "Make sure to run ./build.sh first! It must also be executed after a code update or you will run with the old version."
    exit 1
fi

mode="`echo $1 | tr '[:lower:]' '[:upper:]'`"
if [[ "$mode" != "ACTIVE" ]] && [[ "$mode" != "PASSIVE" ]]; then
    echo "Please specify a valid mode to start with: active | passive"
    exit 1
fi

# the productive=true flag controls some behavior for real games like automatic recording
# recording has a significant memory impact (both heap size and disk space). You should increase Xmx below to 4G.
autoRecording="-Dproductive=false"

args="$@"

export CLASSPATH="modules/autoreferee-main/target/lib/*"

# if the font size is too small (e.g. on high resolution screens), you can add following arguments to the command below:
# -Dswing.plaf.metal.controlFont='sans-serif-16' -Dswing.plaf.metal.userFont='sans-serif-16' \
# 16 is the font size and can be adapted. You can also choose other font types.

JAVA_OPTS="-Xms64m -Xmx4G -server -Xnoclassgc -Xverify:none -Dsun.java2d.d3d=false -XX:+UseConcMarkSweepGC -Djava.net.preferIPv4Stack=true -XX:-OmitStackTraceInFastThrow -XX:+AggressiveOpts $autoRecording -Dautoref.mode=$mode"

java ${JAVA_OPTS} edu.tigers.autoref.AutoReferee ${args}