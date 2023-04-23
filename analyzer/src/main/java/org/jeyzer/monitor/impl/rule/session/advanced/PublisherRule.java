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



import static org.jeyzer.monitor.config.engine.ConfigMonitorThreshold.THRESHOLD_SESSION_PUBLISHER;

import java.util.ArrayList;




import java.util.Arrays;
import java.util.List;

import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.analyzer.data.event.JzrPublisherEvent;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.monitor.config.engine.ConfigMonitorRule;
import org.jeyzer.monitor.engine.event.MonitorSessionEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;
import org.jeyzer.monitor.engine.event.info.SubLevel;
import org.jeyzer.monitor.engine.rule.MonitorPublisherRule;
import org.jeyzer.monitor.engine.rule.MonitorSessionRule;
import org.jeyzer.monitor.engine.rule.condition.applicative.PublisherEventProvider;
import org.jeyzer.monitor.impl.event.session.advanced.PublisherEvent;

public class PublisherRule extends MonitorSessionRule implements MonitorPublisherRule, PublisherEventProvider {

	public static final String RULE_NAME = "Jeyzer publisher";
	
	public static final String RULE_CONDITION_DESCRIPTION = "Publisher session event fired.";
	public static final String RULE_NARRATIVE = "The " + RULE_NAME + " rule holds events published by the Jeyzer publisher.";
	
	private List<JzrPublisherEvent> events = new ArrayList<>();
	
	public PublisherRule(ConfigMonitorRule def) throws JzrInitializationException {
		super(def, RULE_NAME + ":" + def.getRef(), RULE_CONDITION_DESCRIPTION);
	}

	@Override
	public MonitorSessionEvent createSessionEvent(MonitorEventInfo info, ThreadDump td) {
		return new PublisherEvent(info);
	}

	@Override
	public boolean isAdvancedMonitoringBased() {
		return true;
	}
	
	@Override
	protected List<String> getSupportedThresholdTypes(){
		return Arrays.asList(THRESHOLD_SESSION_PUBLISHER);
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
	public void addEvent(JzrPublisherEvent event) {
		events.add(event);
	}

	@Override
	public List<JzrPublisherEvent> getPublisherEvents() {
		return events;
	}
}
