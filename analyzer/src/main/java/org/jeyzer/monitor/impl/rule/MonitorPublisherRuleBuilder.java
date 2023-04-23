package org.jeyzer.monitor.impl.rule;

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




import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jeyzer.analyzer.data.event.JzrPublisherEvent;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.monitor.config.engine.ConfigMonitorRule;
import org.jeyzer.monitor.config.engine.ConfigMonitorThreshold;
import org.jeyzer.monitor.engine.rule.MonitorSessionRule;
import org.jeyzer.monitor.engine.rule.threshold.session.SessionPublisherThreshold;
import org.jeyzer.monitor.impl.rule.session.advanced.PublisherRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MonitorPublisherRuleBuilder {
	
	private static final Logger logger = LoggerFactory.getLogger(MonitorPublisherRuleBuilder.class);
	
	private static final String PUBLISHER_GROUP = "Jeyzer Publisher";
	
	public final List<MonitorSessionRule> createPublisherRules(List<JzrPublisherEvent> pubEvents) {
		Map<String, PublisherRule> publisherRules = new HashMap<>();
		
		for (JzrPublisherEvent pubEvent : pubEvents) {
			PublisherRule rule = publisherRules.get(pubEvent.getCode());
			
			if (rule == null) {
				try {
					rule = createPublisherRule(pubEvent);
				} catch (JzrInitializationException ex) {
					logger.warn("Cannnot create publisher rule. Event " + pubEvent.getCode() + " is invalid. Error message is : " + ex.getMessage());
					continue;// invalid rule, ignore
				}
				publisherRules.put(pubEvent.getCode(), rule);
			}
			
			rule.addEvent(pubEvent);
		}
		
		return new ArrayList<MonitorSessionRule>(publisherRules.values());
	}

	private PublisherRule createPublisherRule(JzrPublisherEvent pubEvent) throws JzrInitializationException{
		ConfigMonitorThreshold threshold = new ConfigMonitorThreshold(
				SessionPublisherThreshold.THRESHOLD_NAME,
				"", // we will use the rule code only
				pubEvent.getLevel(),
				pubEvent.getSublevel(),
				pubEvent.getName(),
				pubEvent.getTrust()
				);
		
		ConfigMonitorRule def = new ConfigMonitorRule(
				threshold,
				pubEvent.getSource(),
				PUBLISHER_GROUP,
				pubEvent.getName(),
				pubEvent.getCode(),
				PublisherRule.RULE_NARRATIVE,
				null // ticket does not apply
				);
		
		return new PublisherRule(def);
	}
}
