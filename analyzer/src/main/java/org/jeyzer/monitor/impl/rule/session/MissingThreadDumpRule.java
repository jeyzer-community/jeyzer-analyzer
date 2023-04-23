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



import static org.jeyzer.monitor.config.engine.ConfigMonitorThreshold.THRESHOLD_GLOBAL_DIFF;
import static org.jeyzer.monitor.config.engine.ConfigMonitorThreshold.THRESHOLD_SESSION_DIFF;





import java.util.Arrays;
import java.util.List;

import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.monitor.config.engine.ConfigMonitorRule;
import org.jeyzer.monitor.engine.event.MonitorSessionEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;
import org.jeyzer.monitor.engine.event.info.SubLevel;
import org.jeyzer.monitor.engine.rule.MonitorSessionRule;
import org.jeyzer.monitor.engine.rule.condition.session.DiffSessionProvider;
import org.jeyzer.monitor.impl.event.session.MissingThreadDumpEvent;


public class MissingThreadDumpRule extends MonitorSessionRule implements DiffSessionProvider{
	
	public static final String RULE_NAME = "Missing thread dump";
	
	public static final String RULE_CONDITION_DESCRIPTION = "Missing thread dump is detected between 2 thread dumps. Restart is ignored.";
	public static final String RULE_NARRATIVE = "The " + RULE_NAME + " rule permits to detect missing recording snapshots. "
			+ "It usually means that the monitored application is either not started (long hiatus) or under very high load in which case a sequence of small hiatus should be observed.";
	
	public MissingThreadDumpRule(ConfigMonitorRule def) throws JzrInitializationException {
		super(def, RULE_NAME, RULE_CONDITION_DESCRIPTION);
	}

	@Override
	public boolean matchSignal(ThreadDump dump, ThreadDump nextDump, int period) {
		return nextDump.hasHiatusBefore();
	}

	@Override
	public MonitorSessionEvent createSessionEvent(MonitorEventInfo info, ThreadDump td) {
		return new MissingThreadDumpEvent(info);
	}

	@Override
	public boolean isAdvancedMonitoringBased() {
		return false;
	}
	
	@Override
	protected List<String> getSupportedThresholdTypes(){
		return Arrays.asList(THRESHOLD_SESSION_DIFF, THRESHOLD_GLOBAL_DIFF);
	}
	
	@Override
	public SubLevel getDefaultSubLevel() {
		return SubLevel.DEFAULT_MEDIUM;
	}
	
	@Override
	public String getDefaultNarrative() {
		return RULE_NARRATIVE;
	}
}
