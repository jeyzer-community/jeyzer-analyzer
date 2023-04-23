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



import static org.jeyzer.monitor.config.engine.ConfigMonitorThreshold.THRESHOLD_SYSTEM_PATTERN;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.jeyzer.analyzer.data.jar.ProcessJarVersion;
import org.jeyzer.analyzer.data.jar.ProcessJars;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.analyzer.session.JzrSession;
import org.jeyzer.monitor.config.engine.ConfigMonitorRule;
import org.jeyzer.monitor.engine.event.MonitorSystemEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;
import org.jeyzer.monitor.engine.event.info.SubLevel;
import org.jeyzer.monitor.engine.rule.MonitorSystemRule;
import org.jeyzer.monitor.engine.rule.condition.system.PatternSystemProvider;
import org.jeyzer.monitor.impl.event.system.ProcessJarNameAbsenceEvent;

public class ProcessJarNameAbsenceRule extends MonitorSystemRule implements PatternSystemProvider{

	public static final String RULE_NAME = "Process jar name absence";
	
	public static final String RULE_NARRATIVE = "The " + RULE_NAME + " rule permits to detect the absences of a given jar file."
			+ "It is useful to detect the absence of external recommended libraries, such as a module extension or a Java agent.";
	
	private String extraInfo;
	
	public ProcessJarNameAbsenceRule(ConfigMonitorRule def) throws JzrInitializationException {
		super(def, RULE_NAME + " : " + def.getExtraInfo(),
				"Process jar name with the (pattern) regex is not found.");
		this.extraInfo = def.getExtraInfo();
	}

	@Override
	public boolean isAdvancedMonitoringBased() {
		return true;
	}
	
	@Override
	protected List<String> getSupportedThresholdTypes(){
		return Arrays.asList(THRESHOLD_SYSTEM_PATTERN);
	}

	@Override
	public boolean matchPattern(JzrSession session, Pattern pattern) {
		ProcessJars processJars = session.getProcessJars();
		if  (processJars == null)
			return false; // not available
		
		for (ProcessJarVersion processJarVersion : processJars.getJarVersions()){
			if (pattern.matcher(processJarVersion.getJarName()).find())
				return false;
		}
		
		return true; // not found
	}

	@Override
	public MonitorSystemEvent createSystemEvent(MonitorEventInfo info) {
		return new ProcessJarNameAbsenceEvent(this.extraInfo, info);
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
