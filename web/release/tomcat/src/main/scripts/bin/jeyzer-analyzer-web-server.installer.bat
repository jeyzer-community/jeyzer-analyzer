@echo off

echo -------------------------------------
echo Jeyzer Analyzer Web
echo -------------------------------------

TITLE=Jeyzer Analyzer Web Server

set JAVA_HOME=${jeyzer.installer.java.home}

set CATALINA_HOME=${tomcat.home.dir.windows}

call ${tomcat.home.dir.windows}\bin\catalina.bat run