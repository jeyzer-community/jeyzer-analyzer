package org.jeyzer.monitor.impl.rule.session;

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







import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.jeyzer.monitor.impl.event.session.ActiveNamedThreadLimitEvent;

public class ActiveNamedThreadLimitRule extends MonitorSessionRule implements CustomSessionProvider {

	public static final String RULE_NAME = "Active named thread limit";
	
	public static final String RULE_CONDITION_DESCRIPTION = "Number of active named threads (like thread pool members) is greater or equal to (value).\n"
			+ "Thread naming is determined based on thread name regex (pattern).";
	public static final String RULE_NARRATIVE = "The " + RULE_NAME + " rule permits to detect high activity within for example a thread pool."
			+ "This rule can be used as a good indicator for pool size tuning.";
	
	// can be null
	private String threadName;
	
	public ActiveNamedThreadLimitRule(ConfigMonitorRule def) throws JzrInitializationException {
		super(def, RULE_NAME + " : " + def.getExtraInfo(), RULE_CONDITION_DESCRIPTION);
		threadName = def.getExtraInfo();
	}

	@Override
	public boolean matchCustomParameters(ThreadDump dump, ConfigMonitorThreshold thresholdConfig) {
		List<ThreadStack> activeNamedThreads = getActiveNamedThreads(dump, thresholdConfig.getPattern());
		return activeNamedThreads.size() >= thresholdConfig.getValue();
	}

	@Override
	public MonitorSessionEvent createSessionEvent(MonitorEventInfo info, ThreadDump td) {
		return new ActiveNamedThreadLimitEvent(info, td, threadName);
	}

	@Override
	public boolean isAdvancedMonitoringBased() {
		return false;
	}

	@Override
	protected List<String> getSupportedThresholdTypes(){
		return Arrays.asList(THRESHOLD_SESSION_CUSTOM, THRESHOLD_GLOBAL_CUSTOM);
	}
	
	@Override
	public SubLevel getDefaultSubLevel() {
		return SubLevel.DEFAULT_MEDIUM;
	}
	
	@Override
	public String getDefaultNarrative() {
		return RULE_NARRATIVE;
	}

	/**
	 * Get the list of active threads matching a thread name pattern (like thread pool name)
	 */
	private List<ThreadStack> getActiveNamedThreads(ThreadDump td, Pattern pattern){
		List<ThreadStack> activeNamedThreads = new ArrayList<>();
		
		for (ThreadStack ts : td.getWorkingThreads()){
			Matcher matcher = pattern.matcher(ts.getName());
			if (matcher.find())
				activeNamedThreads.add(ts);
		}
		
		return activeNamedThreads;
	}
}
