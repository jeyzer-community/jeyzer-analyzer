
# =======================================================
# Jeyzer Web Server variables
#  for portal integration like jeyzer.org
#
# File must be deployed manually in the CATALINA_HOME/bin directory
#  along side with the Jeyzer customized setenv.sh
# 
# Jeyzer Web uses JEYZER_WEB variable prefix 
# Jeyzer Analyzer uses JEYZER_ANALYZER variable prefix 
# =======================================================

# The unique profile to use for the online analyzer
JEYZER_ANALYZER_DEFAULT_PROFILE=Portal
export JEYZER_ANALYZER_DEFAULT_PROFILE

# Display the Jeyzer title
JEYZER_WEB_PORTAL_TITLE_DISPLAY=false
export JEYZER_WEB_PORTAL_TITLE_DISPLAY

# Display the Jeyzer profiles
JEYZER_WEB_PORTAL_PROFILES_DISPLAY=false
export JEYZER_WEB_PORTAL_PROFILES_DISPLAY

# Display the period automatic detection check box
JEYZER_WEB_PORTAL_PERIOD_DISPLAY=false
export JEYZER_WEB_PORTAL_PERIOD_DISPLAY

# Display the agreement check box
JEYZER_WEB_PORTAL_AGREEMENT_DISPLAY=true
export JEYZER_WEB_PORTAL_AGREEMENT_DISPLAY

# Display the time zone detection check box
JEYZER_WEB_PORTAL_TIME_ZONE_DISPLAY=true
export JEYZER_WEB_PORTAL_TIME_ZONE_DISPLAY

# The Jeyzer privacy policy to accept
JEYZER_WEB_PORTAL_PRIVACY_POLICY_URL=http://127.0.0.1/wordpress/privacy-policy
export JEYZER_WEB_PORTAL_PRIVACY_POLICY_URL

# Enable the Captcha
# If enabled, the above agreement check box must also be displayed
JEYZER_WEB_PORTAL_CAPTCHA_DISPLAY=true
export JEYZER_WEB_PORTAL_CAPTCHA_DISPLAY

# The Captcha keys
# https://www.google.com/recaptcha/admin/create
JEYZER_WEB_PORTAL_CAPTCHA_SKEY=6Lc4SN0UAAAAANyKMYYe0rEnzMjv7Zf5cMwENc4v
export JEYZER_WEB_PORTAL_CAPTCHA_SKEY
JEYZER_WEB_PORTAL_CAPTCHA_WEBSITE_KEY=6Lc4SN0UAAAAANBa8rz3Ch7qBLaLO-6nZU1OvDb_
export JEYZER_WEB_PORTAL_CAPTCHA_WEBSITE_KEY

# Web parameters
#  Max file size in Mb
JEYZER_WEB_UPLOAD_RECORDING_MAX_SIZE=10
export JEYZER_WEB_UPLOAD_RECORDING_MAX_SIZE
JEYZER_WEB_UPLOAD_RECORDING_UNCOMPRESSED_MAX_SIZE=150
export JEYZER_WEB_UPLOAD_RECORDING_UNCOMPRESSED_MAX_SIZE
# 3 hours for a 30 sec period recording
JEYZER_WEB_UPLOAD_RECORDING_UNCOMPRESSED_MAX_FILES=362
export JEYZER_WEB_UPLOAD_RECORDING_UNCOMPRESSED_MAX_FILES
JEYZER_WEB_ANALYZER_THREAD_POOL_SIZE=3
export JEYZER_WEB_ANALYZER_THREAD_POOL_SIZE
JEYZER_WEB_DISPLAY_UFO_STACK_FILE_LINK=false
export JEYZER_WEB_DISPLAY_UFO_STACK_FILE_LINK
JEYZER_WEB_DISPLAY_FUNCTION_DISCOVERY=false
export JEYZER_WEB_DISPLAY_FUNCTION_DISCOVERY
