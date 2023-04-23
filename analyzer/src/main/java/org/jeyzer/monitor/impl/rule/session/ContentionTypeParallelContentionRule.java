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
import org.jeyzer.monitor.impl.event.session.ContentionTypeParallelContentionEvent;

public class ContentionTypeParallelContentionRule extends MonitorSessionRule implements CustomSessionProvider{
	
	public static final String RULE_NAME = "Contention type parallel contention";
	
	public static final String RULE_CONDITION_DESCRIPTION = "Contention type name regex (pattern) is seen in more than (value) parallel threads.";
	public static final String RULE_NARRATIVE = "The " + RULE_NAME + " rule permits to detect parallel contentions of same nature. "
			+ "It is useful for example to detect database or disk access contentions. "
			+ "It applies of course on active threads.";
	
	// can be null
	private String contentionTypeName;
	
	public ContentionTypeParallelContentionRule(ConfigMonitorRule def) throws JzrInitializationException {
		super(def, RULE_NAME + " : " + def.getExtraInfo(), RULE_CONDITION_DESCRIPTION);
		contentionTypeName = def.getExtraInfo();
	}

	@Override
	public boolean matchCustomParameters(ThreadDump dump, ConfigMonitorThreshold thresholdConfig) {
		int count = getContentionTypeMatchCount(dump, thresholdConfig.getPattern());
		return count >= thresholdConfig.getValue();
	}

	private int getContentionTypeMatchCount(ThreadDump dump, Pattern pattern) {
		int count = 0;
		
		for (ThreadStack stack : dump.getWorkingThreads()){
			for (String contentionType : stack.getContentionTypeTags()){
				Matcher matcher = pattern.matcher(contentionType);
				if (matcher.find()){
					count++;
					break;
				}
			}
		}
		return count;
	}

	@Override
	public MonitorSessionEvent createSessionEvent(MonitorEventInfo info, ThreadDump td) {
		return new ContentionTypeParallelContentionEvent(info, td, contentionTypeName);
	}

	@Override
	public boolean isAdvancedMonitoringBased() {
		return false;
	}

	@Override
	protected List<String> getSupportedThresholdTypes() {
		return Arrays.asList(THRESHOLD_SESSION_CUSTOM, THRESHOLD_GLOBAL_CUSTOM);
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
