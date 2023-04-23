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
import org.jeyzer.monitor.impl.event.session.DeadlockEvent;

public class DeadlockRule extends MonitorSessionRule implements SignalSessionProvider{

	public static final String RULE_NAME = "Deadlock";

	public static final String RULE_CONDITION_DESCRIPTION = "Deadlock situation is detected.";
	public static final String RULE_NARRATIVE = "The " + RULE_NAME + " rule permits to detect thread deadlocks. "
			+ "It is either based on deadlock information provided by the Java virtual machine or deduced by the Jeyzer Analyzer by looking at the lock dependency cycles between threads. "
			+ "Deadlock occurence requires usually an applicative restart and must get immediate R&D attention.";
	
	public DeadlockRule(ConfigMonitorRule def) throws JzrInitializationException {
		super(def, RULE_NAME, RULE_CONDITION_DESCRIPTION);
	}

	@Override
	public boolean matchSignal(ThreadDump dump) {
		return dump.hasDeadLock() > 0;
	}

	@Override
	public MonitorSessionEvent createSessionEvent(MonitorEventInfo info, ThreadDump td) {
		return new DeadlockEvent(info, td);
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
