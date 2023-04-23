@echo off

rem Jeyzer Analysis startup script
rem see README.txt for instructions 

rem ============================
rem CONFIGURATION - BEGIN
rem ============================

rem The target profile
set JEYZER_TARGET_PROFILE=${jeyzer.profile.id}

rem The application node name
set JEYZER_TARGET_NAME=${jeyzer.profile.name} instance

rem The issue description
set JEYZER_TARGET_DESCRIPTION=Jeyzer analysis of the ${jeyzer.profile.name} application

rem The Jeyzer record directory
set JEYZER_RECORD_DIRECTORY=${jeyzer.recorder.work.dir}\%JEYZER_TARGET_PROFILE%

rem The Jeyzer profile directory
set JEYZER_TARGET_PROFILES_DIR=${jeyzer.analyzer.ext.profiles.dir}\master

rem ============================
rem INTERNALS - DO NOT EDIT
rem ============================

rem The recording period if you know it - optional
rem set JEYZER_RECORD_PERIOD=30s

set "PRESET_DIR=%cd%"
cd ..\..
set "JEYZER_ANALYZER_BIN_HOME=%cd%"
call %JEYZER_ANALYZER_BIN_HOME%\jeyzer-analysis.bat
cd "%PRESET_DIR%"