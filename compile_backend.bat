@echo off
set "JAVA_HOME=C:\Users\User\.jdks\openjdk-24.0.2+12-54"
set "PATH=%JAVA_HOME%\bin;%PATH%"
cd /d "c:\tarbajos de lp\AgroPeruvianFoods\backend"
echo Compiling backend with JAVA_HOME=%JAVA_HOME%...
call mvnw.cmd compile -DskipTests
