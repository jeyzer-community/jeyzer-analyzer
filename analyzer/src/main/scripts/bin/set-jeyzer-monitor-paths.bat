@echo off

rem If Jeyzer installation result, paths get set automatically
set JEYZER_INSTALLER_DEPLOYMENT=${jeyzer.installer.deployment}
if "%JEYZER_INSTALLER_DEPLOYMENT%" == "true" goto gotPathsSetByInstaller

rem ---------------------------------------------------------------------------------------------------------------
rem Set Jeyzer Monitor paths
rem JEYZER_MONITOR_WEB_DEPLOY_DIR is intended be set externally 
rem    once the Jeyzer Monitor gets integrated in any DevOps platform
rem Edit this section if installation was done manually (no installer)
rem ---------------------------------------------------------------------------------------------------------------


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

rem Activate JIRA connectivity
if not "%JEYZER_MONITOR_JIRA_ENABLED%" == "" goto gotMonitorJiraEnabled
set JEYZER_MONITOR_JIRA_ENABLED=false
:gotMonitorJiraEnabled

rem Activate Zabbix connectivity
if not "%JEYZER_MONITOR_ZABBIX_ENABLED%" == "" goto gotMonitorZabbixEnabled
set JEYZER_MONITOR_ZABBIX_ENABLED=false
:gotMonitorZabbixEnabled

rem Deploy on the web the monitoring event docs
if not "%JEYZER_MONITOR_WEB_ENABLED%" == "" goto gotMonitorWebEnabled
set JEYZER_MONITOR_WEB_ENABLED=false
:gotMonitorWebEnabled

rem The Web server deploy directory for the generated monitoring resources (status and images)
if not "%JEYZER_MONITOR_WEB_DEPLOY_DIR%" == "" goto gotMonitorWebDeployDir
set JEYZER_MONITOR_WEB_DEPLOY_DIR=c:/apache-tomcat/webapps/jeyzer-monitor
:gotMonitorWebDeployDir

rem Emit sounds upon events
if not "%JEYZER_MONITOR_SOUND_ENABLED%" == "" goto gotMonitorSoundEnabled
set JEYZER_MONITOR_SOUND_ENABLED=false
:gotMonitorSoundEnabled

goto end

rem ---------------------------------------------------------------------------------------------------------------
rem Jeyzer Monitor paths automatically set  
rem JEYZER_MONITOR_WEB_DEPLOY_DIR is intended be set externally 
rem    once the Jeyzer Monitor gets integrated in any DevOps platform
rem Feel free to adjust manually or re-run the installer
rem ---------------------------------------------------------------------------------------------------------------

:gotPathsSetByInstaller

rem CONFIGURATION - PUBLISHERS
rem ========================================

rem Activate alert email sending
if not "%JEYZER_MONITOR_ALERT_EMAIL_ENABLED%" == "" goto gotMonitorAlertEmailEnabled
set JEYZER_MONITOR_ALERT_EMAIL_ENABLED=${jeyzer.monitor.publish.email}
:gotMonitorAlertEmailEnabled

rem Alert email recipients. Semicolon separated list of emails
if not "%JEYZER_MONITOR_ALERT_EMAIL_RECIPIENTS%" == "" goto gotMonitorAlertEmailRecipients
set JEYZER_MONITOR_ALERT_EMAIL_RECIPIENTS=${jeyzer.monitor.publish.email.recipients}
:gotMonitorAlertEmailRecipients

rem Generate JZR report
if not "%JEYZER_JZR_REPORT_ENABLED%" == "" goto gotAnalyzerReportEnabled
set JEYZER_JZR_REPORT_ENABLED=${jeyzer.monitor.publish.email.report.generation}
:gotAnalyzerReportEnabled

rem Activate JIRA connectivity
if not "%JEYZER_MONITOR_JIRA_ENABLED%" == "" goto gotMonitorJiraEnabled
set JEYZER_MONITOR_JIRA_ENABLED=${jeyzer.monitor.publish.jira}
:gotMonitorJiraEnabled

rem Activate Zabbix connectivity
if not "%JEYZER_MONITOR_ZABBIX_ENABLED%" == "" goto gotMonitorZabbixEnabled
set JEYZER_MONITOR_ZABBIX_ENABLED=${jeyzer.monitor.publish.zabbix}
:gotMonitorZabbixEnabled

rem Deploy on the web the monitoring event docs
if not "%JEYZER_MONITOR_WEB_ENABLED%" == "" goto gotMonitorWebEnabled
set JEYZER_MONITOR_WEB_ENABLED=${jeyzer.monitor.publish.web}
:gotMonitorWebEnabled

rem The Web server deploy directory for the generated monitoring resources (status and images)
if not "%JEYZER_MONITOR_WEB_DEPLOY_DIR%" == "" goto gotMonitorWebDeployDir
set JEYZER_MONITOR_WEB_DEPLOY_DIR=${jeyzer.monitor.publish.web.deploy.dir}
:gotMonitorWebDeployDir

rem Emit sounds upon events
if not "%JEYZER_MONITOR_SOUND_ENABLED%" == "" goto gotMonitorSoundEnabled
set JEYZER_MONITOR_SOUND_ENABLED=${jeyzer.monitor.publish.sound}
:gotMonitorSoundEnabled

goto end

:end
exit /b 0
