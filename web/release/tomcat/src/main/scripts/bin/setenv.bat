
rem =======================================================
rem Jeyzer Web Server variables
rem
rem Jeyzer Web uses JEYZER_WEB_ variable prefix 
rem Jeyzer Analyzer uses JEYZER_ANALYZER variable prefix 
rem =======================================================


rem Jeyzer analysis output directory
set JEYZER_WEB_ANALYZER_WORK_DIR=C:/Dev/test/jeyzer/work/web/analysis

rem Factory directories
set JEYZER_ANALYZER_CONFIG_DIR=C:/Dev/test/jeyzer/analyzer/config

rem Profile directories
set JEYZER_EXTERNAL_MASTER_PROFILES_DIR=C:/Dev/test/jeyzer/profiles/external/master
set JEYZER_EXTERNAL_SHARED_PROFILES_DIR=C:/Dev/test/jeyzer/profiles/external/shared

set JEYZER_BASE_MASTER_PROFILES_DIR=C:/Dev/test/jeyzer/profiles/base/master
set JEYZER_BASE_SHARED_PROFILES_DIR=C:/Dev/test/jeyzer/profiles/base/shared
set JEYZER_BASE_HELPER_PROFILES_DIR=C:/Dev/test/jeyzer/profiles/base/helper

set JEYZER_DEMO_MASTER_PROFILES_DIR=C:/Dev/test/jeyzer/profiles/demo/master
set JEYZER_DEMO_SHARED_PROFILES_DIR=C:/Dev/test/jeyzer/profiles/demo/shared

rem Profile directories
set JEYZER_MASTER_PROFILES_DIR_ROOTS=%JEYZER_EXTERNAL_MASTER_PROFILES_DIR%;%JEYZER_DEMO_MASTER_PROFILES_DIR%;%JEYZER_BASE_MASTER_PROFILES_DIR%;%JEYZER_BASE_HELPER_PROFILES_DIR%

rem The external place where the Jeyzer shared repositories are declared
set JEYZER_EXTERNAL_REPOSITORY_SETUP_DIRECTORY=C:/Dev/test/jeyzer/profiles/shared-repositories

rem Deobfuscation
set JEYZER_DEOBSFUCATION_CONFIG_DIR=C:/Dev/test/jeyzer/deobfuscation
set JEYZER_ANALYZER_KEEP_DEOBFUSCATED_FILES=true
set JEYZER_REPOSITORY_MANAGER_URL=http://localhost:8080

rem Report
set JEYZER_JZR_REPORT_ENABLED=true
set JEYZER_ANALYZER_TEAM_EMAIL_ENABLED=false

rem Monitor
set JEYZER_MONITOR_ANALYZER_STICKERS=code_quality, performance, security, environment, analysis

rem Replay
set JEYZER_ANALYZER_REPLAY_ENABLED=false
set JEYZER_GRAPH_REFRESH_PERIOD=3s
set JEYZER_DYNAMIC_GRAPH_MODE=action_single
set JEYZER_ANALYZER_GRAPH_VIEWER_ENABLED=%JEYZER_ANALYZER_REPLAY_ENABLED%

rem Logging
set JEYZER_WEB_ANALYZER_LOG_DIR=C:/Dev/test/jeyzer/work/log

rem Java core
set JEYZER_WEB_DEBUG=true
set JAVA_OPTS=%JAVA_OPTS% -Xmx2048m -Xms512m -Dlogback.configurationFile=C:\Dev\test\jeyzer\analyzer\config\log\web-analyzer-logback.xml

rem Web parameters
rem  Max file size in Mb
rem set JEYZER_WEB_UPLOAD_RECORDING_MAX_SIZE=10
rem set JEYZER_WEB_UPLOAD_RECORDING_UNCOMPRESSED_MAX_SIZE=150
rem set JEYZER_WEB_UPLOAD_RECORDING_UNCOMPRESSED_MAX_FILES=362
rem set JEYZER_WEB_ANALYZER_THREAD_POOL_SIZE=3
rem set JEYZER_WEB_DISPLAY_UFO_STACK_FILE_LINK=true
rem set JEYZER_WEB_DISPLAY_FUNCTION_DISCOVERY=true
rem set JEYZER_WEB_TEMP_UPLOAD_DIRECTORY=C:\tmp\Jeyzer\web\upload
rem set JEYZER_WEB_TEMP_UPLOAD_RECORDING_MAX_RETENTION_TIME=5m

rem Set the portal variables if required
if not exist "%CATALINA_HOME%\bin\setenv-portal.bat" goto noSetenvPortal
call "%CATALINA_HOME%\bin\setenv-portal.bat"

:noSetenvPortal