ECHO OFF

cd %~dp0

gradlew build -x test
