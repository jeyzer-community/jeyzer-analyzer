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



import static org.jeyzer.monitor.config.engine.ConfigMonitorThreshold.THRESHOLD_GLOBAL_SIGNAL;
import static org.jeyzer.monitor.config.engine.ConfigMonitorThreshold.THRESHOLD_SESSION_SIGNAL;





import java.util.Arrays;
import java.util.List;

import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.monitor.config.engine.ConfigMonitorRule;
import org.jeyzer.monitor.engine.event.MonitorSessionEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;
import org.jeyzer.monitor.engine.event.info.SubLevel;
import org.jeyzer.monitor.engine.rule.MonitorSessionRule;
import org.jeyzer.monitor.engine.rule.condition.session.SignalSessionProvider;
import org.jeyzer.monitor.impl.event.session.SuspendedThreadsEvent;

public class SuspendedThreadsRule extends MonitorSessionRule implements SignalSessionProvider{

	public static final String RULE_NAME = "Suspended threads";

	public static final String RULE_CONDITION_DESCRIPTION = "Thread suspension situation is detected.";
	public static final String RULE_NARRATIVE = "The " + RULE_NAME + " rule permits to detect suspended threads. "
			+ "Suspended threads are threads blocked on a debug breakpoint. "
			+ "It means that someone (like a developer) has connected a debugger on the application : you have to find him."
			+ "Debug access must be strictly limited to NON-production environments as it can halt the process and business activity."
			+ "Production environments should not have their debug port open.";
	
	public SuspendedThreadsRule(ConfigMonitorRule def) throws JzrInitializationException {
		super(def, RULE_NAME, RULE_CONDITION_DESCRIPTION);
	}

	@Override
	public boolean matchSignal(ThreadDump dump) {
		return dump.hasSuspendedThreads() > 0;
	}

	@Override
	public MonitorSessionEvent createSessionEvent(MonitorEventInfo info, ThreadDump td) {
		return new SuspendedThreadsEvent(info, td);
	}

	@Override
	public boolean isAdvancedMonitoringBased() {
		return false;
	}
	
	@Override
	protected List<String> getSupportedThresholdTypes(){
		return Arrays.asList(THRESHOLD_SESSION_SIGNAL, THRESHOLD_GLOBAL_SIGNAL);
	}
	
	@Override
	public SubLevel getDefaultSubLevel() {
		return SubLevel.DEFAULT_VERY_HIGH;
	}
	
	@Override
	public String getDefaultNarrative() {
		return RULE_NARRATIVE;
	}
}
