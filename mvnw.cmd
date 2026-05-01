@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements. See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership. The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License. You may obtain a copy of the License at
@REM
@REM    http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied. See the License for the
@REM specific language governing permissions and limitations
@REM under the License.
@REM ----------------------------------------------------------------------------

@REM Apache Maven Wrapper startup batch script, version 3.3.2

@echo off
@setlocal

set ERROR_CODE=0

set "JAVA_HOME=%JAVA_HOME%"
set "MAVEN_PROJECTBASEDIR=%MAVEN_BASEDIR%"

@REM ==== VALIDATE JAVA ====
if not "%JAVA_HOME%" == "" goto OkJHome

for %%i in (java.exe) do @set "JAVA_EXE=%%~$PATH:i"
if not "%JAVA_EXE%" == "" goto OkJHome

echo.
echo Error: JAVA_HOME not found in your environment. >&2
echo Please set the JAVA_HOME variable to the location of your Java installation. >&2
echo.
goto error

:OkJHome

@REM ==== FIND PROJECT BASE DIR ====
set MAVEN_PROJECTBASEDIR=%MAVEN_BASEDIR%
IF NOT "%MAVEN_BASEDIR%" == "" goto endDetectBaseDir

set EXEC_DIR=%CD%
set WDIR=%EXEC_DIR%
:findBaseDir
IF EXIST "%WDIR%"\.mvn goto baseDirFound
cd ..
IF "%WDIR%"=="%CD%" goto baseDirNotFound
set "WDIR=%CD%"
goto findBaseDir

:baseDirFound
set "MAVEN_PROJECTBASEDIR=%WDIR%"
cd "%EXEC_DIR%"
goto endDetectBaseDir

:baseDirNotFound
set "MAVEN_PROJECTBASEDIR=%EXEC_DIR%"
cd "%EXEC_DIR%"

:endDetectBaseDir

set WRAPPER_JAR="%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar"
set WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain
set WRAPPER_URL="https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar"

FOR /F "usebackq tokens=1,2 delims==" %%A IN ("%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.properties") DO (
    IF "%%A"=="wrapperUrl" SET WRAPPER_URL=%%B
)

@REM Download maven-wrapper.jar if missing
if exist %WRAPPER_JAR% goto executeWrapper

if "%MVNW_VERBOSE%" == "true" (
    echo Downloading Maven wrapper jar from %WRAPPER_URL% ...
)

powershell -Command "&{"^
    "$webclient = new-object System.Net.WebClient;"^
    "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12;"^
    "$webclient.DownloadFile('%WRAPPER_URL%', '%WRAPPER_JAR%')"^
    "}"

if "%MVNW_VERBOSE%" == "true" (
    echo Finished downloading maven-wrapper.jar
)

:executeWrapper
"%JAVA_HOME%\bin\java.exe" ^
  %MAVEN_OPTS% ^
  -classpath %WRAPPER_JAR% ^
  "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" ^
  %WRAPPER_LAUNCHER% %MAVEN_CONFIG% %*

if ERRORLEVEL 1 goto error
goto end

:error
set ERROR_CODE=1

:end
@endlocal & set ERROR_CODE=%ERROR_CODE%
if "%MAVEN_TERMINATE_CMD%"=="on" exit %ERROR_CODE%
cmd /C exit /B %ERROR_CODE%
