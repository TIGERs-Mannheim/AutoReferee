#!/bin/bash

EXEC_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

if ! type mvn &> /dev/null; then
	echo "Please add the maven executable to your \$PATH"
	exit 1
fi

# the productive=true flag controls some behavior for real games like automatic recording
mvn -pl modules/autoreferee-main exec:exec -Dmaven.repo.local=repository \
    -Dexec.args="-Dautoref.mode=PASSIVE -Dproductive=true \
    -Xms128m -Xmx1G -server -Xnoclassgc -Xverify:none -Dsun.java2d.d3d=false -XX:+UseG1GC -Djava.net.preferIPv4Stack=true -XX:-OmitStackTraceInFastThrow \
    -classpath %classpath \
    edu.tigers.autoref.AutoReferee"

if [ "$?" != "0" ]; then
	echo
	echo
	echo "Launching the program failed. Did you build the application using 'mvn install'???'"
fi
