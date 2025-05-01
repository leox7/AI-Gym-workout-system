@echo off
echo Starting AI Gym Workout System...

REM Set the path to your project
set PROJECT_DIR=C:\Users\Admin\CascadeProjects\AIGymWorkoutSystem

REM Set the path to your Maven repository
set M2_REPO=%USERPROFILE%\.m2\repository

REM Create a lib directory if it doesn't exist
if not exist "%PROJECT_DIR%\lib" mkdir "%PROJECT_DIR%\lib"

REM Set MySQL connector path
set MYSQL_CONNECTOR=%PROJECT_DIR%\lib\mysql-connector-j-8.0.33.jar
set MYSQL_CONNECTOR_URL=https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.0.33/mysql-connector-j-8.0.33.jar

REM Download MySQL connector if it doesn't exist
if not exist "%MYSQL_CONNECTOR%" (
    echo MySQL connector not found. Downloading...
    powershell -Command "(New-Object System.Net.WebClient).DownloadFile('%MYSQL_CONNECTOR_URL%', '%MYSQL_CONNECTOR%')"
    echo Download complete.
)

REM Set JavaFX paths
set JAVAFX_VERSION=17.0.2
set JAVAFX_BASE=%M2_REPO%\org\openjfx\javafx-base\%JAVAFX_VERSION%\javafx-base-%JAVAFX_VERSION%.jar
set JAVAFX_CONTROLS=%M2_REPO%\org\openjfx\javafx-controls\%JAVAFX_VERSION%\javafx-controls-%JAVAFX_VERSION%.jar
set JAVAFX_FXML=%M2_REPO%\org\openjfx\javafx-fxml\%JAVAFX_VERSION%\javafx-fxml-%JAVAFX_VERSION%.jar
set JAVAFX_GRAPHICS=%M2_REPO%\org\openjfx\javafx-graphics\%JAVAFX_VERSION%\javafx-graphics-%JAVAFX_VERSION%.jar
set JAVAFX_MEDIA=%M2_REPO%\org\openjfx\javafx-media\%JAVAFX_VERSION%\javafx-media-%JAVAFX_VERSION%.jar

REM Set platform-specific JavaFX paths
set JAVAFX_BASE_WIN=%M2_REPO%\org\openjfx\javafx-base\%JAVAFX_VERSION%\javafx-base-%JAVAFX_VERSION%-win.jar
set JAVAFX_CONTROLS_WIN=%M2_REPO%\org\openjfx\javafx-controls\%JAVAFX_VERSION%\javafx-controls-%JAVAFX_VERSION%-win.jar
set JAVAFX_FXML_WIN=%M2_REPO%\org\openjfx\javafx-fxml\%JAVAFX_VERSION%\javafx-fxml-%JAVAFX_VERSION%-win.jar
set JAVAFX_GRAPHICS_WIN=%M2_REPO%\org\openjfx\javafx-graphics\%JAVAFX_VERSION%\javafx-graphics-%JAVAFX_VERSION%-win.jar
set JAVAFX_MEDIA_WIN=%M2_REPO%\org\openjfx\javafx-media\%JAVAFX_VERSION%\javafx-media-%JAVAFX_VERSION%-win.jar

REM Set dependency paths
set SLF4J_API=%M2_REPO%\org\slf4j\slf4j-api\2.0.7\slf4j-api-2.0.7.jar
set LOGBACK_CLASSIC=%M2_REPO%\ch\qos\logback\logback-classic\1.4.11\logback-classic-1.4.11.jar
set LOGBACK_CORE=%M2_REPO%\ch\qos\logback\logback-core\1.4.11\logback-core-1.4.11.jar
set JSON=%M2_REPO%\org\json\json\20231013\json-20231013.jar
set HTTPCLIENT=%M2_REPO%\org\apache\httpcomponents\httpclient\4.5.14\httpclient-4.5.14.jar
set HTTPCORE=%M2_REPO%\org\apache\httpcomponents\httpcore\4.4.16\httpcore-4.4.16.jar
set COMMONS_LOGGING=%M2_REPO%\commons-logging\commons-logging\1.2\commons-logging-1.2.jar
set COMMONS_CODEC=%M2_REPO%\commons-codec\commons-codec\1.11\commons-codec-1.11.jar
set JBCRYPT=%M2_REPO%\org\mindrot\jbcrypt\0.4\jbcrypt-0.4.jar

REM Set the module path
set MODULE_PATH=%JAVAFX_BASE%;%JAVAFX_CONTROLS%;%JAVAFX_FXML%;%JAVAFX_GRAPHICS%;%JAVAFX_MEDIA%;%JAVAFX_BASE_WIN%;%JAVAFX_CONTROLS_WIN%;%JAVAFX_FXML_WIN%;%JAVAFX_GRAPHICS_WIN%;%JAVAFX_MEDIA_WIN%

REM Set the class path - include the downloaded MySQL connector
set CLASS_PATH=%PROJECT_DIR%\target\classes;%MYSQL_CONNECTOR%;%SLF4J_API%;%LOGBACK_CLASSIC%;%LOGBACK_CORE%;%JSON%;%HTTPCLIENT%;%HTTPCORE%;%COMMONS_LOGGING%;%COMMONS_CODEC%;%JBCRYPT%

REM Run the application
echo Running with Java version:
java -version
echo.
echo Starting application with MySQL connector: %MYSQL_CONNECTOR%
echo.

REM Ensure no preloader settings are passed to Java
set JAVA_OPTS=-Djavafx.preloader=

REM Run with the proper configuration
java %JAVA_OPTS% --module-path "%MODULE_PATH%" --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.media -cp "%CLASS_PATH%" com.aigym.app.Launcher

pause