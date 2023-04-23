package org.jeyzer.monitor.config.applicative;

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




import org.jeyzer.analyzer.error.JzrInitializationException;
import org.w3c.dom.Element;

public class ConfigMonitorApplicativeRuleManager {

	private static final String JZRM_TASK_APP_RULES = "task_applicative_rules";
	private static final String JZRM_SESSION_APP_RULES = "session_applicative_rules";
	private static final String JZRM_SYSTEM_APP_RULES = "system_applicative_rules";
	
	private MonitorApplicativeRuleFilter taskRuleFilter;
	private MonitorApplicativeRuleFilter sessionRuleFilter;
	private MonitorApplicativeRuleFilter systemRuleFilter;
	
	public ConfigMonitorApplicativeRuleManager(Element rulesNode) throws JzrInitializationException {
		this.taskRuleFilter = new MonitorApplicativeRuleFilter(rulesNode, JZRM_TASK_APP_RULES);
		this.sessionRuleFilter = new MonitorApplicativeRuleFilter(rulesNode, JZRM_SESSION_APP_RULES);
		this.systemRuleFilter = new MonitorApplicativeRuleFilter(rulesNode, JZRM_SYSTEM_APP_RULES);
	}
	
	public MonitorApplicativeRuleFilter getApplicativeTaskRuleFilter() {
		return this.taskRuleFilter;
	}
	
	public MonitorApplicativeRuleFilter getApplicativeSessionRuleFilter() {
		return this.sessionRuleFilter;
	}
	
	public MonitorApplicativeRuleFilter getApplicativeSystemRuleFilter() {
		return this.systemRuleFilter;
	}

	public String getId() {
		return taskRuleFilter.getId() + sessionRuleFilter.getId() + systemRuleFilter.getId();
	}
}
