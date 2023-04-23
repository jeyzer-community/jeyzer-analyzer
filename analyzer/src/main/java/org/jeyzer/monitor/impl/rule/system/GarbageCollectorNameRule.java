package org.jeyzer.monitor.impl.rule.system;

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


import static org.jeyzer.monitor.config.engine.ConfigMonitorThreshold.THRESHOLD_SYSTEM_PATTERN;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.analyzer.session.JzrSession;
import org.jeyzer.monitor.config.engine.ConfigMonitorRule;
import org.jeyzer.monitor.engine.event.MonitorSystemEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;
import org.jeyzer.monitor.engine.event.info.SubLevel;
import org.jeyzer.monitor.engine.rule.MonitorSystemRule;
import org.jeyzer.monitor.engine.rule.condition.system.PatternSystemProvider;
import org.jeyzer.monitor.impl.event.system.GarbageCollectorNameEvent;

public class GarbageCollectorNameRule extends MonitorSystemRule implements PatternSystemProvider{

	public static final String RULE_NAME = "Garbage collector name";
	
	public static final String RULE_NARRATIVE = "The " + RULE_NAME + " rule permits to detect the usage of a specific garbage collector (old or young)."
			+ "It is useful to detect deprecated garbage collectors or garbage collectors that could have a performance impact on the monitored application.";
	
	private String extraInfo;
	
	public GarbageCollectorNameRule(ConfigMonitorRule def) throws JzrInitializationException {
		super(def, RULE_NAME + " : " + def.getExtraInfo(),
				"Garbage collector name contains the (pattern) regex.");
		this.extraInfo = def.getExtraInfo();
	}

	@Override
	public boolean isAdvancedMonitoringBased() {
		return true;
	}
	
	@Override
	protected List<String> getSupportedThresholdTypes(){
		return Arrays.asList(THRESHOLD_SYSTEM_PATTERN);
	}

	@Override
	public boolean matchPattern(JzrSession session, Pattern pattern) {
		for (ThreadDump td : session.getDumps()) {
			if (td.getGarbageCollection() == null)
				continue;

			// check old
			if (td.getGarbageCollection().getOldGarbageCollectorInfo() != null) {
				String name = td.getGarbageCollection().getOldGarbageCollectorInfo().getName();
				if (pattern.matcher(name).find())
					return true;
			}
			
			// check young
			if (td.getGarbageCollection().getYoungGarbageCollectorInfo() != null) {
				String name = td.getGarbageCollection().getYoungGarbageCollectorInfo().getName();
				if (pattern.matcher(name).find())
					return true;
			}
		}
		
		return false;
	}

	@Override
	public MonitorSystemEvent createSystemEvent(MonitorEventInfo info) {
		return new GarbageCollectorNameEvent(this.extraInfo, info);
	}
	
	@Override
	public SubLevel getDefaultSubLevel() {
		return SubLevel.DEFAULT_VERY_LOW;
	}
	
	@Override
	public String getDefaultNarrative() {
		return RULE_NARRATIVE;
	}
}
