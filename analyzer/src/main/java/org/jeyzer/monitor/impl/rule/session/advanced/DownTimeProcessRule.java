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





import static org.jeyzer.analyzer.math.FormulaHelper.convertToSeconds;
import static org.jeyzer.monitor.config.engine.ConfigMonitorThreshold.THRESHOLD_SESSION_CUSTOM;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import org.jeyzer.analyzer.config.ConfigUtil;
import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.monitor.config.engine.ConfigMonitorRule;
import org.jeyzer.monitor.config.engine.ConfigMonitorThreshold;
import org.jeyzer.monitor.engine.event.MonitorSessionEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;
import org.jeyzer.monitor.engine.event.info.SubLevel;
import org.jeyzer.monitor.engine.rule.MonitorSessionRule;
import org.jeyzer.monitor.engine.rule.condition.session.CustomSessionProvider;
import org.jeyzer.monitor.impl.event.session.advanced.DownTimeProcessEvent;

public class DownTimeProcessRule extends MonitorSessionRule implements CustomSessionProvider {

	public static final String RULE_NAME = "Process down time";
	
	public static final String DOWN_TIME_PARAM_NAME = "down_time";
	public static final long DOWN_TIME_DEFAULT_VALUE = 600; // 10 mn
	
	public static final String RULE_CONDITION_DESCRIPTION = "Recording snapshot process up time is lower than previous one and time interval is greater than (" + DOWN_TIME_PARAM_NAME + ").";
	public static final String RULE_NARRATIVE = "The " + RULE_NAME + " rule permits to detect long process down times.";
	
	public DownTimeProcessRule(ConfigMonitorRule def) throws JzrInitializationException {
		super(def, RULE_NAME, RULE_CONDITION_DESCRIPTION);
	}
	
	@Override
	public boolean matchCustomParameters(ThreadDump dump, ConfigMonitorThreshold thresholdConfig) {
		return dump.isRestart() 
				&& convertToSeconds(dump.getTimeSlice()) >= getDownTimeLimit(thresholdConfig);
	}

	@Override
	public MonitorSessionEvent createSessionEvent(MonitorEventInfo info, ThreadDump td) {
		return new DownTimeProcessEvent(info, td);
	}

	@Override
	public boolean isAdvancedMonitoringBased() {
		return true;
	}
	
	@Override
	protected List<String> getSupportedThresholdTypes(){
		return Arrays.asList(THRESHOLD_SESSION_CUSTOM);
	}
	
	@Override
	public SubLevel getDefaultSubLevel() {
		return SubLevel.DEFAULT_HIGH;
	}
	
	@Override
	public String getDefaultNarrative() {
		return RULE_NARRATIVE;
	}
	
	private long getDownTimeLimit(ConfigMonitorThreshold thresholdConfig) {
		String paramValue = thresholdConfig.getCustomParameter(DOWN_TIME_PARAM_NAME);
		if (paramValue == null)
			return DOWN_TIME_DEFAULT_VALUE;
		
		Duration duration = ConfigUtil.parseDuration(paramValue);
		if (duration == null)
			return DOWN_TIME_DEFAULT_VALUE;
				
		return duration.getSeconds();
	}
}
