#!/bin/sh

# Jeyzer Multi Monitor startup script
# see README.txt for instructions 

# ========================================
# CONFIGURATION - BEGIN
# ========================================

# ========================================
# PROFILES
# ========================================

# The applicative profiles
JEYZER_TARGET_PROFILE_1=app1
export JEYZER_TARGET_PROFILE_1

JEYZER_TARGET_PROFILE_2=app2
export JEYZER_TARGET_PROFILE_2

# Add other master profiles :
# JEYZER_TARGET_PROFILE_3=app3
# export JEYZER_TARGET_PROFILE_3
# JEYZER_TARGET_PROFILE_4=app4
# export JEYZER_TARGET_PROFILE_4

# Repository manager (Nexus, Tomcat..) for obfuscation mapping files or Jeyzer profiles
export JEYZER_REPOSITORY_MANAGER_URL=http://localhost:8080

# ========================================
# WEB PUBLISHER
# ========================================

JEYZER_MONITOR_WEB_ENABLED=true
export JEYZER_MONITOR_WEB_ENABLED

# Web server host, port and deploy directory
JEYZER_MONITOR_WEB_HOST=192.168.111.1
export JEYZER_MONITOR_WEB_HOST
JEYZER_MONITOR_WEB_PORT=8080
export JEYZER_MONITOR_WEB_PORT

# ========================================
# SOUND PUBLISHER
# ========================================

JEYZER_MONITOR_SOUND_ENABLED=false
export JEYZER_MONITOR_SOUND_ENABLED

# ========================================
# CONFIGURATION - END
# ========================================

# INTERNALS - BEGIN

# Jeyzer Analyzer home (parent directory)
# resolve links - $0 may be a softlink
PRG="$0"

while [ -h "$PRG" ]; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done

# Get standard environment variables
PRGDIR=`dirname "$PRG"`

# set JEYZER_ANALYZER_HOME
[ -z "$JEYZER_ANALYZER_HOME" ] && JEYZER_ANALYZER_HOME=`cd "$PRGDIR/.." >/dev/null; pwd`

# Ensure Jeyzer paths are set
if [ -r "$JEYZER_ANALYZER_HOME"/bin/set-jeyzer-paths.sh ]; then
  . "$JEYZER_ANALYZER_HOME"/bin/set-jeyzer-paths.sh
else
  echo "Cannot find $JEYZER_ANALYZER_HOME/bin/set-jeyzer-paths.sh"
  echo "This file is needed to run this program"
  exit 1
fi

# In monitor mode, the JEYZER_RECORD_DIRECTORY is always scanned : JEYZER_RECORD_FILE must be unset
JEYZER_RECORD_FILE=
export JEYZER_RECORD_FILE

# Ensure Jeyzer Monitor paths are set
if [ -r "$JEYZER_ANALYZER_HOME"/bin/set-jeyzer-monitor-paths.sh ]; then
  . "$JEYZER_ANALYZER_HOME"/bin/set-jeyzer-monitor-paths.sh
else
  echo "Cannot find $JEYZER_ANALYZER_HOME/bin/set-jeyzer-monitor-paths.sh"
  echo "This file is needed to run this program"
  exit 1
fi

# Ensure JAVA_HOME is set
if [ -r "$JEYZER_ANALYZER_HOME"/bin/check-java.sh ]; then
  . "$JEYZER_ANALYZER_HOME"/bin/check-java.sh
else
  echo "Cannot find $JEYZER_ANALYZER_HOME/bin/check-java.sh"
  echo "This file is needed to run this program"
  exit 1
fi

# 3rd party libraries
CLASSPATH=$JEYZER_ANALYZER_HOME/lib/gson-${com.google.code.gson.version}.jar:$JEYZER_ANALYZER_HOME/lib/guava-${com.google.guava.guava.version}.jar:$JEYZER_ANALYZER_HOME/lib/poi-${apache.poi.version}.jar:$JEYZER_ANALYZER_HOME/lib/poi-ooxml-${org.apache.poi.poi-ooxml.version}.jar:$JEYZER_ANALYZER_HOME/lib/poi-ooxml-schemas-${apache.poi-ooxml-schemas.version}.jar:$JEYZER_ANALYZER_HOME/lib/ooxml-schemas-${org.apache.poi.ooxml-schemas.version}.jar:$JEYZER_ANALYZER_HOME/lib/xmlbeans-${apache.poi.xmlbeans.version}.jar:$JEYZER_ANALYZER_HOME/lib/jmusic-${com.explodingart.jmusic.version}.jar

# 3rd party libraries for POI
CLASSPATH=$CLASSPATH:$JEYZER_ANALYZER_HOME/lib/commons-collections4-${org.apache.commons.commons-collections4.version}.jar:$JEYZER_ANALYZER_HOME/lib/commons-codec-${commons-codec.commons-codec.version}.jar:$JEYZER_ANALYZER_HOME/lib/commons-math3-${org.apache.commons.commons-math3.version}.jar

# 3rd party libraries for mail
CLASSPATH=$CLASSPATH:$JEYZER_ANALYZER_HOME/lib/javax.mail-${com.sun.mail.javax.mail.version}.jar:$JEYZER_ANALYZER_HOME/lib/javax.activation-${com.sun.activation.javax.activation.version}.jar

# 3rd party graphstream libraries
CLASSPATH=$CLASSPATH:$JEYZER_ANALYZER_HOME/lib/gs-algo-${org.graphstream.gs-algo.version}.jar:$JEYZER_ANALYZER_HOME/lib/gs-core-alt-${org.graphstream.gs-core-alt.version}.jar:$JEYZER_ANALYZER_HOME/lib/gs-ui-${org.graphstream.gs-ui.version}.jar:$JEYZER_ANALYZER_HOME/lib/jfreechart-${jfreechart.version}.jar:$JEYZER_ANALYZER_HOME/lib/pherd-${org.graphstream.pherd.version}.jar:$JEYZER_ANALYZER_HOME/lib/scala-library-${org.scala-lang.scala-library.version}.jar:$JEYZER_ANALYZER_HOME/lib/mbox2-${org.graphstream.mbox2.version}.jar

# compression libraries
CLASSPATH=$CLASSPATH:$JEYZER_ANALYZER_HOME/lib/commons-compress-${org.apache.commons.commons-compress.version}.jar

# 3rd party deobfuscation libraries
CLASSPATH=$CLASSPATH:$JEYZER_ANALYZER_HOME/lib/retrace-alt-${com.github.artyomcool.retrace.retrace-alt.version}.jar

# 3rd party velocity libraries
CLASSPATH=$CLASSPATH:$JEYZER_ANALYZER_HOME/lib/velocity-${org.apache.velocity.velocity.version}.jar:$JEYZER_ANALYZER_HOME/lib/commons-lang-${commons-lang.commons-lang.version}.jar:$JEYZER_ANALYZER_HOME/lib/commons-collections-${commons-collections.commons-collections.version}.jar

# Jira client library - Okkhttp
CLASSPATH=$CLASSPATH:$JEYZER_ANALYZER_HOME/lib/okhttp-${com.squareup.okhttp3.okhttp.version}.jar:$JEYZER_ANALYZER_HOME/lib/okio-${com.squareup.okio.okio.version}.jar

# Jira client library - REST API client
CLASSPATH=$CLASSPATH:$JEYZER_ANALYZER_HOME/lib/jira-client-${com.atlassian.jira.rest.jira-client.version}.jar:$JEYZER_ANALYZER_HOME/lib/mimepull-${org.jvnet.mimepull.mimepull.version}.jar:$JEYZER_ANALYZER_HOME/lib/osgi-resource-locator-${org.glassfish.hk2.osgi-resource-locator.version}.jar:$JEYZER_ANALYZER_HOME/lib/threetenbp-${org.threeten.threetenbp.version}.jar
CLASSPATH=$CLASSPATH:$JEYZER_ANALYZER_HOME/lib/jersey-common-${org.glassfish.jersey.core.jersey-common.version}.jar:$JEYZER_ANALYZER_HOME/lib/jersey-client-${org.glassfish.jersey.core.jersey-client.version}.jar:$JEYZER_ANALYZER_HOME/lib/jersey-media-multipart-${org.glassfish.jersey.media.jersey-media-multipart.version}.jar
CLASSPATH=$CLASSPATH:$JEYZER_ANALYZER_HOME/lib/jakarta.annotation-api-${jakarta.annotation.jakarta.annotation-api.version}.jar:$JEYZER_ANALYZER_HOME/lib/jakarta.ws.rs-api-${jakarta.ws.rs.jakarta.ws.rs-api.version}.jar:$JEYZER_ANALYZER_HOME/lib/jakarta.inject-${org.glassfish.hk2.external.jakarta.inject.version}.jar
CLASSPATH=$CLASSPATH:$JEYZER_ANALYZER_HOME/lib/jersey-media-json-jackson-${org.glassfish.jersey.media.jersey-media-json-jackson.version}.jar:$JEYZER_ANALYZER_HOME/lib/jackson-databind-${com.fasterxml.jackson.core.jackson-databind.version}.jar:$JEYZER_ANALYZER_HOME/lib/jackson-datatype-threetenbp-${com.github.joschi.jackson.jackson-datatype-threetenbp.version}.jar
CLASSPATH=$CLASSPATH:$JEYZER_ANALYZER_HOME/lib/jackson-annotations-${com.fasterxml.jackson.core.jackson-annotations.version}.jar:$JEYZER_ANALYZER_HOME/lib/jackson-core-${com.fasterxml.jackson.core.jackson-core.version}.jar:$JEYZER_ANALYZER_HOME/lib/jersey-entity-filtering-${org.glassfish.jersey.ext.jersey-entity-filtering.version}.jar
CLASSPATH=$CLASSPATH:$JEYZER_ANALYZER_HOME/lib/jersey-hk2-${org.glassfish.jersey.inject.jersey-hk2.version}.jar:$JEYZER_ANALYZER_HOME/lib/curvesapi-${com.github.virtuald.curvesapi.version}.jar:$JEYZER_ANALYZER_HOME/lib/hamcrest-core-${org.hamcrest.hamcrest-core.version}.jar
CLASSPATH=$CLASSPATH:$JEYZER_ANALYZER_HOME/lib/hk2-api-${org.glassfish.hk2.hk2-api.version}.jar:$JEYZER_ANALYZER_HOME/lib/hk2-locator-${org.glassfish.hk2.hk2-locator.version}.jar:$JEYZER_ANALYZER_HOME/lib/hk2-utils-${org.glassfish.hk2.hk2-utils.version}.jar

# logging libraries
CLASSPATH=$CLASSPATH:$JEYZER_ANALYZER_HOME/lib/slf4j-api-${slf4j-api.version}.jar:$JEYZER_ANALYZER_HOME/lib/logback-core-${logback-core.version}.jar:$JEYZER_ANALYZER_HOME/lib/logback-classic-${ch.qos.logback.logback-classic.version}.jar

# Jeyzer-analyzer library
CLASSPATH=$CLASSPATH:$JEYZER_ANALYZER_HOME/lib/jeyzer-analyzer.jar
export CLASSPATH

# Multi-monitor and logback configurations
PARAMS="-Djeyzer.multimonitor.config="$JEYZER_TARGET_PROFILES_DIR"/multi_monitor.xml -Dlogback.configurationFile="$JEYZER_ANALYZER_CONFIG_DIR"/log/multi-monitor-logback.xml"
export PARAMS

#java options
JAVA_OPTS="-Xms512m -Xmx3g"
export JAVA_OPTS

# JMX options
#JMX_OPTS="-Dcom.sun.management.jmxremote.port=5001 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false"
#export JMX_OPTS

# debug options
#DEBUG_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,address=5000,server=y"
#export DEBUG_OPTS

# INTERNALS - END

echo Starting Jeyzer Multi Monitor v${pom.version}...
$JAVA_HOME/bin/java $JAVA_OPTS $DEBUG_OPTS $JMX_OPTS -cp $CLASSPATH $PARAMS org.jeyzer.monitor.JeyzerMultiMonitor