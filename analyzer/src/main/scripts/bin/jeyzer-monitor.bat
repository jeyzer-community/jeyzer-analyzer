@echo off

rem Jeyzer Monitor startup script
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

rem The recording period
if not "%JEYZER_RECORD_PERIOD%" == "" goto gotRecordPeriod
set JEYZER_RECORD_PERIOD=30s
:gotRecordPeriod

rem The monitor scanning period
rem Make sure it is lower than the JEYZER_RECORD_ARCHIVE_TIME_OFFSET
if not "%JEYZER_MONITOR_SCAN_PERIOD%" == "" goto gotMonitorScanPeriod
set JEYZER_MONITOR_SCAN_PERIOD=5m
:gotMonitorScanPeriod

rem The application node name
if not "%JEYZER_TARGET_NAME%" == "" goto gotTargetName
set JEYZER_TARGET_NAME=demo_features_mx
:gotTargetName

rem The issuer
if not "%JEYZER_TARGET_ISSUER%" == "" goto gotTargetIssuer
set JEYZER_TARGET_ISSUER=Not available
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

rem Web server host, port and deploy directory
set JEYZER_MONITOR_WEB_HOST=192.168.111.1
set JEYZER_MONITOR_WEB_PORT=8080


rem CONFIGURATION - GRAPH
rem  ========================================

rem Graph viewer display
set JEYZER_ANALYZER_GRAPH_VIEWER_ENABLED=false

rem Graph snapshot generation
if not "%JEYZER_MONITOR_GRAPH_PICTURE_ENABLED%" == "" goto gotMonitorGraphPictureEnabled
set JEYZER_MONITOR_GRAPH_PICTURE_ENABLED=true
:gotMonitorGraphPictureEnabled


rem ============================
rem CONFIGURATION - END
rem ============================

rem INTERNALS - BEGIN

set JEYZER_TARGET_DESCRIPTION=Monitoring session
set JEYZER_ANALYZER_TEAM_EMAIL_ENABLED=false
set JEYZER_ANALYZER_REPLAY_ENABLED=false

rem Not used : prevent from configuration warning
set JEYZER_DYNAMIC_GRAPH_MODE=action_single

rem If deobfuscation is active, keep deobfuscated files  
set JEYZER_ANALYZER_KEEP_DEOBFUSCATED_FILES=false

TITLE Jeyzer Monitor

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
call "%JEYZER_ANALYZER_HOME%\bin\set-jeyzer-paths.bat" %1
if errorlevel 1 goto exit

rem In monitor mode, the JEYZER_RECORD_DIRECTORY is always scanned : JEYZER_RECORD_FILE must be disabled
set JEYZER_RECORD_FILE=

rem Ensure Jeyzer Monitor paths get set
if exist "%JEYZER_ANALYZER_HOME%\bin\set-jeyzer-monitor-paths.bat" goto okSetMonitorPaths
echo Cannot find "%JEYZER_ANALYZER_HOME%\bin\set-jeyzer-monitor-paths.bat"
echo This file is needed to run this program
goto exit
:okSetMonitorPaths
call "%JEYZER_ANALYZER_HOME%\bin\set-jeyzer-monitor-paths.bat"
if errorlevel 1 goto exit

rem Ensure JAVA_HOME is set
if exist "%JEYZER_ANALYZER_HOME%\bin\check-java.bat" goto okCheckJava
echo Cannot find "%JEYZER_ANALYZER_HOME%\bin\check-java.bat"
echo This file is needed to run this program
goto exit
:okCheckJava
call "%JEYZER_ANALYZER_HOME%\bin\check-java.bat"
if errorlevel 1 goto exit

rem 3rd party libraries
set CLASSPATH=%JEYZER_ANALYZER_HOME%\lib\guava-${com.google.guava.guava.version}.jar;%JEYZER_ANALYZER_HOME%\lib\poi-${apache.poi.version}.jar;%JEYZER_ANALYZER_HOME%\lib\poi-ooxml-${org.apache.poi.poi-ooxml.version}.jar;%JEYZER_ANALYZER_HOME%\lib\poi-ooxml-schemas-${apache.poi-ooxml-schemas.version}.jar;%JEYZER_ANALYZER_HOME%\lib\ooxml-schemas-${org.apache.poi.ooxml-schemas.version}.jar;%JEYZER_ANALYZER_HOME%\lib\xmlbeans-${apache.poi.xmlbeans.version}.jar;%JEYZER_ANALYZER_HOME%\lib\jmusic-${com.explodingart.jmusic.version}.jar

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

rem compression library
set CLASSPATH=%CLASSPATH%;%JEYZER_ANALYZER_HOME%\lib\commons-compress-${org.apache.commons.commons-compress.version}.jar

rem Jira client library - Okkhttp
set CLASSPATH=%CLASSPATH%;%JEYZER_ANALYZER_HOME%\lib\okhttp-${com.squareup.okhttp3.okhttp.version}.jar;%JEYZER_ANALYZER_HOME%\lib\okio-${com.squareup.okio.okio.version}.jar

rem Jira client library - REST API client
set CLASSPATH=%CLASSPATH%;%JEYZER_ANALYZER_HOME%\lib\jira-client-${com.atlassian.jira.rest.jira-client.version}.jar;%JEYZER_ANALYZER_HOME%\lib\mimepull-${org.jvnet.mimepull.mimepull.version}.jar;%JEYZER_ANALYZER_HOME%\lib\osgi-resource-locator-${org.glassfish.hk2.osgi-resource-locator.version}.jar;%JEYZER_ANALYZER_HOME%\lib\threetenbp-${org.threeten.threetenbp.version}.jar
set CLASSPATH=%CLASSPATH%;%JEYZER_ANALYZER_HOME%\lib\jersey-common-${org.glassfish.jersey.core.jersey-common.version}.jar;%JEYZER_ANALYZER_HOME%\lib\jersey-client-${org.glassfish.jersey.core.jersey-client.version}.jar;%JEYZER_ANALYZER_HOME%\lib\jersey-media-multipart-${org.glassfish.jersey.media.jersey-media-multipart.version}.jar
set CLASSPATH=%CLASSPATH%;%JEYZER_ANALYZER_HOME%\lib\jakarta.annotation-api-${jakarta.annotation.jakarta.annotation-api.version}.jar;%JEYZER_ANALYZER_HOME%\lib\jakarta.ws.rs-api-${jakarta.ws.rs.jakarta.ws.rs-api.version}.jar;%JEYZER_ANALYZER_HOME%\lib\jakarta.inject-${org.glassfish.hk2.external.jakarta.inject.version}.jar
set CLASSPATH=%CLASSPATH%;%JEYZER_ANALYZER_HOME%\lib\jersey-media-json-jackson-${org.glassfish.jersey.media.jersey-media-json-jackson.version}.jar;%JEYZER_ANALYZER_HOME%\lib\jackson-databind-${com.fasterxml.jackson.core.jackson-databind.version}.jar;%JEYZER_ANALYZER_HOME%\lib\jackson-datatype-threetenbp-${com.github.joschi.jackson.jackson-datatype-threetenbp.version}.jar
set CLASSPATH=%CLASSPATH%;%JEYZER_ANALYZER_HOME%\lib\jackson-annotations-${com.fasterxml.jackson.core.jackson-annotations.version}.jar;%JEYZER_ANALYZER_HOME%\lib\jackson-core-${com.fasterxml.jackson.core.jackson-core.version}.jar;%JEYZER_ANALYZER_HOME%\lib\jersey-entity-filtering-${org.glassfish.jersey.ext.jersey-entity-filtering.version}.jar
set CLASSPATH=%CLASSPATH%;%JEYZER_ANALYZER_HOME%\lib\jersey-hk2-${org.glassfish.jersey.inject.jersey-hk2.version}.jar;%JEYZER_ANALYZER_HOME%\lib\curvesapi-${com.github.virtuald.curvesapi.version}.jar;%JEYZER_ANALYZER_HOME%\lib\hamcrest-core-${org.hamcrest.hamcrest-core.version}.jar
set CLASSPATH=%CLASSPATH%;%JEYZER_ANALYZER_HOME%\lib\hk2-api-${org.glassfish.hk2.hk2-api.version}.jar;%JEYZER_ANALYZER_HOME%\lib\hk2-locator-${org.glassfish.hk2.hk2-locator.version}.jar;%JEYZER_ANALYZER_HOME%\lib\hk2-utils-${org.glassfish.hk2.hk2-utils.version}.jar

rem Profile and logback configurations
set PARAMS=-Djeyzer.analysis.config=%JEYZER_TARGET_PROFILES_DIR%\%JEYZER_TARGET_PROFILE%\%JEYZER_TARGET_PROFILE%_analysis.xml -Djeyzer.monitor.config=%JEYZER_TARGET_PROFILES_DIR%\%JEYZER_TARGET_PROFILE%\%JEYZER_TARGET_PROFILE%_monitor.xml -Dlogback.configurationFile=%JEYZER_ANALYZER_CONFIG_DIR%\log\monitor-logback.xml

rem java options
set JAVA_OPTS=-Xms128m -Xmx1024m

rem JMX options
rem set JMX_OPTS=-Dcom.sun.management.jmxremote.port=5001 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false

rem debug options
rem set DEBUG_OPTS=-Xdebug -Xrunjdwp:transport=dt_socket,address=5000,server=y

rem INTERNALS - END

echo Starting Jeyzer Monitor v${pom.version}...
"%JAVA_HOME%\bin\java.exe" %JAVA_OPTS% %DEBUG_OPTS% %JMX_OPTS% -cp %CLASSPATH% %PARAMS% org.jeyzer.monitor.JeyzerMonitor
goto end

:exit
exit /b 1

:end
exit /b 0