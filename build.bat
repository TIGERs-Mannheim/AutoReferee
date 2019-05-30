ECHO OFF

cd %~dp0

mvnw clean install -Pfast -Dmaven.repo.local=repository
