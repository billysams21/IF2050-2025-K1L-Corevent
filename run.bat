@echo off
echo Starting Corevent Desktop Application...
echo.

REM Check if JAR file exists
if not exist "target\Corevent-1.0.0-SNAPSHOT.jar" (
    echo JAR file not found. Building project...
    echo.
    .\mvnw.cmd clean package -DskipTests
    echo.
)

REM Run the application
echo Running Corevent...
java -jar target\Corevent-1.0.0-SNAPSHOT.jar

echo.
echo Application has stopped.
pause 