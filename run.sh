#!/bin/bash

EXEC_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

if ! type mvn &> /dev/null; then
	echo "Please add the maven executable to your \$PATH"
	exit 1
fi

mode="`echo $1 | tr '[:lower:]' '[:upper:]'`"
if [ "$mode" != "ACTIVE" ] && [ "$mode" != "PASSIVE" ]; then
    echo "Please specify a valid mode to start with: active | passive"
    exit 1
fi

# the productive=true flag controls some behavior for real games like automatic recording
# recording has a significant memory impact (both heap size and disk space).
autoRecording="-Dproductive=false"

# if the font size is too small (e.g. on high resolution screens), you can add following arguments to the command below:
# -Dswing.plaf.metal.controlFont='sans-serif-16' -Dswing.plaf.metal.userFont='sans-serif-16' \
# 16 is the font size and can be adapted. You can also choose other font types.

mvn -pl modules/autoreferee-main exec:exec -Dmaven.repo.local=repository \
    --no-snapshot-updates \
    -Dexec.args="-Dautoref.mode=$mode $autoRecording \
    -Xms128m -Xmx1G -server -Xnoclassgc -Xverify:none -Dsun.java2d.d3d=false -XX:+UseG1GC -Djava.net.preferIPv4Stack=true -XX:-OmitStackTraceInFastThrow \
    -classpath %classpath \
    edu.tigers.autoref.AutoReferee"

if [ "$?" != "0" ]; then
	echo
	echo
	echo "Launching the program failed. Did you build the application using 'mvn install'???'"
fi
