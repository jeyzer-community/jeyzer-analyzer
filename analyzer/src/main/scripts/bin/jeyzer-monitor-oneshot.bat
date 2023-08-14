@echo off

rem Jeyzer Monitor Oneshot script for Nagios
rem see README.txt for instructions 

rem Set the working directory
cd /d %~dp0

rem ============================
rem CONFIGURATION - BEGIN
rem ============================

rem The master profile to load
if not "%JEYZER_TARGET_PROFILE%" == "" goto gotTargetProfile
set JEYZER_TARGET_PROFILE=demo-features-mx
:gotTargetProfile

rem The recording period
if not "%JEYZER_RECORD_PERIOD%" == "" goto gotRecordPeriod
set JEYZER_RECORD_PERIOD=30s
:gotRecordPeriod

rem The application node name
if not "%JEYZER_TARGET_NAME%" == "" goto gotTargetName
set JEYZER_TARGET_NAME=demo_features_mx
:gotTargetName

rem The issuer
if not "%JEYZER_TARGET_ISSUER%" == "" goto gotTargetIssuer
set JEYZER_TARGET_ISSUER=Jeyzer Monitor for Nagios
:gotTargetIssuer

rem The monitor analyzer stickers
rem List of comma separated stickers. Standard ones : code_quality, performance, security, environment, analysis
if not "%JEYZER_MONITOR_ANALYZER_STICKERS%" == "" goto gotMonitorAnalyzerStickers
set JEYZER_MONITOR_ANALYZER_STICKERS=code_quality, performance, security, environment, analysis
:gotMonitorAnalyzerStickers

rem Repository manager (Nexus, Tomcat..) for obfuscation mapping files or Jeyzer profiles
set JEYZER_REPOSITORY_MANAGER_URL=http://localhost:8080

rem CONFIGURATION - PUBLISHERS
rem ========================================

rem Activate alert email sending
if not "%JEYZER_MONITOR_ALERT_EMAIL_ENABLED%" == "" goto gotMonitorAlertEmailEnabled
set JEYZER_MONITOR_ALERT_EMAIL_ENABLED=false
:gotMonitorAlertEmailEnabled

rem Alert email recipients. Semicolon separated list of emails
if not "%JEYZER_MONITOR_ALERT_EMAIL_RECIPIENTS%" == "" goto gotMonitorAlertEmailRecipients
set JEYZER_MONITOR_ALERT_EMAIL_RECIPIENTS=recipients@domain.com
:gotMonitorAlertEmailRecipients

rem Generate JZR report
if not "%JEYZER_JZR_REPORT_ENABLED%" == "" goto gotAnalyzerReportEnabled
set JEYZER_JZR_REPORT_ENABLED=false
:gotAnalyzerReportEnabled

rem Deploy on the web the monitoring event docs
if not "%JEYZER_MONITOR_WEB_ENABLED%" == "" goto gotMonitorWebEnabled
set JEYZER_MONITOR_WEB_ENABLED=true
:gotMonitorWebEnabled

rem Emit sounds upon events
if not "%JEYZER_MONITOR_SOUND_ENABLED%" == "" goto gotMonitorSoundEnabled
set JEYZER_MONITOR_SOUND_ENABLED=false
:gotMonitorSoundEnabled

rem Web server host, port and deploy directory
set JEYZER_MONITOR_WEB_HOST=192.168.111.1
set JEYZER_MONITOR_WEB_PORT=8080



rem ============================
rem CONFIGURATION - END
rem ============================

rem INTERNALS - BEGIN

rem The monitor scanning period
rem Period set to -1 for oneshot monitoring
set JEYZER_MONITOR_SCAN_PERIOD=-1

rem Graph viewer display
set JEYZER_ANALYZER_GRAPH_VIEWER_ENABLED=false

rem Replay
set JEYZER_ANALYZER_REPLAY_ENABLED=false

rem JIRA
set set JEYZER_MONITOR_JIRA_ENABLED=false

rem Not used : prevent from configuration warning
set JEYZER_DYNAMIC_GRAPH_MODE=action_single

rem If deobfuscation is active, keep deobfuscated files  
set JEYZER_ANALYZER_KEEP_DEOBFUSCATED_FILES=false

rem The Jeyzer Analyzer home (parent directory)
set "CURRENT_DIR=%cd%"
cd ..
set "JEYZER_ANALYZER_HOME=%cd%"
cd "%CURRENT_DIR%"

rem Ensure Jeyzer paths get set
if exist "%JEYZER_ANALYZER_HOME%\bin\set-jeyzer-paths.bat" goto okSetJeyzerPaths
echo Cannot find "%JEYZER_ANALYZER_HOME%\bin\set-jeyzer-paths.bat"
echo This file is needed to run this program
goto end
:okSetJeyzerPaths
call "%JEYZER_ANALYZER_HOME%\bin\set-jeyzer-paths.bat"
if errorlevel 1 goto end

rem In monitor mode, the JEYZER_RECORD_DIRECTORY is always scanned : JEYZER_RECORD_FILE must be disabled
set JEYZER_RECORD_FILE=

rem Ensure Jeyzer Monitor paths get set
if exist "%JEYZER_ANALYZER_HOME%\bin\set-jeyzer-monitor-paths.bat" goto okSetMonitorPaths
echo Cannot find "%JEYZER_ANALYZER_HOME%\bin\set-jeyzer-monitor-paths.bat"
echo This file is needed to run this program
goto end
:okSetMonitorPaths
call "%JEYZER_ANALYZER_HOME%\bin\set-jeyzer-monitor-paths.bat"
if errorlevel 1 goto end

rem Ensure JAVA_HOME is set
if exist "%JEYZER_ANALYZER_HOME%\bin\check-java.bat" goto okCheckJava
echo Cannot find "%JEYZER_ANALYZER_HOME%\bin\check-java.bat"
echo This file is needed to run this program
goto end
:okCheckJava
call "%JEYZER_ANALYZER_HOME%\bin\check-java.bat"
if errorlevel 1 goto end

rem 3rd party libraries
set CLASSPATH=%JEYZER_ANALYZER_HOME%\lib\gson-${com.google.code.gson.version}.jar;%JEYZER_ANALYZER_HOME%\lib\guava-${com.google.guava.guava.version}.jar;%JEYZER_ANALYZER_HOME%\lib\poi-${apache.poi.version}.jar;%JEYZER_ANALYZER_HOME%\lib\poi-ooxml-${org.apache.poi.poi-ooxml.version}.jar;%JEYZER_ANALYZER_HOME%\lib\poi-ooxml-schemas-${apache.poi-ooxml-schemas.version}.jar;%JEYZER_ANALYZER_HOME%\lib\ooxml-schemas-${org.apache.poi.ooxml-schemas.version}.jar;%JEYZER_ANALYZER_HOME%\lib\xmlbeans-${apache.poi.xmlbeans.version}.jar;%JEYZER_ANALYZER_HOME%\lib\jmusic-${com.explodingart.jmusic.version}.jar

rem 3rd party libraries for POI
set CLASSPATH=%CLASSPATH%;%JEYZER_ANALYZER_HOME%\lib\commons-collections4-${org.apache.commons.commons-collections4.version}.jar;%JEYZER_ANALYZER_HOME%\lib\commons-codec-${commons-codec.commons-codec.version}.jar;%JEYZER_ANALYZER_HOME%\lib\commons-math3-${org.apache.commons.commons-math3.version}.jar

rem 3rd party libraries for mail
set CLASSPATH=%CLASSPATH%;%JEYZER_ANALYZER_HOME%\lib\javax.mail-${com.sun.mail.javax.mail.version}.jar;%JEYZER_ANALYZER_HOME%\lib\javax.activation-${com.sun.activation.javax.activation.version}.jar

rem 3rd party graphstream libraries
set CLASSPATH=%CLASSPATH%;%JEYZER_ANALYZER_HOME%\lib\gs-algo-${org.graphstream.gs-algo.version}.jar;%JEYZER_ANALYZER_HOME%\lib\gs-core-alt-${org.graphstream.gs-core-alt.version}.jar;%JEYZER_ANALYZER_HOME%\lib\gs-ui-${org.graphstream.gs-ui.version}.jar;%JEYZER_ANALYZER_HOME%\lib\jfreechart-${jfreechart.version}.jar;%JEYZER_ANALYZER_HOME%\lib\pherd-${org.graphstream.pherd.version}.jar;%JEYZER_ANALYZER_HOME%\lib\scala-library-${org.scala-lang.scala-library.version}.jar;%JEYZER_ANALYZER_HOME%\lib\mbox2-${org.graphstream.mbox2.version}.jar

rem compression library
set CLASSPATH=%CLASSPATH%;%JEYZER_ANALYZER_HOME%\lib\commons-compress-${org.apache.commons.commons-compress.version}.jar

rem 3rd party deobfuscation libraries
set CLASSPATH=%CLASSPATH%;%JEYZER_ANALYZER_HOME%\lib\retrace-alt-${com.github.artyomcool.retrace.retrace-alt.version}.jar

rem 3rd party velocity libraries
set CLASSPATH=%CLASSPATH%;%JEYZER_ANALYZER_HOME%\lib\velocity-${org.apache.velocity.velocity.version}.jar;%JEYZER_ANALYZER_HOME%\lib\commons-lang-${commons-lang.commons-lang.version}.jar;%JEYZER_ANALYZER_HOME%\lib\commons-collections-${commons-collections.commons-collections.version}.jar

rem logging libraries
set CLASSPATH=%CLASSPATH%;%JEYZER_ANALYZER_HOME%\lib\slf4j-api-${slf4j-api.version}.jar;%JEYZER_ANALYZER_HOME%\lib\logback-core-${logback-core.version}.jar;%JEYZER_ANALYZER_HOME%\lib\logback-classic-${ch.qos.logback.logback-classic.version}.jar

rem Jeyzer-analyzer library
set CLASSPATH=%CLASSPATH%;%JEYZER_ANALYZER_HOME%\lib\jeyzer-analyzer.jar

rem profile and logback configurations
set PARAMS=-Djeyzer.analysis.config=%JEYZER_TARGET_PROFILES_DIR%\%JEYZER_TARGET_PROFILE%\%JEYZER_TARGET_PROFILE%_analysis.xml -Djeyzer.monitor.config=%JEYZER_TARGET_PROFILES_DIR%\%JEYZER_TARGET_PROFILE%\%JEYZER_TARGET_PROFILE%_monitor.xml -Dlogback.configurationFile=%JEYZER_ANALYZER_CONFIG_DIR%\log\nagios-logback.xml

rem java options
set JAVA_OPTS=-Xms128m -Xmx1024m

rem JMX options
rem set JMX_OPTS=-Dcom.sun.management.jmxremote.port=5001 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false

rem debug options
rem set DEBUG_OPTS=-Xdebug -Xrunjdwp:transport=dt_socket,address=5000,server=y

rem Safe if already defined
set ERRORLEVEL=

rem INTERNALS - END

rem echo Starting Jeyzer Monitor for Nagios v${pom.version}...
"%JAVA_HOME%\bin\java.exe" %JAVA_OPTS% %DEBUG_OPTS% %JMX_OPTS% -cp %CLASSPATH% %PARAMS% org.jeyzer.monitor.JeyzerNagiosActiveMonitor

rem Exit with monitoring error code
exit %ERRORLEVEL%

:end
rem Setup error
exit 3