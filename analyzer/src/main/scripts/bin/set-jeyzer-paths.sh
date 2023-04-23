#!/bin/sh

JEYZER_INSTALLER_DEPLOYMENT=%{jeyzer.installer.deployment}
if [ "$JEYZER_INSTALLER_DEPLOYMENT" != "true" ]; then
  # ---------------------------------------------------------------------------------------------------------------
  # Set Jeyzer paths
  # JEYZER_RECORD_DIRECTORY and JEYZER_TARGET_PROFILES_DIR are intended be set externally 
  #    once the Analyzer gets integrated in any DevOps platform
  # Same for JEYZER_RECORDINGS_ROOT_DIR once the Multiple Monitor gets integrated
  # Edit this section if installation was done manually (no installer)
  # Profiles directory structure is self contained : all profiles are under $JEYZER_ANALYZER_CONFIG_DIR/profiles
  # ---------------------------------------------------------------------------------------------------------------

  # The recording root directory - required for the multi-monitor
  if [ -z "$JEYZER_RECORDINGS_ROOT_DIR" ]; then
    JEYZER_RECORDINGS_ROOT_DIR=/tmp/jeyzer/recordings
	export JEYZER_RECORDINGS_ROOT_DIR
  fi

  # The recording directory 
  if [ -z "$JEYZER_RECORD_DIRECTORY" ]; then
    JEYZER_RECORD_DIRECTORY=/tmp/jeyzer/recordings/$JEYZER_TARGET_PROFILE
	export JEYZER_RECORD_DIRECTORY
  fi
  
  # The recording gz or zip file name located in the recording directory.
  # Optional. If not set, the recording directory is scanned. Disabled in monitor mode.
  # JEYZER_RECORD_FILE=recording.gz
  # export JEYZER_RECORD_FILE

  # The root output directory for logs and reports
  JEYZER_OUTPUT_DIR="$JEYZER_RECORD_DIRECTORY"
  export JEYZER_OUTPUT_DIR

  # The Jeyzer root configuration directory
  JEYZER_ANALYZER_CONFIG_DIR="$JEYZER_ANALYZER_HOME"/config
  export JEYZER_ANALYZER_CONFIG_DIR

  # For facility reasons to access the target profile, base, external and demo profiles get merged
  # The Jeyzer base profile directory
  JEYZER_BASE_MASTER_PROFILES_DIR="$JEYZER_ANALYZER_CONFIG_DIR"/profiles/master
  export JEYZER_BASE_MASTER_PROFILES_DIR
  JEYZER_BASE_SHARED_PROFILES_DIR="$JEYZER_ANALYZER_CONFIG_DIR"/profiles/shared
  export JEYZER_BASE_SHARED_PROFILES_DIR
  JEYZER_BASE_HELPER_PROFILES_DIR="$JEYZER_ANALYZER_CONFIG_DIR"/profiles/helper
  export JEYZER_BASE_HELPER_PROFILES_DIR

  # The Jeyzer external profile directory
  JEYZER_EXTERNAL_MASTER_PROFILES_DIR="$JEYZER_ANALYZER_CONFIG_DIR"/profiles/master
  export JEYZER_EXTERNAL_MASTER_PROFILES_DIR
  JEYZER_EXTERNAL_SHARED_PROFILES_DIR="$JEYZER_ANALYZER_CONFIG_DIR"/profiles/shared
  export JEYZER_EXTERNAL_SHARED_PROFILES_DIR

  # The Jeyzer demo profiles directory (must be set for demos)
  JEYZER_DEMO_MASTER_PROFILES_DIR="$JEYZER_ANALYZER_CONFIG_DIR"/profiles/master
  export JEYZER_DEMO_MASTER_PROFILES_DIR
  JEYZER_DEMO_SHARED_PROFILES_DIR="$JEYZER_ANALYZER_CONFIG_DIR"/profiles/shared
  export JEYZER_DEMO_SHARED_PROFILES_DIR

  # The Jeyzer target profiles directory, to be set externally once the Analyzer gets integrated in any DevOps platform
  # Otherwise points to any of the external, base or demo profiles 
  if [ -z "$JEYZER_TARGET_PROFILES_DIR" ]; then
    JEYZER_TARGET_PROFILES_DIR="$JEYZER_ANALYZER_CONFIG_DIR"/profiles/master
	export JEYZER_TARGET_PROFILES_DIR
  fi
  
  # The external place where the Jeyzer shared repositories are declared
    if [ -z "$JEYZER_EXTERNAL_REPOSITORY_SETUP_DIRECTORY" ]; then
    JEYZER_EXTERNAL_REPOSITORY_SETUP_DIRECTORY="$JEYZER_ANALYZER_CONFIG_DIR"/shared-repositories
	export JEYZER_EXTERNAL_REPOSITORY_SETUP_DIRECTORY
  fi
  
  # Deobfuscation configuration home
  JEYZER_DEOBSFUCATION_CONFIG_DIR="$JEYZER_ANALYZER_CONFIG_DIR"/deobfuscation/mappings
  export JEYZER_DEOBSFUCATION_CONFIG_DIR
  
else  

  # ---------------------------------------------------------------------------------------------------------------
  # Jeyzer paths automatically set  
  # JEYZER_RECORD_DIRECTORY and JEYZER_TARGET_PROFILES_DIR are intended be set externally 
  #    once the Analyzer gets integrated in any DevOps platform
  # Same for JEYZER_RECORDINGS_ROOT_DIR once the Multiple Monitor gets integrated
  # Feel free to adjust manually or re-run the installer
  # Profiles directory structure is Jeyzer ecosystem oriented: it reflects the Jeyzer Web one 
  # ---------------------------------------------------------------------------------------------------------------

  # The recording root directory - required for the multi-monitor
  if [ -z "$JEYZER_RECORDINGS_ROOT_DIR" ]; then
    JEYZER_RECORDINGS_ROOT_DIR="%{jeyzer.analyzer.work.dir}/recordings"
	export JEYZER_RECORDINGS_ROOT_DIR
  fi
  
  # The recording directory
  if [ -z "$JEYZER_RECORD_DIRECTORY" ]; then
    JEYZER_RECORD_DIRECTORY="%{jeyzer.analyzer.work.dir}/recordings/$JEYZER_TARGET_PROFILE"
	export JEYZER_RECORD_DIRECTORY
  fi
  
  # The recording gz or zip file name located in the recording directory.
  # Optional. If not set, the recording directory is scanned. Disabled in monitor mode.
  # JEYZER_RECORD_FILE=recording.gz
  # export JEYZER_RECORD_FILE

  # The root output directory for logs and reports
  JEYZER_OUTPUT_DIR="$JEYZER_RECORD_DIRECTORY"
  export JEYZER_OUTPUT_DIR

  # The Jeyzer root configuration directory
  JEYZER_ANALYZER_CONFIG_DIR=%{INSTALL_PATH}/analyzer/config
  export JEYZER_ANALYZER_CONFIG_DIR

  # The Jeyzer external profile directory
  JEYZER_EXTERNAL_MASTER_PROFILES_DIR=%{jeyzer.analyzer.ext.profiles.dir}/master
  export JEYZER_EXTERNAL_MASTER_PROFILES_DIR
  JEYZER_EXTERNAL_SHARED_PROFILES_DIR=%{jeyzer.analyzer.ext.profiles.dir}/shared
  export JEYZER_EXTERNAL_SHARED_PROFILES_DIR

  # The Jeyzer base profile directory
  JEYZER_BASE_MASTER_PROFILES_DIR=%{INSTALL_PATH}/profiles/base/master
  export JEYZER_BASE_MASTER_PROFILES_DIR
  JEYZER_BASE_SHARED_PROFILES_DIR=%{INSTALL_PATH}/profiles/base/shared
  export JEYZER_BASE_SHARED_PROFILES_DIR
  JEYZER_BASE_HELPER_PROFILES_DIR=%{INSTALL_PATH}/profiles/base/helper
  export JEYZER_BASE_HELPER_PROFILES_DIR

  # The Jeyzer demo profiles directory (must be set for demos)
  JEYZER_DEMO_MASTER_PROFILES_DIR=%{INSTALL_PATH}/profiles/demo/master
  export JEYZER_DEMO_MASTER_PROFILES_DIR
  JEYZER_DEMO_SHARED_PROFILES_DIR=%{INSTALL_PATH}/profiles/demo/shared
  export JEYZER_DEMO_SHARED_PROFILES_DIR

  # The Jeyzer target profiles directory, to be set externally once the Analyzer gets integrated in any DevOps platform
  # Otherwise, if not set, point to the demo master profile directory (if available).
  if [ -z "$JEYZER_TARGET_PROFILES_DIR" ]; then
    JEYZER_TARGET_PROFILES_DIR="$JEYZER_DEMO_MASTER_PROFILES_DIR"
	export JEYZER_TARGET_PROFILES_DIR
  fi
  
  # The external place where Jeyzer repositories (required for shared profiles) are declared
  #  to be set externally once the Analyzer gets integrated in any DevOps platform
  if [ -z "$JEYZER_EXTERNAL_REPOSITORY_SETUP_DIRECTORY" ]; then
    JEYZER_EXTERNAL_REPOSITORY_SETUP_DIRECTORY=%{INSTALL_PATH}/profiles/shared-repositories
	export JEYZER_EXTERNAL_REPOSITORY_SETUP_DIRECTORY
  fi

  # Deobfuscation configuration home
  JEYZER_DEOBSFUCATION_CONFIG_DIR=%{jeyzer.analyzer.deobfuscation.dir}
  export JEYZER_DEOBSFUCATION_CONFIG_DIR
  
fi
