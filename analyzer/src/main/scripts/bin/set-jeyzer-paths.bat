@echo off

rem If Jeyzer installation result, paths get set automatically
set JEYZER_INSTALLER_DEPLOYMENT=${jeyzer.installer.deployment}
if "%JEYZER_INSTALLER_DEPLOYMENT%" == "true" goto gotPathsSetByInstaller

rem ---------------------------------------------------------------------------------------------------------------
rem Set Jeyzer paths
rem JEYZER_RECORD_DIRECTORY and JEYZER_TARGET_PROFILES_DIR are intended be set externally 
rem    once the Analyzer gets integrated in any DevOps platform
rem Same for JEYZER_RECORDINGS_ROOT_DIR once the Multiple Monitor gets integrated
rem Edit this section if installation was done manually (no installer)
rem Profiles directory structure is self contained : all profiles are under %JEYZER_ANALYZER_CONFIG_DIR%\profiles
rem ---------------------------------------------------------------------------------------------------------------

rem The recording root directory - required for the multi-monitor
if not "%JEYZER_RECORDINGS_ROOT_DIR%" == "" goto gotRecordRootDir
set JEYZER_RECORDINGS_ROOT_DIR=C:/tmp/jeyzer/recordings
:gotRecordRootDir

rem The recording directory
if not "%JEYZER_RECORD_DIRECTORY%" == "" goto gotRecordDir
set JEYZER_RECORD_DIRECTORY=C:/tmp/jeyzer/recordings/%JEYZER_TARGET_PROFILE%
:gotRecordDir

rem The recording gz or zip file name located in the recording directory.
rem Optional. If not set, the recording directory is scanned. Disabled in monitor mode.
rem set JEYZER_RECORD_FILE=recording.zip

rem The root output directory for logs and reports
set JEYZER_OUTPUT_DIR=%JEYZER_RECORD_DIRECTORY%

rem The Jeyzer root configuration directory
set JEYZER_ANALYZER_CONFIG_DIR=%JEYZER_ANALYZER_HOME%\config

rem For facility reasons to access the target profile, base, external and demo profiles get merged
rem The Jeyzer base profile directory
set JEYZER_BASE_MASTER_PROFILES_DIR=%JEYZER_ANALYZER_CONFIG_DIR%\profiles\master
set JEYZER_BASE_SHARED_PROFILES_DIR=%JEYZER_ANALYZER_CONFIG_DIR%\profiles\shared
set JEYZER_BASE_HELPER_PROFILES_DIR=%JEYZER_ANALYZER_CONFIG_DIR%\profiles\helper

rem The Jeyzer external profile directory
set JEYZER_EXTERNAL_MASTER_PROFILES_DIR=%JEYZER_ANALYZER_CONFIG_DIR%\profiles\master
set JEYZER_EXTERNAL_SHARED_PROFILES_DIR=%JEYZER_ANALYZER_CONFIG_DIR%\profiles\shared

rem The Jeyzer demo profiles directory (must be set for demos)
set JEYZER_DEMO_MASTER_PROFILES_DIR=%JEYZER_ANALYZER_CONFIG_DIR%\profiles\master
set JEYZER_DEMO_SHARED_PROFILES_DIR=%JEYZER_ANALYZER_CONFIG_DIR%\profiles\shared

rem The Jeyzer target profiles directory, to be set externally once the Analyzer gets integrated in any DevOps platform
rem Otherwise points to any of the external, base or demo profiles 
if not "%JEYZER_TARGET_PROFILES_DIR%" == "" goto gotManualTargetProfileDir
set JEYZER_TARGET_PROFILES_DIR=%JEYZER_ANALYZER_CONFIG_DIR%\profiles\master
:gotManualTargetProfileDir

rem The external place where the Jeyzer shared repositories are declared
if not "%JEYZER_EXTERNAL_REPOSITORY_SETUP_DIRECTORY%" == "" goto gotExternalRepositorySetupDir
set JEYZER_EXTERNAL_REPOSITORY_SETUP_DIRECTORY=%JEYZER_ANALYZER_CONFIG_DIR%\shared-repositories
:gotExternalRepositorySetupDir

rem Deobfuscation configuration home
set JEYZER_DEOBSFUCATION_CONFIG_DIR=%JEYZER_ANALYZER_CONFIG_DIR%\deobfuscation\mappings

goto end

rem ---------------------------------------------------------------------------------------------------------------
rem Jeyzer paths automatically set  
rem JEYZER_RECORD_DIRECTORY and JEYZER_TARGET_PROFILES_DIR are intended be set externally 
rem    once the Analyzer gets integrated in any DevOps platform
rem Same for JEYZER_RECORDINGS_ROOT_DIR once the Multiple Monitor gets integrated
rem Feel free to adjust manually or re-run the installer
rem Profiles directory structure is Jeyzer ecosystem oriented: it reflects the Jeyzer Web one 
rem ---------------------------------------------------------------------------------------------------------------

:gotPathsSetByInstaller

rem The recording root directory - required for the multi-monitor
if not "%JEYZER_RECORDINGS_ROOT_DIR%" == "" goto gotRecordRootDir
set JEYZER_RECORDINGS_ROOT_DIR=${jeyzer.analyzer.work.dir}/recordings
:gotRecordRootDir

rem The recording directory
if not "%JEYZER_RECORD_DIRECTORY%" == "" goto gotRecordDir
set JEYZER_RECORD_DIRECTORY=${jeyzer.analyzer.work.dir}/recordings/%JEYZER_TARGET_PROFILE%
:gotRecordDir

rem The recording gz or zip file name located in the recording directory.
rem Optional. If not set, the recording directory is scanned. Disabled in monitor mode.
rem set JEYZER_RECORD_FILE=recording.zip

rem The root output directory for logs and reports
set JEYZER_OUTPUT_DIR=%JEYZER_RECORD_DIRECTORY%

rem The Jeyzer root configuration directory
set JEYZER_ANALYZER_CONFIG_DIR=${INSTALL_PATH}/analyzer/config

rem The Jeyzer external profile directory
set JEYZER_EXTERNAL_MASTER_PROFILES_DIR=${jeyzer.analyzer.ext.profiles.dir}/master
set JEYZER_EXTERNAL_SHARED_PROFILES_DIR=${jeyzer.analyzer.ext.profiles.dir}/shared

rem The Jeyzer base profile directory
set JEYZER_BASE_MASTER_PROFILES_DIR=${INSTALL_PATH}/profiles/base/master
set JEYZER_BASE_SHARED_PROFILES_DIR=${INSTALL_PATH}/profiles/base/shared
set JEYZER_BASE_HELPER_PROFILES_DIR=${INSTALL_PATH}/profiles/base/helper

rem The Jeyzer demo profile directory (must be set for demos)
set JEYZER_DEMO_MASTER_PROFILES_DIR=${INSTALL_PATH}/profiles/demo/master
set JEYZER_DEMO_SHARED_PROFILES_DIR=${INSTALL_PATH}/profiles/demo/shared

rem The Jeyzer target profiles directory, to be set externally once the Analyzer gets integrated in any DevOps platform
rem Otherwise, if not set, point to the demo master profile directory (if available)
if not "%JEYZER_TARGET_PROFILES_DIR%" == "" goto gotAutoTargetProfileDir
set JEYZER_TARGET_PROFILES_DIR=%JEYZER_DEMO_MASTER_PROFILES_DIR%
:gotAutoTargetProfileDir

rem The external place where the Jeyzer shared repositories are declared
rem  to be set externally once the Analyzer gets integrated in any DevOps platform
if not "%JEYZER_EXTERNAL_REPOSITORY_SETUP_DIRECTORY%" == "" goto gotExternalRepositorySetupDir
set JEYZER_EXTERNAL_REPOSITORY_SETUP_DIRECTORY=${INSTALL_PATH}/profiles/shared-repositories
:gotExternalRepositorySetupDir

rem Deobfuscation configuration home
set JEYZER_DEOBSFUCATION_CONFIG_DIR=${jeyzer.analyzer.deobfuscation.dir}

goto end

:end
exit /b 0
