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



import static org.jeyzer.monitor.config.engine.ConfigMonitorThreshold.THRESHOLD_SYSTEM_APPLICATIVE;

import java.util.ArrayList;




import java.util.Arrays;
import java.util.List;

import org.jeyzer.analyzer.data.event.ExternalEvent;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.monitor.config.engine.ConfigMonitorRule;
import org.jeyzer.monitor.engine.event.MonitorSystemEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;
import org.jeyzer.monitor.engine.event.info.SubLevel;
import org.jeyzer.monitor.engine.rule.MonitorApplicativeRule;
import org.jeyzer.monitor.engine.rule.MonitorSystemRule;
import org.jeyzer.monitor.engine.rule.condition.applicative.ApplicativeEventProvider;
import org.jeyzer.monitor.impl.event.system.ApplicativeSystemEvent;

public class ApplicativeSystemRule extends MonitorSystemRule implements ApplicativeEventProvider, MonitorApplicativeRule{

	public static final String RULE_NAME = "Applicative system";
	
	public static final String RULE_CONDITION_DESCRIPTION = "Application system event fired.";
	public static final String RULE_NARRATIVE = "The " + RULE_NAME + " rule holds system level events published by the monitored application.";
	
	private List<ExternalEvent> events = new ArrayList<>();
	
	public ApplicativeSystemRule(ConfigMonitorRule def) throws JzrInitializationException {
		super(def, RULE_NAME + ":" + def.getRef(), RULE_CONDITION_DESCRIPTION);
	}

	@Override
	public boolean isAdvancedMonitoringBased() {
		return true;
	}

	@Override
	protected List<String> getSupportedThresholdTypes(){
		return Arrays.asList(THRESHOLD_SYSTEM_APPLICATIVE);
	}

	@Override
	public MonitorSystemEvent createSystemEvent(MonitorEventInfo info) {
		return new ApplicativeSystemEvent(info);
	}
	
	@Override
	public SubLevel getDefaultSubLevel() {
		return SubLevel.DEFAULT_MEDIUM;
	}
	
	@Override
	public String getDefaultNarrative() {
		return RULE_NARRATIVE;
	}

	@Override
	public void addEvent(ExternalEvent event) {
		events.add(event);
	}

	@Override
	public List<ExternalEvent> getApplicativeEvents() {
		return events;
	}
}
