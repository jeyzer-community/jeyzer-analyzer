package org.jeyzer.analyzer.input.translator.jfr.mapper;

/*-
 * ---------------------------LICENSE_START---------------------------
 * Jeyzer Analyzer
 * --
 * Copyright (C) 2020 - 2021 Jeyzer
 * --
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 * ----------------------------LICENSE_END----------------------------
 */

import static org.jeyzer.analyzer.input.translator.jfr.reader.JFRDescriptor.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jeyzer.analyzer.input.translator.jfr.reader.JFRDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jdk.jfr.ValueDescriptor;
import jdk.jfr.consumer.RecordedEvent;

public class JZRProcessCardMapper {
	
	private static final Logger logger = LoggerFactory.getLogger(JZRProcessCardMapper.class);
	
	// JVMInformation
	  // startTime = 18:02:59.872
	  // jvmName = "Java HotSpot(TM) 64-Bit Server VM"
	  // jvmVersion = "Java HotSpot(TM) 64-Bit Server VM (11.0.6+8-LTS) for windows-amd64 JRE (11.0.6+8-LTS), built on Dec 11 2019 09:17:57 by "mach5one" with MS VC++ 15.5 (VS2017)"
	  // jvmArguments = "-javaagent:C:\demo-pg\jeyzer-262\recorder\lib\jeyzer-agent.jar=C:\demo-pg\jeyzer-262\demo\config\record\agent\jeyzer-agent.xml -Djeyzer.publisher.active=true -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=2500 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -XX:+UseBiasedLocking -Xmx256m -Xms128m --module-path=C:\demo-pg\jeyzer-262\demo\mods --add-modules=org.jeyzer.publish,org.jeyzer.demo.shared,org.slf4j,org.jeyzer.demo"
	  // jvmFlags = null
	  // javaArguments = "org.jeyzer.demo.features.FeatureDemo"
      // jvmStartTime = 18:01:07.994
	
	private static final Map<String, String> jvmInformation = new HashMap<>();
	static {
		jvmInformation.put(JFR_FIELD_START_TIME, "jfr.start.time");
		jvmInformation.put("jvmName",       "java.vm.name");
		jvmInformation.put("jvmVersion",    "jfr.process.jvm.version");
		jvmInformation.put("jvmArguments",  "jzr.ext.process.input.parameters");
		jvmInformation.put("jvmFlags",      "jfr.process.jvm.flags");
		jvmInformation.put("jvmStartTime",  "jzr.ext.process.start.time");
		jvmInformation.put("javaArguments", "sun.java.command");
	}
	
	private static final Map<String, String> cpuInformation = new HashMap<>();
	static {
		cpuInformation.put("cpu",         "jfr.system.cpu");
		cpuInformation.put("description", "jfr.system.cpu.description");
		cpuInformation.put("sockets",     "jfr.system.cpu.sockets");
		cpuInformation.put("cores",       "jfr.system.cpu.cores");
		cpuInformation.put("hwThreads",   "jfr.system.cpu.hwThreads");
	}
	
	private static final Map<String, String> osInformation = new HashMap<>();
	static {
		osInformation.put("osVersion",  "os.version");
	}
	
	private static final Map<String, String> initialEnvVariables = new HashMap<>();
	static {
		initialEnvVariables.put("COMPUTERNAME",         "jfr.system.host.name");
		initialEnvVariables.put("HOMEPATH",             "user.home");
		initialEnvVariables.put("Path",                 "jfr.system.path");
		initialEnvVariables.put("USERNAME",             "user.name");
		initialEnvVariables.put("NUMBER_OF_PROCESSORS", "jzr.ext.process.available.processors");
	}
	
	public long mapJVMInfo(JFRDescriptor descriptor, List<String> entries) {
		long jvmStartTime = -1;

		if (descriptor.getJVMInfoEvents().isEmpty()) {
			logger.warn("JFR JVM information is empty.");
			return jvmStartTime;
		}
		
		if (descriptor.getJVMInfoEvents().size() > 1)
			logger.warn("JFR JVM information : more than one found. JFR analysis is currently processing only one JVM run.");
		
		RecordedEvent event = descriptor.getJVMInfoEvents().get(0); // there should be only 1
		for (ValueDescriptor desc : event.getFields()) {
			if (jvmInformation.containsKey(desc.getName())) {
				if (desc.getName().equals(JFR_FIELD_START_TIME)) {
					entries.add(jvmInformation.get(desc.getName()) + "=" + event.getStartTime().toEpochMilli()); // Epoch time in ms					
				}
				else {
					if (desc.getName().equals("jvmStartTime")) {
						jvmStartTime =	event.getInstant("jvmStartTime").toEpochMilli();
						entries.add(jvmInformation.get(desc.getName()) + "=" + jvmStartTime); // Epoch time in ms
					}
					else {
						entries.add(jvmInformation.get(desc.getName()) + "=" + event.getString(desc.getName()));						
					}
				}
			}
			else {
				logger.info("JFR JVM information not mapped in Jeyzer : " + desc.getName());				
			}
		}
		
		return jvmStartTime;
	}
	
	public void mapOSInfo(JFRDescriptor descriptor, List<String> entries) {
		if (descriptor.getOSInfoEvents().isEmpty()) {
			logger.warn("JFR OS information is empty.");
			return;
		}
		
		if (descriptor.getOSInfoEvents().size() > 1)
			logger.warn("JFR OS information : more than one found. JFR analysis is currently processing only one JVM run.");
			
		RecordedEvent event = descriptor.getOSInfoEvents().get(0);
		for (ValueDescriptor desc : event.getFields()) {
			if (osInformation.containsKey(desc.getName()))
				entries.add(osInformation.get(desc.getName()) + "=" + event.getString(desc.getName()));
			else if (!JFR_FIELD_START_TIME.equals(desc.getName()))
				logger.info("JFR OS information not mapped in Jeyzer : " + desc.getName());					
		}
	}
	
	public void mapCPUInfo(JFRDescriptor descriptor, List<String> entries) {
		if (descriptor.getCPUInfoEvents().isEmpty()) {
			logger.warn("JFR CPU information is empty.");
			return;
		}
		
		RecordedEvent event = descriptor.getCPUInfoEvents().get(0);
		for (ValueDescriptor desc : event.getFields()) {
			if (cpuInformation.containsKey(desc.getName())) {
				if (desc.getTypeName().equals("int"))
					entries.add(cpuInformation.get(desc.getName()) + "=" + event.getInt(desc.getName()));
				else
					entries.add(cpuInformation.get(desc.getName()) + "=" + event.getString(desc.getName()));
			}
			else if (!JFR_FIELD_START_TIME.equals(desc.getName()))
				logger.info("JFR CPU information not mapped in Jeyzer : " + desc.getName());					
		}
	}
	
	public void mapInitialEnvironmentVariable(JFRDescriptor descriptor, List<String> entries) {
		for (RecordedEvent event : descriptor.getInitialEnvironmentVariableEvents()) {
			if (initialEnvVariables.containsKey(event.getString("key")))
				entries.add(initialEnvVariables.get(event.getString("key")) + "=" + event.getString("value"));
			// do not report unknown variable because too many variables
		}
	}

	public void mapInitialSystemProperties(JFRDescriptor descriptor, List<String> entries) {
		for (RecordedEvent tdEvent : descriptor.getSystemPropertyEvents())
			entries.add(tdEvent.getString("key") + "=" + tdEvent.getString("value"));
	}
}
