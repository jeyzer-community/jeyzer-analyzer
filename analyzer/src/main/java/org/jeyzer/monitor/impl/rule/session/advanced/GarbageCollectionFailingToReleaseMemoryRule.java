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



import static org.jeyzer.monitor.config.engine.ConfigMonitorThreshold.THRESHOLD_GLOBAL_CUSTOM;
import static org.jeyzer.monitor.config.engine.ConfigMonitorThreshold.THRESHOLD_SESSION_CUSTOM;




import java.util.Arrays;
import java.util.List;

import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.analyzer.data.gc.GarbageCollection;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.monitor.config.engine.ConfigMonitorRule;
import org.jeyzer.monitor.config.engine.ConfigMonitorThreshold;
import org.jeyzer.monitor.engine.event.MonitorSessionEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;
import org.jeyzer.monitor.engine.event.info.SubLevel;
import org.jeyzer.monitor.engine.rule.MonitorSessionRule;
import org.jeyzer.monitor.engine.rule.condition.session.CustomSessionProvider;
import org.jeyzer.monitor.impl.event.session.advanced.GarbageCollectionFailingToReleaseMemoryEvent;

public class GarbageCollectionFailingToReleaseMemoryRule extends MonitorSessionRule implements CustomSessionProvider {

	public static final String RULE_NAME = "GC failing to release memory";
	
	public static final String USED_MEMORY_PARAM_NAME = "used_memory";
	public static final String RELEASED_MEMORY_PARAM_NAME = "released_memory";
	public static final String GC_TIME_PARAM_NAME = "gc_time";

	public static final String RULE_CONDITION_DESCRIPTION = "Global garbage collection time is greater than ("+ GC_TIME_PARAM_NAME + ")\n"
			+ "Released memory % in old memory zone is lower than ("+ RELEASED_MEMORY_PARAM_NAME + ")\n"
			+ "Used memory % in old memory zone is greater than ("+ USED_MEMORY_PARAM_NAME + ").";
	public static final String RULE_NARRATIVE = "The " + RULE_NAME + " rule permits to detect out of memory situations. "
			+ "Out of memory is considered happening once the GC time is getting constantly high along with old memory zone fully occupied with long term live objects (meaning very low memory release). "
			+ "This situation requires usually an applicative restart and must get immediate R&D attention. "
			+ "It is recommended to perform a heap memory dump before restart.";
	
	public GarbageCollectionFailingToReleaseMemoryRule(ConfigMonitorRule def) throws JzrInitializationException {
		super(def, RULE_NAME, RULE_CONDITION_DESCRIPTION);
	}

	@Override
	public MonitorSessionEvent createSessionEvent(MonitorEventInfo info, ThreadDump td) {
		return new GarbageCollectionFailingToReleaseMemoryEvent(info, td);
	}

	@Override
	public boolean isAdvancedMonitoringBased() {
		return true;
	}
	
	@Override
	protected List<String> getSupportedThresholdTypes(){
		return Arrays.asList(THRESHOLD_SESSION_CUSTOM, THRESHOLD_GLOBAL_CUSTOM);
	}

	@Override
	public boolean matchCustomParameters(ThreadDump dump, ConfigMonitorThreshold thresholdConfig) {
		long usedMemoryThreshold = getValue(thresholdConfig, USED_MEMORY_PARAM_NAME);
		long releasedMemoryThreshold = getValue(thresholdConfig, RELEASED_MEMORY_PARAM_NAME);
		long gcTimeThreshold = getValue(thresholdConfig, GC_TIME_PARAM_NAME);
		
		GarbageCollection garbageCollection = dump.getGarbageCollection();
		
		if (garbageCollection.getOldGarbageCollectorInfo() == null)
			return false;
		
		return garbageCollection.getGcTime() > gcTimeThreshold
				&& garbageCollection.getOldGarbageCollectorInfo().getReleasedMemoryPercent() < releasedMemoryThreshold
				&& garbageCollection.getOldGarbageCollectorInfo().getUsedMemoryAfterPercent() > usedMemoryThreshold;
	}

	private long getValue(ConfigMonitorThreshold thresholdConfig, String paramName) {
		String paramValue = thresholdConfig.getCustomParameter(paramName);
		if (paramValue == null)
			return -1;
		
		try {
			return Long.parseLong(paramValue);
		} catch (NumberFormatException e) {
			return -1;
		}
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
