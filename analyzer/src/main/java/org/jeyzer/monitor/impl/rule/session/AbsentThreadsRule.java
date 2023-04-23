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
import org.jeyzer.monitor.impl.event.session.AbsentThreadsEvent;

public class AbsentThreadsRule  extends MonitorSessionRule implements CustomSessionProvider {

	public static final String RULE_NAME = "Absent threads";
	
	public static final String RULE_CONDITION_DESCRIPTION = "Number of watched threads is lower or equal to (value).\n"
			+ "Threads watching is determined based on thread name regex (pattern) ans is applied on both active and inactive threads.";
	public static final String RULE_NARRATIVE = "The " + RULE_NAME + " rule permits to detect a low number of particular threads. "
			+ "It is useful to monitor service threads which could for example die unexpectedly.";
	
	// can be null
	private String threadFamily;
	
	public AbsentThreadsRule(ConfigMonitorRule def) throws JzrInitializationException {
		super(def, RULE_NAME + " : " + def.getExtraInfo(), RULE_CONDITION_DESCRIPTION);
		threadFamily = def.getExtraInfo();
	}

	@Override
	public boolean matchCustomParameters(ThreadDump dump, ConfigMonitorThreshold thresholdConfig) {
		List<ThreadStack> threads = getThreads(dump, thresholdConfig.getPattern());
		return threads.size() <= thresholdConfig.getValue();
	}

	@Override
	public MonitorSessionEvent createSessionEvent(MonitorEventInfo info, ThreadDump td) {
		return new AbsentThreadsEvent(info, td, threadFamily);
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
		return SubLevel.DEFAULT_HIGH;
	}
	
	@Override
	public String getDefaultNarrative() {
		return RULE_NARRATIVE;
	}

	/**
	 * Get the list of threads matching a thread name pattern
	 */
	private List<ThreadStack> getThreads(ThreadDump td, Pattern pattern){
		List<ThreadStack> threads = new ArrayList<>();
		
		for (ThreadStack ts : td.getThreads()){
			Matcher matcher = pattern.matcher(ts.getName());
			if (matcher.find())
				threads.add(ts);
		}
		
		return threads;
	}
}
