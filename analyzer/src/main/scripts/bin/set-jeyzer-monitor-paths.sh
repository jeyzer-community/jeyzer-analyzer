#!/bin/sh

JEYZER_INSTALLER_DEPLOYMENT=%{jeyzer.installer.deployment}
if [ "$JEYZER_INSTALLER_DEPLOYMENT" != "true" ]; then
  # ---------------------------------------------------------------------------------------------------------------
  # Set Jeyzer Monitor paths
  # JEYZER_MONITOR_WEB_DEPLOY_DIR is intended be set externally 
  #    once the JeyzerMonitor gets integrated in any DevOps platform
  # Edit this section if installation was done manually (no installer)
  # ---------------------------------------------------------------------------------------------------------------
  
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
  
  # Activate JIRA connectivity
  if [ -z "$JEYZER_MONITOR_JIRA_ENABLED" ]; then
    JEYZER_MONITOR_JIRA_ENABLED=false
    export JEYZER_MONITOR_JIRA_ENABLED
  fi

  # Activate Zabbix connectivity
  if [ -z "$JEYZER_MONITOR_ZABBIX_ENABLED" ]; then
    JEYZER_MONITOR_ZABBIX_ENABLED=false
    export JEYZER_MONITOR_ZABBIX_ENABLED
  fi

  # Deploy on the web the monitoring event docs
  if [ -z "$JEYZER_MONITOR_WEB_ENABLED" ]; then
    JEYZER_MONITOR_WEB_ENABLED=true
    export JEYZER_MONITOR_WEB_ENABLED
  fi
  
  # The Web server deploy directory for the generated monitoring resources (status and images)
  if [ -z "$JEYZER_MONITOR_WEB_DEPLOY_DIR" ]; then
    JEYZER_MONITOR_WEB_DEPLOY_DIR=/usr/data/apache-tomcat/webapps/jeyzer-monitor
	export JEYZER_MONITOR_WEB_DEPLOY_DIR
  fi
  
  # Emit sounds upon events
  if [ -z "$JEYZER_MONITOR_SOUND_ENABLED" ]; then
    JEYZER_MONITOR_SOUND_ENABLED=false
    export JEYZER_MONITOR_SOUND_ENABLED
  fi
  
else  

  # ---------------------------------------------------------------------------------------------------------------
  # Set Jeyzer Monitor paths
  # JEYZER_MONITOR_WEB_DEPLOY_DIR is intended be set externally 
  #    once the JeyzerMonitor gets integrated in any DevOps platform
  # Feel free to adjust manually or re-run the installer
  # ---------------------------------------------------------------------------------------------------------------
  
  # CONFIGURATION - PUBLISHERS
  # ========================================

  # Activate alert email sending
  if [ -z "$JEYZER_MONITOR_ALERT_EMAIL_ENABLED" ]; then
    JEYZER_MONITOR_ALERT_EMAIL_ENABLED=%{jeyzer.monitor.publish.email}
    export JEYZER_MONITOR_ALERT_EMAIL_ENABLED
  fi
  
  # Alert email recipients. Semicolon separated list of emails
  if [ -z "$JEYZER_MONITOR_ALERT_EMAIL_RECIPIENTS" ]; then
    JEYZER_MONITOR_ALERT_EMAIL_RECIPIENTS=%{jeyzer.monitor.publish.email.recipients}
    export JEYZER_MONITOR_ALERT_EMAIL_RECIPIENTS
  fi

  # Generate JZR report
  if [ -z "$JEYZER_JZR_REPORT_ENABLED" ]; then
    JEYZER_JZR_REPORT_ENABLED=%{jeyzer.monitor.publish.email.report.generation}
    export JEYZER_JZR_REPORT_ENABLED
  fi
  
  # Activate JIRA connectivity
  if [ -z "$JEYZER_MONITOR_JIRA_ENABLED" ]; then
    JEYZER_MONITOR_JIRA_ENABLED=%{jeyzer.monitor.publish.jira}
    export JEYZER_MONITOR_JIRA_ENABLED
  fi

  # Activate Zabbix connectivity
  if [ -z "$JEYZER_MONITOR_ZABBIX_ENABLED" ]; then
    JEYZER_MONITOR_ZABBIX_ENABLED=%{jeyzer.monitor.publish.zabbix}
    export JEYZER_MONITOR_ZABBIX_ENABLED
  fi

  # Deploy on the web the monitoring event docs
  if [ -z "$JEYZER_MONITOR_WEB_ENABLED" ]; then
    JEYZER_MONITOR_WEB_ENABLED=%{jeyzer.monitor.publish.web}
    export JEYZER_MONITOR_WEB_ENABLED
  fi
  
  # The Web server deploy directory for the generated monitoring resources (status and images)
  if [ -z "$JEYZER_MONITOR_WEB_DEPLOY_DIR" ]; then
    JEYZER_MONITOR_WEB_DEPLOY_DIR=%{jeyzer.monitor.publish.web.deploy.dir}
	export JEYZER_MONITOR_WEB_DEPLOY_DIR
  fi
  
  # Emit sounds upon events
  if [ -z "$JEYZER_MONITOR_SOUND_ENABLED" ]; then
    JEYZER_MONITOR_SOUND_ENABLED=%{jeyzer.monitor.publish.sound}
    export JEYZER_MONITOR_SOUND_ENABLED
  fi
  
fi
