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



import static org.jeyzer.monitor.config.engine.ConfigMonitorThreshold.THRESHOLD_GLOBAL_VALUE;
import static org.jeyzer.monitor.config.engine.ConfigMonitorThreshold.THRESHOLD_SESSION_VALUE;





import java.util.Arrays;
import java.util.List;

import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.monitor.config.engine.ConfigMonitorRule;
import org.jeyzer.monitor.engine.event.MonitorSessionEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;
import org.jeyzer.monitor.engine.event.info.SubLevel;
import org.jeyzer.monitor.engine.rule.MonitorSessionRule;
import org.jeyzer.monitor.engine.rule.condition.session.ValueSessionProvider;
import org.jeyzer.monitor.impl.event.session.advanced.GarbageCollectionTimeEvent;

public class GarbageCollectionTimeRule extends MonitorSessionRule implements ValueSessionProvider {

	public static final String RULE_NAME = "Excessive GC time";
	
	public static final String RULE_CONDITION_DESCRIPTION = "Global GC execution time in ms is greater or equal to (value).";
	public static final String RULE_NARRATIVE = "The " + RULE_NAME + " rule permits to detect high global GC usage. "
			+ "It reflects usually high activity peaks with large memory allocations. "
			+ "Upon high GC times and depending on the GC strategy, Java virtual machines can suspend the applicative threads and therefore introduce secondary undesired effects like timeouts. "
			+ "In such case, it is recommended to tune the GC settings to reduce the GC time.";

	public GarbageCollectionTimeRule(ConfigMonitorRule def) throws JzrInitializationException {
		super(def, RULE_NAME, RULE_CONDITION_DESCRIPTION);
	}

	@Override
	public boolean matchValue(ThreadDump dump, long value) {
		return dump.getGarbageCollection().getGcTime() >= value;
	}

	@Override
	public MonitorSessionEvent createSessionEvent(MonitorEventInfo info, ThreadDump td) {
		return new GarbageCollectionTimeEvent(info, td);
	}

	@Override
	public boolean isAdvancedMonitoringBased() {
		return true;
	}

	@Override
	protected List<String> getSupportedThresholdTypes(){
		return Arrays.asList(THRESHOLD_SESSION_VALUE, THRESHOLD_GLOBAL_VALUE);
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
