package org.jeyzer.monitor.impl.rule.task.advanced;

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



import static org.jeyzer.monitor.config.engine.ConfigMonitorThreshold.THRESHOLD_ACTION_APPLICATIVE;

import java.util.ArrayList;




import java.util.Arrays;
import java.util.List;

import org.jeyzer.analyzer.data.ThreadAction;
import org.jeyzer.analyzer.data.event.ExternalEvent;
import org.jeyzer.analyzer.data.stack.ThreadStack.ThreadStackHandler;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.monitor.config.engine.ConfigMonitorRule;
import org.jeyzer.monitor.engine.event.MonitorTaskEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;
import org.jeyzer.monitor.engine.event.info.SubLevel;
import org.jeyzer.monitor.engine.rule.MonitorApplicativeRule;
import org.jeyzer.monitor.engine.rule.MonitorTaskRule;
import org.jeyzer.monitor.engine.rule.condition.applicative.ApplicativeEventProvider;
import org.jeyzer.monitor.impl.event.task.advanced.ApplicativeTaskEvent;

public class ApplicativeTaskRule extends MonitorTaskRule implements ApplicativeEventProvider, MonitorApplicativeRule {

	public static final String RULE_NAME = "Applicative task";
	
	public static final String RULE_CONDITION_DESCRIPTION = "Application thread event fired.";
	public static final String RULE_NARRATIVE = "The " + RULE_NAME + " rule holds action level events published by the monitored application.";

	private List<ExternalEvent> events = new ArrayList<>();
	
	public ApplicativeTaskRule(ConfigMonitorRule def) throws JzrInitializationException {
		super(def, RULE_NAME + ":" + def.getRef(), RULE_CONDITION_DESCRIPTION);
	}

	@Override
	public MonitorTaskEvent createTaskEvent(MonitorEventInfo info, ThreadAction action, ThreadStackHandler stack) {
		return new ApplicativeTaskEvent(info, action, stack);
	}

	@Override
	public boolean isAdvancedMonitoringBased() {
		return true;
	}

	@Override
	protected List<String> getSupportedThresholdTypes(){
		return Arrays.asList(THRESHOLD_ACTION_APPLICATIVE);
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
