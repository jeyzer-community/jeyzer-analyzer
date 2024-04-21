package org.jeyzer.monitor.impl.rule.session.advanced;

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

import static org.jeyzer.monitor.config.engine.ConfigMonitorThreshold.THRESHOLD_GLOBAL_CUSTOM;
import static org.jeyzer.monitor.config.engine.ConfigMonitorThreshold.THRESHOLD_SESSION_CUSTOM;

import java.util.Arrays;
import java.util.List;

import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.analyzer.data.stack.ThreadStack;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.monitor.config.engine.ConfigMonitorRule;
import org.jeyzer.monitor.config.engine.ConfigMonitorThreshold;
import org.jeyzer.monitor.engine.event.MonitorSessionEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;
import org.jeyzer.monitor.engine.event.info.SubLevel;
import org.jeyzer.monitor.engine.rule.MonitorSessionRule;
import org.jeyzer.monitor.engine.rule.condition.session.CustomSessionProvider;
import org.jeyzer.monitor.impl.event.session.advanced.ContentionTypeAndCPUProcessEvent;

public class ContentionTypeAndCPUProcessRule extends MonitorSessionRule implements CustomSessionProvider {

	public static final String RULE_NAME = "Contention type and high process CPU";
	
	public static final String CONTENTION_TYPE_PARAM_NAME = "contention_type";
	public static final String PROCESS_CPU_PERCENT_PARAM_NAME = "process_cpu_percent";

	public static final String RULE_CONDITION_DESCRIPTION = "Contention type ("+ CONTENTION_TYPE_PARAM_NAME + ") is detected\n"
			+ "Process CPU percentage is higher or equal to (" + PROCESS_CPU_PERCENT_PARAM_NAME + ")"
			;
	public static final String RULE_NARRATIVE = "The " + RULE_NAME + " rule permits to detect contentions under high CPU peaks.";
	
	public ContentionTypeAndCPUProcessRule(ConfigMonitorRule def) throws JzrInitializationException {
		super(def, RULE_NAME, RULE_CONDITION_DESCRIPTION);
	}

	@Override
	public MonitorSessionEvent createSessionEvent(MonitorEventInfo info, ThreadDump td) {
		return new ContentionTypeAndCPUProcessEvent(info, td);
	}

	@Override
	public boolean isAdvancedMonitoringBased() {
		return true;
	}
	
	@Override
	protected List<String> getSupportedThresholdTypes(){
		return Arrays.asList(THRESHOLD_SESSION_CUSTOM, THRESHOLD_GLOBAL_CUSTOM);
	}

	@Override
	public boolean matchCustomParameters(ThreadDump dump, ConfigMonitorThreshold thresholdConfig) {		
		long processCPUPercentThreshold = getValue(thresholdConfig, PROCESS_CPU_PERCENT_PARAM_NAME);
		if (dump.getProcessCpu() >= processCPUPercentThreshold) {
			return matchContentionType(dump,thresholdConfig);
		}
		return false;
	}

	private boolean matchContentionType(ThreadDump dump, ConfigMonitorThreshold thresholdConfig) {
		String contentionTypePattern = thresholdConfig.getCustomParameter(CONTENTION_TYPE_PARAM_NAME);
		if (contentionTypePattern == null || contentionTypePattern.isEmpty())
			return false;
		
		for (ThreadStack stack : dump.getWorkingThreads()){
			if (stack.getContentionTypeTags().contains(contentionTypePattern))
				return true;
		}

		return false;
	}

	private long getValue(ConfigMonitorThreshold thresholdConfig, String paramName) {
		String paramValue = thresholdConfig.getCustomParameter(paramName);
		if (paramValue == null)
			return -1;
		
		try {
			return Long.parseLong(paramValue);
		} catch (NumberFormatException e) {
			return -1;
		}
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
