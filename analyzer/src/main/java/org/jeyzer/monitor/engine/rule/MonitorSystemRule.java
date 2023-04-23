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
import org.jeyzer.monitor.config.engine.ConfigMonitorRule;
import org.jeyzer.monitor.config.engine.ConfigMonitorThreshold;
import org.jeyzer.monitor.engine.event.MonitorSystemEvent;
import org.jeyzer.monitor.engine.event.info.Level;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;
import org.jeyzer.monitor.engine.rule.threshold.MonitorSystemThreshold;
import org.jeyzer.monitor.engine.rule.threshold.MonitorThreshold;
import org.jeyzer.monitor.engine.rule.threshold.system.SystemApplicativeThreshold;
import org.jeyzer.monitor.engine.rule.threshold.system.SystemPatternThreshold;
import org.jeyzer.monitor.engine.rule.threshold.system.SystemSignalThreshold;
import org.jeyzer.monitor.engine.rule.threshold.system.SystemValueThreshold;

import com.google.common.collect.Multimap;

public abstract class MonitorSystemRule extends MonitorRule {

	protected List<MonitorSystemThreshold> infoSystemThresholds = new ArrayList<>();
	protected List<MonitorSystemThreshold> warnSystemThresholds = new ArrayList<>();
	protected List<MonitorSystemThreshold> critSystemThresholds = new ArrayList<>();		
	
	public MonitorSystemRule(ConfigMonitorRule def, String ruleName, String conditionDescription) throws JzrInitializationException {
		super(def, ruleName, conditionDescription);
		loadThresholds(def.getConfigMonitorThresholds());
	}
	
	public abstract MonitorSystemEvent createSystemEvent(MonitorEventInfo info);

	public void applyCandidacy(JzrMonitorSession session, Multimap<String, MonitorSystemEvent> events) {
		if (!this.enabled)
			return; // rule disabled by sticker		
		
		// system
		applyCandidacy(this.critSystemThresholds, session, events);
		applyCandidacy(this.warnSystemThresholds, session, events);
		applyCandidacy(this.infoSystemThresholds, session, events);
	}
	
	public void applyElection(JzrMonitorSession session, Multimap<String, MonitorSystemEvent> events) throws JzrMonitorException {
		if (!this.enabled)
			return; // rule disabled by sticker		
		
		// system
		applyElection(this.infoSystemThresholds, session, events);
		applyElection(this.warnSystemThresholds, session, events);
		applyElection(this.critSystemThresholds, session, events);
	}
	
	@Override
	public List<MonitorThreshold> getThresholds(){
		List<MonitorThreshold> thresholds = new ArrayList<>();
		
		thresholds.addAll(this.infoSystemThresholds);
		thresholds.addAll(this.warnSystemThresholds);
		thresholds.addAll(this.critSystemThresholds);
		
		return thresholds;
	}
	
	private void applyCandidacy(List<MonitorSystemThreshold> thresholds, JzrMonitorSession session, Multimap<String, MonitorSystemEvent> events) {
		for (MonitorSystemThreshold threshold : thresholds){
			threshold.applyCandidacy(this, session, events);
		}
	}
	
	private void applyElection(List<MonitorSystemThreshold> thresholds, JzrMonitorSession session, Multimap<String, MonitorSystemEvent> events) throws JzrMonitorException {
		for (MonitorSystemThreshold threshold : thresholds){
			threshold.applyElection(this, events);
		}
		
		// remove duplicate elected events (same start and end date, but of different category)
		// do it once for each threshold level category
		if (!thresholds.isEmpty()){
			// remove 
			thresholds.get(0).removeElectedDuplicates(this, events);
		}
	}

	private void loadThresholds(List<ConfigMonitorThreshold> configMonitorThresholds) throws JzrInitializationException {
		for (ConfigMonitorThreshold thCfg : configMonitorThresholds){
			if (SystemPatternThreshold.THRESHOLD_NAME.equals(thCfg.getName())){
				addSystemThreshold(new SystemPatternThreshold(thCfg, this.getDefaultSubLevel()));
			}
			else if (SystemValueThreshold.THRESHOLD_NAME.equals(thCfg.getName())){
				addSystemThreshold(new SystemValueThreshold(thCfg, this.getDefaultSubLevel()));
			}
			else if (SystemSignalThreshold.THRESHOLD_NAME.equals(thCfg.getName())){
				addSystemThreshold(new SystemSignalThreshold(thCfg, this.getDefaultSubLevel()));
			}
			else if (SystemApplicativeThreshold.THRESHOLD_NAME.equals(thCfg.getName())) {
				addSystemThreshold(new SystemApplicativeThreshold(thCfg));
			}
			else{
				throw new JzrInitializationException("System threshold not supported : " + thCfg.getName());
			}
		}
	}

	protected void addSystemThreshold(MonitorSystemThreshold threshold) {
		if (Level.INFO.equals(threshold.getLevel()))
			this.infoSystemThresholds.add(threshold);
		else if (Level.WARNING.equals(threshold.getLevel()))
			this.warnSystemThresholds.add(threshold);
		else if (Level.CRITICAL.equals(threshold.getLevel()))
			this.critSystemThresholds.add(threshold);
	}	
	
}
