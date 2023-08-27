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



import static org.jeyzer.monitor.config.engine.ConfigMonitorThreshold.THRESHOLD_SYSTEM_SIGNAL;

import java.util.Arrays;
import java.util.List;

import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.analyzer.session.JzrSession;
import org.jeyzer.monitor.config.engine.ConfigMonitorRule;
import org.jeyzer.monitor.engine.event.MonitorSystemEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;
import org.jeyzer.monitor.engine.event.info.SubLevel;
import org.jeyzer.monitor.engine.rule.MonitorSystemRule;
import org.jeyzer.monitor.engine.rule.condition.system.SignalSystemProvider;
import org.jeyzer.monitor.impl.event.system.VirtualThreadPresenceEvent;


public class VirtualThreadPresenceRule extends MonitorSystemRule implements SignalSystemProvider{

	public static final String RULE_NAME = "Virtual thread presence";
	
	public static final String RULE_NARRATIVE = "The " + RULE_NAME + " rule permits to detect the usage of virtual threads.\n"
			+ " Virtual threads have been introduced with Java 17 and are officially released since Java 21.\n"
			+ " Because it introduces new concepts and push further the performance limits, it must be monitored closely.";
	
	public VirtualThreadPresenceRule(ConfigMonitorRule def) throws JzrInitializationException {
		super(def, RULE_NAME, 
				"Virtual thread usage is detected.");
	}

	@Override	
	public boolean isAdvancedMonitoringBased() {
		return false;
	}
	
	@Override
	protected List<String> getSupportedThresholdTypes(){
		return Arrays.asList(THRESHOLD_SYSTEM_SIGNAL);
	}

	@Override
	public boolean matchSignal(JzrSession session) {
		return session.hasVirtualThreadPresence();
	}

	@Override
	public MonitorSystemEvent createSystemEvent(MonitorEventInfo info) {
		return new VirtualThreadPresenceEvent("Virtual threads detected", info);
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
