
rem =======================================================
rem Jeyzer Web Server variables
rem  for portal integration like jeyzer.org
rem  
rem File must be deployed manually in the CATALINA_HOME\bin directory
rem  along side with the Jeyzer customized setenv.bat
rem
rem Jeyzer Web uses JEYZER_WEB_ variable prefix 
rem Jeyzer Analyzer uses JEYZER_ANALYZER variable prefix 
rem =======================================================

rem The unique profile to use for the online analyzer
set JEYZER_ANALYZER_DEFAULT_PROFILE=Portal

rem Display the Jeyzer title
set JEYZER_WEB_PORTAL_TITLE_DISPLAY=false

rem Display the Jeyzer profiles
set JEYZER_WEB_PORTAL_PROFILES_DISPLAY=false

rem Display the time zone detection check box
set JEYZER_WEB_PORTAL_TIME_ZONE_DISPLAY=true

rem Display the period automatic detection check box
set JEYZER_WEB_PORTAL_PERIOD_DISPLAY=false

rem Display the agreement check box
set JEYZER_WEB_PORTAL_AGREEMENT_DISPLAY=true

rem The Jeyzer privacy policy to accept
set JEYZER_WEB_PORTAL_PRIVACY_POLICY_URL=http://127.0.0.1/wordpress/privacy-policy

rem Enable the Captcha
rem If enabled, the above agreement check box must also be displayed
set JEYZER_WEB_PORTAL_CAPTCHA_DISPLAY=true

rem  The Captcha keys
rem  https://www.google.com/recaptcha/admin/create
set JEYZER_WEB_PORTAL_CAPTCHA_SKEY=6Lc4SN0UAAAAANyKMYYe0rEnzMjv7Zf5cMwENc4v
set JEYZER_WEB_PORTAL_CAPTCHA_WEBSITE_KEY=6Lc4SN0UAAAAANBa8rz3Ch7qBLaLO-6nZU1OvDb_

rem Max file size in Mb
set JEYZER_WEB_UPLOAD_RECORDING_MAX_SIZE=10
set JEYZER_WEB_UPLOAD_RECORDING_UNCOMPRESSED_MAX_SIZE=150
rem 3 hours for a 30 sec period recording
set JEYZER_WEB_UPLOAD_RECORDING_UNCOMPRESSED_MAX_FILES=362
set JEYZER_WEB_ANALYZER_THREAD_POOL_SIZE=3
set JEYZER_WEB_DISPLAY_UFO_STACK_FILE_LINK=false
set JEYZER_WEB_DISPLAY_FUNCTION_DISCOVERY=false