package org.jeyzer.monitor.engine.rule;

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
import java.util.List;

import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.analyzer.error.JzrMonitorException;
import org.jeyzer.analyzer.session.JzrMonitorSession;
import org.jeyzer.monitor.config.engine.ConfigMonitorAnalyzerThreshold;
import org.jeyzer.monitor.config.engine.ConfigMonitorRule;
import org.jeyzer.monitor.engine.event.MonitorAnalyzerEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;
import org.jeyzer.monitor.engine.event.info.Scope;
import org.jeyzer.monitor.engine.rule.threshold.MonitorThreshold;
import org.jeyzer.monitor.engine.rule.threshold.analyzer.AnalyzerExceptionThreshold;

import com.google.common.collect.Multimap;

public abstract class MonitorAnalyzerRule extends MonitorRule{
	
	private static final String ANALYZER_RULE_GROUP = "Jeyzer Analyzer";
	
	private AnalyzerExceptionThreshold threshold;
	
	public MonitorAnalyzerRule(String name, String ruleRef, String conditionDescription, String narrative, ConfigMonitorAnalyzerThreshold cfg) throws JzrInitializationException{
		super(name, ruleRef, conditionDescription, narrative, null, ANALYZER_RULE_GROUP, ConfigMonitorRule.JEYZER_SOURCE, false);
		this.threshold = new AnalyzerExceptionThreshold(cfg, Scope.ANALYZER, this.getDefaultSubLevel()); 
	}
	
	public void applyCandidacy(JzrMonitorSession session, Multimap<String, MonitorAnalyzerEvent> events) {
		threshold.applyCandidacy(this, session, events);
	}
	
	public void applyElection(JzrMonitorSession session, Multimap<String, MonitorAnalyzerEvent> events) throws JzrMonitorException {
		threshold.applyElection(this, events);
	}

	public abstract MonitorAnalyzerEvent createAnalyzerEvent(MonitorEventInfo info);
	
	public List<MonitorThreshold> getThresholds(){
		List<MonitorThreshold> thresholds = new ArrayList<>();
		thresholds.add(threshold);
		return thresholds;
	}
	
	@Override
	public boolean isAdvancedMonitoringBased() {
		return false;
	}

	@Override
	protected List<String> getSupportedThresholdTypes() {
		return new ArrayList<String>(); // not supported
	}
}
