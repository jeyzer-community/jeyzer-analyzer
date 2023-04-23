package org.jeyzer.monitor.impl.rule.system;

/*-
 * ---------------------------LICENSE_START---------------------------
 * Jeyzer Analyzer
 * --
 * Copyright (C) 2020 Jeyzer
 * --
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 * ----------------------------LICENSE_END----------------------------
 */



import static org.jeyzer.monitor.config.engine.ConfigMonitorThreshold.THRESHOLD_SYSTEM_SIGNAL;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jeyzer.analyzer.data.jar.ProcessJarVersion;
import org.jeyzer.analyzer.data.jar.ProcessJarVersionType;
import org.jeyzer.analyzer.data.jar.ProcessJars;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.analyzer.session.JzrSession;
import org.jeyzer.monitor.config.engine.ConfigMonitorRule;
import org.jeyzer.monitor.engine.event.MonitorSystemEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;
import org.jeyzer.monitor.engine.event.info.SubLevel;
import org.jeyzer.monitor.engine.rule.MonitorSystemRule;
import org.jeyzer.monitor.engine.rule.condition.system.SignalSystemProvider;
import org.jeyzer.monitor.impl.event.system.ProcessJarManifestVersionMismatchEvent;

public class ProcessJarManifestVersionMismatchRule extends MonitorSystemRule implements SignalSystemProvider{

	public static final String RULE_NAME = "Process jar manifest version mismatch";
	
	public static final String RULE_NARRATIVE = "The " + RULE_NAME + " checks that the Manifest version attributes match the jar file name version.\n "
			+ "If the versions differ, it means that the jar build is wrong or someone has altered the jar file.\n"
			+ "In both cases, you could run with a different version of the library than you would think.";
	
	private Map<ProcessJarVersion, Map<String, String>> mismatchVersionsPerJar = new HashMap<>(); // for each jar file, get the list of mismatch versions
	
	public ProcessJarManifestVersionMismatchRule(ConfigMonitorRule def) throws JzrInitializationException {
		super(def, RULE_NAME, "Jar file version is different from its Manifest version attributes.");
	}
	
	@Override
	public boolean matchSignal(JzrSession session) {
		ProcessJars processJars = session.getProcessJars();
		if (processJars == null)
			return false;
		
		boolean result = false;
		for (ProcessJarVersion jarVersion : processJars.getJarVersions()) {
			if (jarVersion.hasNoVersion())
				continue;
			String jarFileVersion = jarVersion.getJarVersion(ProcessJarVersionType.JAR_FILE_VERSION);
			if (jarFileVersion == null)
				continue;
			if (!matchManifestVersion(jarFileVersion, jarVersion, ProcessJarVersionType.IMPLEMENTATION_VERSION))
				result = true;
			if (!matchManifestVersion(jarFileVersion, jarVersion, ProcessJarVersionType.SPECIFICATION_VERSION))
				result = true;
			if (!matchManifestVersion(jarFileVersion, jarVersion, ProcessJarVersionType.BUNDLE_VERSION))
				result = true;
		}
		return result;
	}

	private boolean matchManifestVersion(String jarFileVersion, ProcessJarVersion jarVersion, ProcessJarVersionType type) {
		String manifestVersion = jarVersion.getJarVersion(type);
		
		if (manifestVersion== null)
			return true; // do not complain if manifest version is missing
		
		if (!jarFileVersion.equals(manifestVersion)) {
			Map<String, String> mismatchVersions = mismatchVersionsPerJar.computeIfAbsent(jarVersion, p -> new LinkedHashMap<>());
			mismatchVersions.put(type.getAttributeName(), manifestVersion);
			return false;
		}
		
		return true;
	}

	@Override
	public boolean isAdvancedMonitoringBased() {
		return true;
	}

	@Override
	protected List<String> getSupportedThresholdTypes(){
		return Arrays.asList(THRESHOLD_SYSTEM_SIGNAL);
	}

	@Override
	public MonitorSystemEvent createSystemEvent(MonitorEventInfo info) {
		return new ProcessJarManifestVersionMismatchEvent(this.mismatchVersionsPerJar, info);
	}
	
	@Override
	public SubLevel getDefaultSubLevel() {
		return SubLevel.DEFAULT_LOW;
	}
	
	@Override
	public String getDefaultNarrative() {
		return RULE_NARRATIVE;
	}
}
