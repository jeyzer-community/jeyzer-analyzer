@echo off

rem Jeyzer Multi Monitor startup script
rem see README.txt for instructions 

rem ============================
rem CONFIGURATION - BEGIN
rem ============================

rem ========================================
rem PROFILES
rem ========================================

rem The master profile to loads
set JEYZER_TARGET_PROFILE_1=app1

set JEYZER_TARGET_PROFILE_2=app2

rem Add other master profiles :
rem set JEYZER_TARGET_PROFILE_3=app3
rem set JEYZER_TARGET_PROFILE_4=app4


rem Repository manager (Nexus, Tomcat..) for obfuscation mapping files or Jeyzer profiles
set JEYZER_REPOSITORY_MANAGER_URL=http://localhost:8080

rem ========================================
rem WEB PUBLISHER
rem ========================================

rem Deploy on the web the monitoring event docs
set JEYZER_MONITOR_WEB_ENABLED=true

rem Web server host, port and deploy directory
set JEYZER_MONITOR_WEB_HOST=192.168.111.1
set JEYZER_MONITOR_WEB_PORT=8080

rem ========================================
rem SOUND PUBLISHER
rem ========================================

rem Sound activation
set JEYZER_MONITOR_SOUND_ENABLED=false

rem ============================
rem CONFIGURATION - END
rem ============================

rem INTERNALS - BEGIN

TITLE Jeyzer Multi Monitor

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

rem Ensure Jeyzer Monitor paths get set
if exist "%JEYZER_ANALYZER_HOME%\bin\set-jeyzer-monitor-paths.bat" goto okSetMonitorPaths
echo Cannot find "%JEYZER_ANALYZER_HOME%\bin\set-jeyzer-monitor-paths.bat"
echo This file is needed to run this program
goto exit
:okSetMonitorPaths
call "%JEYZER_ANALYZER_HOME%\bin\set-jeyzer-monitor-paths.bat"
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

rem logging libraries
set CLASSPATH=%CLASSPATH%;%JEYZER_ANALYZER_HOME%\lib\slf4j-api-${slf4j-api.version}.jar;%JEYZER_ANALYZER_HOME%\lib\logback-core-${logback-core.version}.jar;%JEYZER_ANALYZER_HOME%\lib\logback-classic-${ch.qos.logback.logback-classic.version}.jar

rem Jeyzer-analyzer library
set CLASSPATH=%CLASSPATH%;%JEYZER_ANALYZER_HOME%\lib\jeyzer-analyzer.jar

rem Multi-monitor and logback configurations
set PARAMS=-Djeyzer.multimonitor.config=%JEYZER_TARGET_PROFILES_DIR%\multi_monitor.xml -Dlogback.configurationFile=%JEYZER_ANALYZER_CONFIG_DIR%\log\multi-monitor-logback.xml

rem java options
set JAVA_OPTS=-Xms512m -Xmx3g

rem JMX options
rem set JMX_OPTS=-Dcom.sun.management.jmxremote.port=5001 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false

rem debug options
rem set DEBUG_OPTS=-Xdebug -Xrunjdwp:transport=dt_socket,address=5000,server=y

rem INTERNALS - END

echo Starting Jeyzer Multi Monitor v${pom.version}...
"%JAVA_HOME%\bin\java.exe" %JAVA_OPTS% %DEBUG_OPTS% %JMX_OPTS% -cp %CLASSPATH% %PARAMS% org.jeyzer.monitor.JeyzerMultiMonitor
goto end

:exit
exit /b 1

:end
exit /b 0