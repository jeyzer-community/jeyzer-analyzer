#!/bin/sh

# Jeyzer Monitor Oneshot script for Nagios
# see README.txt for instructions 

# ========================================
# CONFIGURATION - BEGIN
# ========================================

# The applicative profile. Ex : sample_app
if [ -z "$JEYZER_TARGET_PROFILE" ]; then
  JEYZER_TARGET_PROFILE=demo-features-mx
  export JEYZER_TARGET_PROFILE
fi

# The recording period
if [ -z "$JEYZER_RECORD_PERIOD" ]; then
  JEYZER_RECORD_PERIOD=30s
  export JEYZER_RECORD_PERIOD
fi

# The application node name
if [ -z "$JEYZER_TARGET_NAME" ]; then
  JEYZER_TARGET_NAME=demo_features_mx
  export JEYZER_TARGET_NAME
fi

# The issuer
if [ -z "$JEYZER_TARGET_ISSUER" ]; then
  JEYZER_TARGET_ISSUER="Jeyzer Monitor for Nagios"
  export JEYZER_TARGET_ISSUER
fi

# The monitor analyzer stickers
# List of comma separated stickers. Standard ones : code_quality, performance, security, environment, analysis
if [ -z "$JEYZER_MONITOR_ANALYZER_STICKERS" ]; then
  JEYZER_MONITOR_ANALYZER_STICKERS="code_quality, performance, security, environment, analysis"
  export JEYZER_MONITOR_ANALYZER_STICKERS
fi

# Repository manager (Nexus, Tomcat..) for obfuscation mapping files or Jeyzer profiles
export JEYZER_REPOSITORY_MANAGER_URL=http://localhost:8080

# CONFIGURATION - PUBLISHERS
# ========================================

# Activate alert email sending
if [ -z "$JEYZER_MONITOR_ALERT_EMAIL_ENABLED" ]; then
  JEYZER_MONITOR_ALERT_EMAIL_ENABLED=false
  export JEYZER_MONITOR_ALERT_EMAIL_ENABLED
fi

# Alert email recipients. Semicolon separated list of emails
if [ -z "$JEYZER_MONITOR_ALERT_EMAIL_RECIPIENTS" ]; then
  JEYZER_MONITOR_ALERT_EMAIL_RECIPIENTS=recipients@domain.com
  export JEYZER_MONITOR_ALERT_EMAIL_RECIPIENTS
fi

# Generate JZR report
if [ -z "$JEYZER_JZR_REPORT_ENABLED" ]; then
  JEYZER_JZR_REPORT_ENABLED=false
  export JEYZER_JZR_REPORT_ENABLED
fi

# Deploy on the web the monitoring event docs
if [ -z "$JEYZER_MONITOR_WEB_ENABLED" ]; then
  JEYZER_MONITOR_WEB_ENABLED=true
  export JEYZER_MONITOR_WEB_ENABLED
fi

# Emit sounds upon events
if [ -z "$JEYZER_MONITOR_SOUND_ENABLED" ]; then
  JEYZER_MONITOR_SOUND_ENABLED=false
  export JEYZER_MONITOR_SOUND_ENABLED
fi

# Web server host, port and deploy directory
JEYZER_MONITOR_WEB_HOST=192.168.111.1
export JEYZER_MONITOR_WEB_HOST
JEYZER_MONITOR_WEB_PORT=8080
export JEYZER_MONITOR_WEB_PORT


# ========================================
# CONFIGURATION - END
# ========================================

# INTERNALS - BEGIN

# The monitor scanning period
# Period set to -1 for oneshot monitoring
JEYZER_MONITOR_SCAN_PERIOD=-1
export JEYZER_MONITOR_SCAN_PERIOD

# Graph viewer display
JEYZER_ANALYZER_GRAPH_VIEWER_ENABLED=false
export JEYZER_ANALYZER_GRAPH_VIEWER_ENABLED

# Replay
JEYZER_ANALYZER_REPLAY_ENABLED=false
export JEYZER_ANALYZER_REPLAY_ENABLED

# Jira
JEYZER_MONITOR_JIRA_ENABLED=false
export JEYZER_MONITOR_JIRA_ENABLED

# Not used : prevent from configuration warning
JEYZER_DYNAMIC_GRAPH_MODE=action_single
export JEYZER_DYNAMIC_GRAPH_MODE

# If deobfuscation is active, keep deobfuscated files  
JEYZER_ANALYZER_KEEP_DEOBFUSCATED_FILES=false
export JEYZER_ANALYZER_KEEP_DEOBFUSCATED_FILES


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
  exit 3
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
  exit 3
fi

# Ensure JAVA_HOME is set
if [ -r "$JEYZER_ANALYZER_HOME"/bin/check-java.sh ]; then
  . "$JEYZER_ANALYZER_HOME"/bin/check-java.sh
else
  echo "Cannot find $JEYZER_ANALYZER_HOME/bin/check-java.sh"
  echo "This file is needed to run this program"
  exit 3
fi

# 3rd party libraries
CLASSPATH=$JEYZER_ANALYZER_HOME/lib/guava-${com.google.guava.guava.version}.jar:$JEYZER_ANALYZER_HOME/lib/poi-${apache.poi.version}.jar:$JEYZER_ANALYZER_HOME/lib/poi-ooxml-${org.apache.poi.poi-ooxml.version}.jar:$JEYZER_ANALYZER_HOME/lib/poi-ooxml-schemas-${apache.poi-ooxml-schemas.version}.jar:$JEYZER_ANALYZER_HOME/lib/ooxml-schemas-${org.apache.poi.ooxml-schemas.version}.jar:$JEYZER_ANALYZER_HOME/lib/xmlbeans-${apache.poi.xmlbeans.version}.jar:$JEYZER_ANALYZER_HOME/lib/jmusic-${com.explodingart.jmusic.version}.jar

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

# logging libraries
CLASSPATH=$CLASSPATH:$JEYZER_ANALYZER_HOME/lib/slf4j-api-${slf4j-api.version}.jar:$JEYZER_ANALYZER_HOME/lib/logback-core-${logback-core.version}.jar:$JEYZER_ANALYZER_HOME/lib/logback-classic-${ch.qos.logback.logback-classic.version}.jar

# Jeyzer-analyzer library
CLASSPATH=$CLASSPATH:$JEYZER_ANALYZER_HOME/lib/jeyzer-analyzer.jar
export CLASSPATH

# profile and logback configurations
PARAMS="-Djeyzer.analysis.config="$JEYZER_TARGET_PROFILES_DIR"/"$JEYZER_TARGET_PROFILE"/"$JEYZER_TARGET_PROFILE"_analysis.xml -Djeyzer.monitor.config="$JEYZER_TARGET_PROFILES_DIR"/"$JEYZER_TARGET_PROFILE"/"$JEYZER_TARGET_PROFILE"_monitor.xml -Dlogback.configurationFile="$JEYZER_ANALYZER_CONFIG_DIR"/log/nagios-logback.xml"
export PARAMS

#java options
JAVA_OPTS="-Xms128m -Xmx1024m"
export JAVA_OPTS

# JMX options
#JMX_OPTS="-Dcom.sun.management.jmxremote.port=5001 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false"
#export JMX_OPTS

# debug options
#DEBUG_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,address=5000,server=y"
#export DEBUG_OPTS

# INTERNALS - END

# echo Starting Jeyzer Monitor for Nagios v${pom.version}...
$JAVA_HOME/bin/java $JAVA_OPTS $DEBUG_OPTS $JMX_OPTS -cp $CLASSPATH $PARAMS org.jeyzer.monitor.JeyzerNagiosActiveMonitor

# Exit status
echo $?