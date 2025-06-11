@echo off
echo Corevent Development Mode
echo.

echo Compiling project...
.\mvnw.cmd clean compile

echo.
echo Running in development mode...
.\mvnw.cmd exec:java -Dexec.mainClass="com.corevent.CoreventApplication"

pause 