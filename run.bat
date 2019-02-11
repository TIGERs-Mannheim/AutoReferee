ECHO OFF

cd %~dp0

IF NOT EXIST "modules/autoreferee-main/target" (
    echo "Make sure to run build.bat first! It must also be executed after a code update or you will run with the old version."
    exit
)

REM Set default mode to PASSIVE. Possible modes: PASSIVE, ACTIVE
set mode="PASSIVE"

REM the productive=true flag controls some behavior for real games like automatic recording
REM recording has a significant memory impact (both heap size and disk space). You should increase Xmx below to 4G.
set autoRecording="-Dproductive=false"

set args=%*

set CLASSPATH="modules/autoreferee-main/target/lib/*"
set JAVA_OPTS="-Xms64m -Xmx4G -server -Xnoclassgc -Xverify:none -Dsun.java2d.d3d=false -XX:+UseConcMarkSweepGC -Djava.net.preferIPv4Stack=true -XX:-OmitStackTraceInFastThrow -XX:+AggressiveOpts %autoRecording% -Dautoref.mode=%mode%"

java %JAVA_OPTS% edu.tigers.autoref.AutoReferee %args%