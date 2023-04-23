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






import java.util.ArrayList;
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
import org.jeyzer.monitor.impl.event.session.NamedThreadLeakEvent;


public class NamedThreadLeakRule  extends AbstractThreadLeakRule {

	public static final String RULE_NAME = "Named thread leak";
	
	public static final String RULE_CONDITION_DESCRIPTION = "Global number of (pattern) named threads is increasing constantly, by at least ("+ DELTA_Y_PARAM_NAME + ") thread every ("+ DELTA_X_PARAM_NAME + ") thread dumps.\n"
			+ "Named pattern is regex based. Includes active and inactive threads.";
	public static final String RULE_NARRATIVE = "The " + RULE_NAME + " rule permits to detect a thread leak on a particular set of threads, which can be for example a thread pool.";
	
	// can be null
	private String threadName;
	
	public NamedThreadLeakRule(ConfigMonitorRule def) throws JzrInitializationException {
		super(def, RULE_NAME + " : " + def.getExtraInfo(), RULE_CONDITION_DESCRIPTION);
		threadName = def.getExtraInfo();
	}
	
	@Override
	public int getThreadsCount(ThreadDump dump, ConfigMonitorThreshold thresholdConfig) {
		List<ThreadStack> namedThreads = new ArrayList<>();
		Pattern pattern = thresholdConfig.getPattern();
		
		for (ThreadStack ts : dump.getThreads()){
			Matcher matcher = pattern.matcher(ts.getName());
			if (matcher.find())
				namedThreads.add(ts);
		}
		
		return namedThreads.size();
	}

	@Override
	public MonitorSessionEvent createSessionEvent(MonitorEventInfo info, ThreadDump td) {
		return new NamedThreadLeakEvent(info, td, threadName);
	}
	
	@Override
	public SubLevel getDefaultSubLevel() {
		return SubLevel.DEFAULT_HIGH;
	}
	
	@Override
	public String getDefaultNarrative() {
		return RULE_NARRATIVE;
	}
}
