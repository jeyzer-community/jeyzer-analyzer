@echo off

rem Jeyzer Monitor Console startup script
rem see README.txt for instructions 

rem ============================
rem CONFIGURATION - BEGIN
rem ============================

rem Important:
rem Once integrated in any Devops platform, all below variables will be set externally

rem The master profile to load
if not "%JEYZER_TARGET_PROFILE%" == "" goto gotTargetProfile
set JEYZER_TARGET_PROFILE=demo-features-mx
:gotTargetProfile

rem Graph viewer display
set JEYZER_ANALYZER_GRAPH_VIEWER_ENABLED=true

rem Graph mode : action_single, action_merged
if not "%JEYZER_DYNAMIC_GRAPH_MODE%" == "" goto gotDynamicGraphMode
set JEYZER_DYNAMIC_GRAPH_MODE=action_merged
:gotDynamicGraphMode

rem The recording period
if not "%JEYZER_RECORD_PERIOD%" == "" goto gotRecordPeriod
set JEYZER_RECORD_PERIOD=30s
:gotRecordPeriod

rem The application node name
if not "%JEYZER_TARGET_NAME%" == "" goto gotTargetName
set JEYZER_TARGET_NAME=demo_features_mx
:gotTargetName

rem Repository manager (Nexus, Tomcat..) for obfuscation mapping files
set JEYZER_REPOSITORY_MANAGER_URL=http://localhost:8080

rem ============================
rem CONFIGURATION - END
rem ============================

rem INTERNALS - BEGIN

set JEYZER_JZR_REPORT_ENABLED=false
set JEYZER_ANALYZER_REPLAY_ENABLED=false
set JEYZER_MONITOR_ALERT_EMAIL_ENABLED=false
set JEYZER_MONITOR_JIRA_ENABLED=false
set JEYZER_MONITOR_ZABBIX_ENABLED=false
set JEYZER_MONITOR_WEB_ENABLED=false
set JEYZER_MONITOR_SOUND_ENABLED=false

rem If deobfuscation is active, keep deobfuscated files  
set JEYZER_ANALYZER_KEEP_DEOBFUSCATED_FILES=true

TITLE=Jeyzer Monitor Console

rem The Jeyzer Analyzer home (parent directory)
set "CURRENT_DIR=%cd%"
cd ..
set "JEYZER_ANALYZER_HOME=%cd%"
cd "%CURRENT_DIR%"

rem Ensure Jeyzer paths get set
if exist "%JEYZER_ANALYZER_HOME%\bin\set-jeyzer-paths.bat" goto okSetJeyzerPaths
echo Cannot find "%JEYZER_ANALYZER_HOME%\bin\set-jeyzer-paths.bat"
echo This file is needed to run this program
goto exit
:okSetJeyzerPaths
call "%JEYZER_ANALYZER_HOME%\bin\set-jeyzer-paths.bat"
if errorlevel 1 goto exit

rem In monitor mode, the JEYZER_RECORD_DIRECTORY is always scanned : JEYZER_RECORD_FILE must be disabled
set JEYZER_RECORD_FILE=

rem Ensure JAVA_HOME is set
if exist "%JEYZER_ANALYZER_HOME%\bin\check-java.bat" goto okCheckJava
echo Cannot find "%JEYZER_ANALYZER_HOME%\bin\check-java.bat"
echo This file is needed to run this program
goto exit
:okCheckJava
call "%JEYZER_ANALYZER_HOME%\bin\check-java.bat"
if errorlevel 1 goto exit

rem 3rd party libraries
set CLASSPATH=%JEYZER_ANALYZER_HOME%\lib\gson-${com.google.code.gson.version}.jar;%JEYZER_ANALYZER_HOME%\lib\guava-${com.google.guava.guava.version}.jar;%JEYZER_ANALYZER_HOME%\lib\poi-${apache.poi.version}.jar;%JEYZER_ANALYZER_HOME%\lib\poi-ooxml-${org.apache.poi.poi-ooxml.version}.jar;%JEYZER_ANALYZER_HOME%\lib\poi-ooxml-schemas-${apache.poi-ooxml-schemas.version}.jar;%JEYZER_ANALYZER_HOME%\lib\ooxml-schemas-${org.apache.poi.ooxml-schemas.version}.jar;%JEYZER_ANALYZER_HOME%\lib\xmlbeans-${apache.poi.xmlbeans.version}.jar;%JEYZER_ANALYZER_HOME%\lib\jmusic-${com.explodingart.jmusic.version}.jar

rem 3rd party libraries for POI
set CLASSPATH=%CLASSPATH%;%JEYZER_ANALYZER_HOME%\lib\commons-collections4-${org.apache.commons.commons-collections4.version}.jar;%JEYZER_ANALYZER_HOME%\lib\commons-codec-${commons-codec.commons-codec.version}.jar;%JEYZER_ANALYZER_HOME%\lib\commons-math3-${org.apache.commons.commons-math3.version}.jar

rem 3rd party libraries for mail
set CLASSPATH=%CLASSPATH%;%JEYZER_ANALYZER_HOME%\lib\javax.mail-${com.sun.mail.javax.mail.version}.jar;%JEYZER_ANALYZER_HOME%\lib\javax.activation-${com.sun.activation.javax.activation.version}.jar

rem compression library
set CLASSPATH=%CLASSPATH%;%JEYZER_ANALYZER_HOME%\lib\commons-compress-${org.apache.commons.commons-compress.version}.jar

rem 3rd party deobfuscation libraries
set CLASSPATH=%CLASSPATH%;%JEYZER_ANALYZER_HOME%\lib\retrace-alt-${com.github.artyomcool.retrace.retrace-alt.version}.jar

rem 3rd party graphstream libraries
set CLASSPATH=%CLASSPATH%;%JEYZER_ANALYZER_HOME%\lib\gs-algo-${org.graphstream.gs-algo.version}.jar;%JEYZER_ANALYZER_HOME%\lib\gs-core-alt-${org.graphstream.gs-core-alt.version}.jar;%JEYZER_ANALYZER_HOME%\lib\gs-ui-${org.graphstream.gs-ui.version}.jar;%JEYZER_ANALYZER_HOME%\lib\jfreechart-${jfreechart.version}.jar;%JEYZER_ANALYZER_HOME%\lib\pherd-${org.graphstream.pherd.version}.jar;%JEYZER_ANALYZER_HOME%\lib\scala-library-${org.scala-lang.scala-library.version}.jar;%JEYZER_ANALYZER_HOME%\lib\mbox2-${org.graphstream.mbox2.version}.jar

rem 3rd party velocity libraries
set CLASSPATH=%CLASSPATH%;%JEYZER_ANALYZER_HOME%\lib\velocity-${org.apache.velocity.velocity.version}.jar;%JEYZER_ANALYZER_HOME%\lib\commons-lang-${commons-lang.commons-lang.version}.jar;%JEYZER_ANALYZER_HOME%\lib\commons-collections-${commons-collections.commons-collections.version}.jar

rem logging libraries
set CLASSPATH=%CLASSPATH%;%JEYZER_ANALYZER_HOME%\lib\slf4j-api-${slf4j-api.version}.jar;%JEYZER_ANALYZER_HOME%\lib\logback-core-${logback-core.version}.jar;%JEYZER_ANALYZER_HOME%\lib\logback-classic-${ch.qos.logback.logback-classic.version}.jar

rem Jeyzer-analyzer library
set CLASSPATH=%CLASSPATH%;%JEYZER_ANALYZER_HOME%\lib\jeyzer-analyzer.jar

rem profile, console and logback configurations
set PARAMS=-Djeyzer.analysis.config=%JEYZER_TARGET_PROFILES_DIR%\%JEYZER_TARGET_PROFILE%\%JEYZER_TARGET_PROFILE%_analysis.xml -Djeyzer.monitor.console.config=%JEYZER_TARGET_PROFILES_DIR%\%JEYZER_TARGET_PROFILE%\%JEYZER_TARGET_PROFILE%_monitor_console.xml -Dlogback.configurationFile=%JEYZER_ANALYZER_CONFIG_DIR%\log\monitor-console-logback.xml

rem java options
set JAVA_OPTS=-Xms128m -Xmx1024m

rem JMX options
rem set JMX_OPTS=-Dcom.sun.management.jmxremote.port=5001 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false

rem debug options
rem set DEBUG_OPTS=-Xdebug -Xrunjdwp:transport=dt_socket,address=5000,server=y

rem INTERNALS - END

echo Starting Jeyzer Monitor Console v${pom.version}...
"%JAVA_HOME%\bin\java.exe" %JAVA_OPTS% %DEBUG_OPTS% %JMX_OPTS% -cp %CLASSPATH% %PARAMS% org.jeyzer.monitor.JeyzerMonitorConsole
goto end

:exit
exit /b 1

:end
exit /b 0