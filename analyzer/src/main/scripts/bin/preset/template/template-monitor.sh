#!/bin/sh

# Jeyzer Monitor startup script
# see README.txt for instructions 

# ============================
# CONFIGURATION - BEGIN
# ============================

# The target profile
JEYZER_TARGET_PROFILE="%{jeyzer.profile.id}"
export JEYZER_TARGET_PROFILE

# The application node name
JEYZER_TARGET_NAME="%{jeyzer.profile.name} instance"
export JEYZER_TARGET_NAME

# The Jeyzer record directory
JEYZER_RECORD_DIRECTORY=%{jeyzer.recorder.work.dir}/"$JEYZER_TARGET_PROFILE"
export JEYZER_RECORD_DIRECTORY

# The monitor scanning period
export JEYZER_MONITOR_SCAN_PERIOD=5m

# The Jeyzer profile directory
JEYZER_TARGET_PROFILES_DIR=%{jeyzer.analyzer.ext.profiles.dir}/master
export JEYZER_TARGET_PROFILES_DIR


# ============================
# INTERNALS - DO NOT EDIT
# ============================

# The recording period if you know it - optional
# JEYZER_RECORD_PERIOD=30s
# export JEYZER_RECORD_PERIOD

# Jeyzer Analyzer bin home (parent-parent directory)
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

# set JEYZER_ANALYZER_BIN_HOME
[ -z "$JEYZER_ANALYZER_BIN_HOME" ] && JEYZER_ANALYZER_BIN_HOME=`cd "$PRGDIR/../.." >/dev/null; pwd`

cd $JEYZER_ANALYZER_BIN_HOME
$JEYZER_ANALYZER_BIN_HOME/jeyzer-monitor.sh
cd $PRGDIR