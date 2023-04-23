#!/bin/sh

echo -------------------------------------
echo Jeyzer Analyzer Web
echo -------------------------------------

JAVA_HOME="%{jeyzer.installer.java.home}"
export JAVA_HOME

CATALINA_HOME="%{tomcat.home.dir.unix}"
export CATALINA_HOME

%{tomcat.home.dir.unix}/bin/catalina.sh run