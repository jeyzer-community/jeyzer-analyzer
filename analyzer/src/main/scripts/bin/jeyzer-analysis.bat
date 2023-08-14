@echo off

rem Jeyzer Analysis startup script
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

rem The application node name
if not "%JEYZER_TARGET_NAME%" == "" goto gotTargetName
set JEYZER_TARGET_NAME=demo_features_mx
:gotTargetName

rem The issue description
if not "%JEYZER_TARGET_DESCRIPTION%" == "" goto gotTargetDescription
set JEYZER_TARGET_DESCRIPTION=Not available
:gotTargetDescription

rem The issuer
if not "%JEYZER_TARGET_ISSUER%" == "" goto gotTargetIssuer
set JEYZER_TARGET_ISSUER=Not available
:gotTargetIssuer

rem The monitor analyzer stickers
rem List of comma separated stickers. Standard ones : code_quality, performance, security, environment, analysis
if not "%JEYZER_MONITOR_ANALYZER_STICKERS%" == "" goto gotMonitorAnalyzerStickers
set JEYZER_MONITOR_ANALYZER_STICKERS=code_quality, performance, security, environment, analysis
:gotMonitorAnalyzerStickers

rem Activate report email sending
if not "%JEYZER_ANALYZER_TEAM_EMAIL_ENABLED%" == "" goto gotAnalyzerTeamEmailEnabled
set JEYZER_ANALYZER_TEAM_EMAIL_ENABLED=false
:gotAnalyzerTeamEmailEnabled

rem Discovery parameters (up to 5 params can be defined)
rem set JEYZER_ANALYZER_DISCOVERY_FUNCTIONS_1=keyword1,keyword2

rem Discovery report parameters (up to 5 params can be defined)
rem set JEYZER_ANALYZER_DISCOVERY_FUNCTIONS_COLOR_1=RGB-172-186-230

rem Repository manager (Nexus, Tomcat..) for obfuscation mapping files
set JEYZER_REPOSITORY_MANAGER_URL=http://localhost:8080

rem ============================
rem CONFIGURATION - END
rem ============================

rem INTERNALS - BEGIN

set JEYZER_JZR_REPORT_ENABLED=true
set JEYZER_ANALYZER_REPLAY_ENABLED=false
set JEYZER_ANALYZER_GRAPH_VIEWER_ENABLED=%JEYZER_ANALYZER_REPLAY_ENABLED%

rem If deobfuscation is active, keep deobfuscated files  
set JEYZER_ANALYZER_KEEP_DEOBFUSCATED_FILES=true

rem Graph mode : action_single, action_merged
set JEYZER_DYNAMIC_GRAPH_MODE=action_merged

TITLE Jeyzer Analyzer

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

rem Analysis profile and logback configuration
set PARAMS=-Djeyzer.analysis.config=%JEYZER_TARGET_PROFILES_DIR%\%JEYZER_TARGET_PROFILE%\%JEYZER_TARGET_PROFILE%_analysis.xml -Dlogback.configurationFile=%JEYZER_ANALYZER_CONFIG_DIR%\log\analyzer-logback.xml

rem java options
set JAVA_OPTS=-Xms128m -Xmx1024m

rem JMX options
rem set JMX_OPTS=-Dcom.sun.management.jmxremote.port=5001 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false

rem debug options
rem set DEBUG_OPTS=-Xdebug -Xrunjdwp:transport=dt_socket,address=5000,server=y

rem INTERNALS - END

echo Starting Jeyzer Analyzer v2.1...
"%JAVA_HOME%\bin\java.exe" %JAVA_OPTS% %DEBUG_OPTS% %JMX_OPTS% -cp %CLASSPATH% %PARAMS% org.jeyzer.analyzer.JeyzerAnalyzer
goto end

:exit
exit /b 1

:end
exit /b 0