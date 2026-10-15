@echo off
set "JAVA_HOME=C:\Users\User\.jdks\openjdk-24.0.2+12-54"
set "PATH=%JAVA_HOME%\bin;%PATH%"
cd /d "c:\tarbajos de lp\AgroPeruvianFoods\backend"
echo Starting BomERP Backend (Spring Boot en http://localhost:8080)...
call mvnw.cmd spring-boot:run
