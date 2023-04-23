
                                     JEYZER - Analyzer & Monitoring Software

  What is it?
  -----------

  Jeyzer is a Java process monitoring and reporting tool suite.
  Based on the parsing of periodic thread dumps / JZR recording snapshots / JFR recording issued from a java application, the tool suite allows : 
  - The generation of process activity reports in Excel format, also called JZR reports, either through command line or web interface. Cf. jeyzer-analyzer-web-server.bat/sh 
  - The monitoring of the activity including the generation of an event journal and the alert sending. Cf. jeyzer-monitor.bat/sh (JFR not supported)
  - The graphical visualization of thread activity in live or replay mode. Cf. jeyzer-monitor-console.bat/sh and jeyzer-replay.bat/sh (JFR not supported)
  Goal of the tool is to make easier, in a non-invasive manner :
  - The detection of java locks, deadlocks and suspended threads
  - The detection of performance bottlenecks
  - The detection of heavy load situations
  - The collecting of applicative data : functionality, scheduled activity, etc.
  - The collecting of applicative applicative and business events.
  - The collecting of technical data : database call, I/O access, etc.
  - The collecting of metrics : CPU, memory, thread state, garbage collection, disk space usages
   
  Documentation
  -------------

  The most up-to-date documentation will be available on line on https://jeyzer.org/documentation

  
  System Requirements
  -------------------

  JDK/JRE:
    1.8 or above
    Certified on Amazon Coretto 8, 11 and 17. Tested as well on OpenJDK and Oracle JDK 8, 11 and 17.
    JFR analysis requires Java 11 or 17.
  Memory:
    No minimum requirement.
  Disk:
    No minimum requirement. 
  Operating System:
    Unix or Windows. 


  Deploying Jeyzer
  ----------------------------	

  Jeyzer can be deployed through 2 ways :
  
  A) Docker
  
  Jeyzer Docker image is available on Docker Hub.
  Run command is :
    docker run -p 80:9080 -v "C:\jeyzer\profiles\external":/data/jeyzer/profiles/external -v "C:\jeyzer\work":/data/jeyzer/work -ti --rm -e DISPLAY=host.docker.internal:0.0 jeyzer
  By default it starts the Jeyzer Web Analyzer on internal port 9080, to be mapped externally.
  The DISPLAY=host.docker.internal:0.0 parameter is required if you intend to display Xterm windows (use then the XWinrc server on Windows).
  The Jeyzer Docker image contains :
   - Standard Jeyzer installation under /data/jeyzer
   - Jeyzer installer package under /data/jeyzer-installer, which was used to deploy Jeyzer in silent mode at Docker image build time.
   - Amazon Corretto 11 (amazoncorretto:11 image)
   - Amazon Linux distribution
  Mounted volumes are optional and do cover :
   - External Jeyzer profiles : add/update here any external profile. Stored inside /data/jeyzer/profiles/external, mapped to your local drive. 
     Important : Adding a profile required to restart the Jeyzer Web Analyzer (and therefore the container).
   - Working directory : where the JZR recordings get stored.
  PS: local directories get automatically created.
  Portal Jeyzer Docker image, named jeyzer-portal, is also available :
    docker run -p 80:9080 -v C:\jeyzer\profiles\external:/data/jeyzer/profiles/external -v C:\jeyzer\work:/data/jeyzer/work jeyzer-portal

  B) Jeyzer installer
  
  Jeyzer installer permits to select and deploy the different Jeyzer components under Windows and Linux.
  Jeyzer Web Analyzer, Jeyzer Monitor, Jeyzer Recorder Agent, Jeyzer Recorder Client, Jeyzer Analyzer Console, Jeyzer demos.
  Start command is :
    java -jar jeyzer-installer.jar [auto-install.xml]
  When provided, the auto-install.xml permits to perform silent installations using predefined configuration values retrieved from the same file. 
  To create the auto-install.xml, use the Jeyzer UI installer in UI mode : the last installation panel will permit to save it.
  Java prerequisites : JRE v8 or v11. 
  The Jeyzer installer will set the Jeyzer JAVA_HOME path with its own Java runtime path, meaning the one used to start the installer.
  Jeyzer Ecosystem and Recorder installers have their own MD5 checksum.
  

  Configuring Jeyzer
  ----------------------------	
  
  Jeyzer Installer permits to configure the main parameters of each Jeyzer application.
  If you need to update it manually or if the installer has not been used :
  
  For the Jeyzer Web application, edit only the web/apache-tomcat-<version>/bin/setenv.bat|sh 
  If the Jeyzer Web application is embedded into a portal, create the web/apache-tomcat-<version>/bin/setenv-portal.bat/sh file 
    and add there the optional JEYZER_WEB_PORTAL_ prefixed variables (described above).
  
  For the other Jeyzer applications :
  
  1) Open the Jeyzer application start script (ex: analyzer/bin/jeyzer-analysis.bat|sh)

  2) Edit the CONFIGURATION section
  
  3) Open the analyzer/bin/set-jeyzer-paths.bat
  
  4) Edit the relevant paths configuration section depending on the JEYZER_INSTALLER_DEPLOYMENT value
  
  5) For the Jeyzer Monitor, open also the analyzer/bin/set-jeyzer-monitor-paths.bat
  
  6) Edit the relevant paths configuration section depending on the JEYZER_INSTALLER_DEPLOYMENT value
	
  7) For the Jeyzer Monitor, if you need to monitor multiple applications in parallel, edit the multi_monitor.xml 
     to configure the different monitoring target sections

  Jeyzer can be easily integrated in any devOps platform : the main Jeyzer configuration can be indeed defined outside. 
  In this approach, the start scripts set the environment variables only if those do not exist in the ambient environment.
  In a devOps platform, it permits for example to customize the Jeyzer configuration depending on the target monitored application.
  Note that the Linux service file is deployed by the installer. Follow the instructions below to register it.
  
  Jeyzer configuration uses the ISO-8601 time format. The PT prefix is optional. ex: period=30s, time=3m30s, time_to_live=10h
  
  If Jeyzer Installer has been used, the JAVA_HOME is set in the analyzer/bin/check-java.bat, otherwise it is taken from the ambient environment.
  
  To determine if Jeyzer has been installed using the Jeyzer Installer, check the JEYZER_INSTALLER_DEPLOYMENT boolean value in the analyzer/bin/set-jeyzer-paths.bat.

  
  Running the Jeyzer Web Analyzer
  --------------------------------

  1) Run the analyzer/bin/jeyzer-analyzer-web-server.bat|sh 
     with the start (spawn console) or run command  line parameter

  2) The JZR report will generated in the <JEYZER_OUTPUT_DIR>/analysis directory
  
  
  Running the Jeyzer Web Analyzer as a Linux service (RedHat / CentOS 7)
  --------------------------------

  1) Register and start the Jeyzer Web Analyzer as a service :
     Copy the analyzer/bin/jeyzer-analyzer-web.service inside the /etc/systemd/system directory
     Execute these commands :
      systemctl daemon-reload
      systemctl enable jeyzer-analyzer-web.service
      systemctl start jeyzer-analyzer-web.service
    Check the running status :
      systemctl --all | grep jeyzer

  2) The JZR report will generated in the <JEYZER_OUTPUT_DIR>/analysis directory
  
  
  Running the Jeyzer Analyzer
  ----------------------------

  1) Run the analyzer/bin/jeyzer-analysis.bat|sh

  2) The JZR report will generated in the <JEYZER_OUTPUT_DIR>/analysis directory
 
  
  Running the Jeyzer Monitor
  ---------------------------
  
  1) Run the analyzer/bin/jeyzer-monitor.bat|sh

  2) The monitoring reports will be generated in the <JEYZER_OUTPUT_DIR>/monitor directory


  Running the Jeyzer Multi-Monitor
  ----------------------------------

  1) Run the analyzer/bin/jeyzer-multi-monitor.bat|sh

  2) Monitoring reports will be generated in the <JEYZER_RECORDINGS_ROOT_DIR>/<[JEYZER_TARGET_PROFILE]>/monitor directories


  Running the Jeyzer Replay
  ---------------------------

  1) Run the analyzer/bin/jeyzer-replay.bat|sh  

  2) The visualizer application will open and display the thread activity. 
     Open it as wide as possible to get the best display.


  Running the Jeyzer Monitoring Console
  ---------------------------------------

  1) Run the analyzer/bin/jeyzer-monitor-console.bat|sh

  2) The visualizer application will open and display the thread activity. 
     Open it as wide as possible to get the best display.


  Running the Jeyzer Monitoring Oneshot
  ---------------------------------------

  1) Run the analyzer/bin/jeyzer-monitor-oneshot.bat|sh  

  2) The monitoring reports will be generated in the <JEYZER_OUTPUT_DIR>/monitor directory
     Script will exit upon monitoring completion.


  Jeyzer logs
  --------------------------------------

  Jeyzer Web log is available in <JEYZER_HOME>/analysis/log/jeyzer_web_analyzer.log
  Jeyzer Multi Monitor log is available in <JEYZER_HOME>/analysis/log/jeyzer_multi_monitor.log
  Jeyzer Monitor log is available in <JEYZER_OUTPUT_DIR>/log/jeyzer_monitor-<JEYZER_TARGET_PROFILE>.log 
  
  To change the log level to DEBUG, edit the <JEYZER_ANALYZER_CONFIG_DIR>/log/<Jeyzer application>-logback.xml
  and update the following lines :
  <logger name="org.jeyzer.analyzer" level="DEBUG" />
  <logger name="org.jeyzer.monitor" level="DEBUG" />
  
  
  Licensing
  ---------

  This program is free software distributed under the terms of the Jeyzer Community License Agreement Version 1.1.
  Please see the file called LICENSE.txt in the licenses directory.
  
  
  Contact Info
  ------------
  If you have any questions, comments or suggestions, we would like to hear from
  you.  For reporting bugs, you can get a dump of program settings by clicking
  Support on the Help menu.  Copy and paste the settings into an email to help
  us track down problems.

    Email:  contact@jeyzer.org 
    Web:    https://www.jeyzer.org/

  