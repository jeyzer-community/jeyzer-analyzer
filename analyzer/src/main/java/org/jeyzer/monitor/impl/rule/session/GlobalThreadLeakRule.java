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




import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.monitor.config.engine.ConfigMonitorRule;
import org.jeyzer.monitor.config.engine.ConfigMonitorThreshold;
import org.jeyzer.monitor.engine.event.MonitorSessionEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;
import org.jeyzer.monitor.engine.event.info.SubLevel;
import org.jeyzer.monitor.impl.event.session.GlobalThreadLeakEvent;

public class GlobalThreadLeakRule  extends AbstractThreadLeakRule {

	public static final String RULE_NAME = "Global thread leak";
	
	public static final String RULE_CONDITION_DESCRIPTION = "Global number of threads is increasing constantly, by at least ("+ DELTA_Y_PARAM_NAME + ") threads every ("+ DELTA_X_PARAM_NAME + ") recording snapshots.\n Number of threads must be higher than (value).";
	public static final String RULE_NARRATIVE = "The " + RULE_NAME + " rule permits to detect a thread leak at process level. "
			+ "Maximum number of threads in a process is operating system dependant, usually around X thousands of threads. "
			+ "Breaking the limit will result in out of memory errors related to file descriptors. "
			+ "Thread leak occurence requires usually an applicative restart and must get immediate R&D attention."
			+ "It is recommended to use this rule along with the rule " + GlobalThreadLimitRule.RULE_NAME;
	
	public GlobalThreadLeakRule(ConfigMonitorRule def) throws JzrInitializationException {
		super(def, RULE_NAME, RULE_CONDITION_DESCRIPTION);
	}
	
	@Override
	public int getThreadsCount(ThreadDump dump, ConfigMonitorThreshold thresholdConfig) {
		return dump.getThreads().size();
	}

	@Override
	public MonitorSessionEvent createSessionEvent(MonitorEventInfo info, ThreadDump td) {
		return new GlobalThreadLeakEvent(info, td);
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
