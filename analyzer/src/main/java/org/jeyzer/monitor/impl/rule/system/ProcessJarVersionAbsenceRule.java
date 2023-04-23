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



import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jeyzer.analyzer.data.jar.ProcessJarVersion;
import org.jeyzer.analyzer.data.jar.ProcessJars;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.analyzer.session.JzrSession;
import org.jeyzer.monitor.config.engine.ConfigMonitorRule;
import org.jeyzer.monitor.engine.event.MonitorSystemEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;
import org.jeyzer.monitor.engine.event.info.SubLevel;
import org.jeyzer.monitor.engine.rule.MonitorSystemRule;
import org.jeyzer.monitor.engine.rule.condition.system.SignalSystemProvider;
import org.jeyzer.monitor.impl.event.system.ProcessJarVersionAbsenceEvent;

public class ProcessJarVersionAbsenceRule extends MonitorSystemRule implements SignalSystemProvider{

	public static final String RULE_NAME = "Process jar version absence";
	
	public static final String RULE_NARRATIVE = "The " + RULE_NAME + " rule checks if the process jars are not versionned."
			+ "This rule should be typically used in production environments and therefore depend on a related sticker.";
	
	private List<ProcessJarVersion> jarVersions = new ArrayList<>();
	
	public ProcessJarVersionAbsenceRule(ConfigMonitorRule def) throws JzrInitializationException {
		super(def, RULE_NAME, "Process jars contain no version.");
	}
	
	@Override
	public boolean matchSignal(JzrSession session) {
		ProcessJars processJars = session.getProcessJars();
		if (processJars == null)
			return false;
		
		for (ProcessJarVersion jarVersion : processJars.getJarVersions()) {
			if (jarVersion.getJarVersion() == null)
				jarVersions.add(jarVersion);
		}
		return !jarVersions.isEmpty();
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
		return new ProcessJarVersionAbsenceEvent(this.jarVersions, info);
	}
	
	@Override
	public SubLevel getDefaultSubLevel() {
		return SubLevel.DEFAULT_VERY_LOW;
	}
	
	@Override
	public String getDefaultNarrative() {
		return RULE_NARRATIVE;
	}
}
