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

import org.jeyzer.analyzer.data.event.ExternalEvent;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.monitor.config.engine.ConfigMonitorRule;
import org.jeyzer.monitor.config.engine.ConfigMonitorTaskThreshold;
import org.jeyzer.monitor.config.engine.ConfigMonitorThreshold;
import org.jeyzer.monitor.engine.rule.MonitorApplicativeRule;
import org.jeyzer.monitor.engine.rule.MonitorSessionRule;
import org.jeyzer.monitor.engine.rule.MonitorSystemRule;
import org.jeyzer.monitor.engine.rule.MonitorTaskRule;
import org.jeyzer.monitor.engine.rule.threshold.action.ActionApplicativeThreshold;
import org.jeyzer.monitor.engine.rule.threshold.session.SessionApplicativeThreshold;
import org.jeyzer.monitor.engine.rule.threshold.system.SystemApplicativeThreshold;
import org.jeyzer.monitor.impl.rule.session.advanced.ApplicativeSessionRule;
import org.jeyzer.monitor.impl.rule.system.ApplicativeSystemRule;
import org.jeyzer.monitor.impl.rule.task.advanced.ApplicativeTaskRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MonitorExternalRuleBuilder {
	
	private static final Logger logger = LoggerFactory.getLogger(MonitorExternalRuleBuilder.class);
	
	private static final String SESSION_PREFIX = "Session@";
	
	private final List<MonitorTaskRule> taskRules = new ArrayList<>();
	private final List<MonitorSessionRule> sessionRules = new ArrayList<>();
	private final List<MonitorSystemRule> systemRules = new ArrayList<>();
	
	public final void createExternalRules(List<ExternalEvent> extEvents) {
		Map<String, MonitorApplicativeRule> applicativeRules = new HashMap<>();
		// Backup session rules for applicative task events that will not match any action
		Map<String, ApplicativeSessionRule> applicativeSessionBackRules = new HashMap<>();
		
		for (ExternalEvent extEvent : extEvents) {
			MonitorApplicativeRule rule = applicativeRules.get(extEvent.getCode());
			
			if (rule == null) {
				try {
					rule = createApplicativeRule(extEvent);
				} catch (JzrInitializationException ex) {
					logger.warn("Cannnot create applicative rule. Event " + extEvent.getId() + " is invalid. Error message is : " + ex.getMessage());
					continue;// invalid rule, ignore
				}
				if (rule == null)
					continue; // invalid rule, ignore
				
				applicativeRules.put(extEvent.getCode(), rule);
				
				if (extEvent.isActionEvent()){
					ApplicativeSessionRule sessionRule;
					
					try {
						// get the action event process at session level in case it doesn't match any action
						sessionRule = createApplicativeSessionRule(extEvent);
					} catch (JzrInitializationException ex) {
						logger.warn("Cannnot create applicative session rule. Event " + extEvent.getId() + " is invalid. Error message is " + ex.getMessage());
						continue;// invalid rule, ignore
					}
					applicativeSessionBackRules.put(SESSION_PREFIX + extEvent.getCode(), sessionRule);
				}
			}
			
			rule.addEvent(extEvent);
			
			if (extEvent.isActionEvent()) {
				// set the session backup
				ApplicativeSessionRule sessionRule = applicativeSessionBackRules.get(SESSION_PREFIX + extEvent.getCode());
				if (sessionRule!= null)
					sessionRule.addEvent(extEvent);
			}
		}
	}

	public final List<MonitorTaskRule> getMonitorApplicativeTaskRules() {
		return taskRules;
	}

	public final List<MonitorSessionRule> getMonitorApplicativeSessionRules() {
		return sessionRules;
	}

	public final List<MonitorSystemRule> getMonitorApplicativeSystemRules() {
		return systemRules;
	}

	private ApplicativeSessionRule createApplicativeSessionRule(ExternalEvent extEvent) throws JzrInitializationException {
		String group = buildGroupName(extEvent);

		ConfigMonitorRule def = new ConfigMonitorRule(
				new ConfigMonitorThreshold(
						SessionApplicativeThreshold.THRESHOLD_NAME, // force session
						"", // we will use the rule code only
						extEvent.getLevel(),
						extEvent.getSublevel(),
						extEvent.getMessage(),
						extEvent.getTrust()
						),
				extEvent.getSource(),
				group,
				extEvent.getName(),
				extEvent.getCode(),
				extEvent.getNarrative(),
				extEvent.getTicket()
				);
				
		ApplicativeSessionRule rule= new ApplicativeSessionRule(def);
		sessionRules.add((MonitorSessionRule)rule);
		
		return rule;
	}

	private MonitorApplicativeRule createApplicativeRule(ExternalEvent extEvent) throws JzrInitializationException{
		MonitorApplicativeRule rule = null;
		String group = buildGroupName(extEvent);

		ConfigMonitorRule def = new ConfigMonitorRule(
				createConfigMonitorThreshold(extEvent),
				extEvent.getSource(),
				group,
				extEvent.getName(),
				extEvent.getCode(),
				extEvent.getNarrative(),
				extEvent.getTicket()
				);
		
		if (ExternalEvent.SCOPE_ACTION.equals(extEvent.getScope())){
			rule = new ApplicativeTaskRule(def);
			taskRules.add((MonitorTaskRule)rule);
		}
		else if (ExternalEvent.SCOPE_GLOBAL.equals(extEvent.getScope())){
			rule = new ApplicativeSessionRule(def);
			sessionRules.add((MonitorSessionRule)rule);
		}			
		else if (ExternalEvent.SCOPE_SYSTEM.equals(extEvent.getScope())){
			rule = new ApplicativeSystemRule(def);
			systemRules.add((MonitorSystemRule)rule);
		}
		else {
			logger.warn("Cannnot create applicative rule. Event scope is invalid for event " + extEvent.getId());			
		}
		
		return rule;
	}

	private ConfigMonitorThreshold createConfigMonitorThreshold(ExternalEvent extEvent) {
		switch(extEvent.getScope()) {
		case ExternalEvent.SCOPE_ACTION:
			return new ConfigMonitorTaskThreshold(
							ActionApplicativeThreshold.THRESHOLD_NAME,
							"", // we will use the rule code only
							extEvent.getLevel(),
							extEvent.getSublevel(),
							extEvent.getMessage(),
							extEvent.getTrust()
							);
		case ExternalEvent.SCOPE_GLOBAL:
			return new ConfigMonitorThreshold(
					SessionApplicativeThreshold.THRESHOLD_NAME,
					"", // we will use the rule code only
					extEvent.getLevel(),
					extEvent.getSublevel(),
					extEvent.getMessage(),
					extEvent.getTrust()
					);
		case ExternalEvent.SCOPE_SYSTEM:
			return new ConfigMonitorThreshold(
					SystemApplicativeThreshold.THRESHOLD_NAME,
					"", // we will use the rule code only
					extEvent.getLevel(),
					extEvent.getSublevel(),
					extEvent.getMessage(),
					extEvent.getTrust()
					);		
		default:
			logger.warn("Cannnot create applicative rule config. Event scope is invalid for event " + extEvent.getId());
		}
		return null;
	}

	private String buildGroupName(ExternalEvent extEvent) {
		// service and type are optional
		String group = extEvent.getService();
		
		if (group.isEmpty() && extEvent.getType().isEmpty())
			return "not available";

		return group.isEmpty() ? 
				extEvent.getType() : 
				extEvent.getType().isEmpty()? 
						group : group + "/" + extEvent.getType();
	}
}
