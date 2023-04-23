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

import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.analyzer.error.JzrMonitorException;
import org.jeyzer.analyzer.session.JzrMonitorSession;
import org.jeyzer.monitor.config.engine.ConfigMonitorRule;
import org.jeyzer.monitor.config.engine.ConfigMonitorThreshold;
import org.jeyzer.monitor.engine.event.MonitorSessionEvent;
import org.jeyzer.monitor.engine.event.info.Level;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;
import org.jeyzer.monitor.engine.rule.threshold.MonitorSessionThreshold;
import org.jeyzer.monitor.engine.rule.threshold.MonitorThreshold;
import org.jeyzer.monitor.engine.rule.threshold.global.GlobalCustomThreshold;
import org.jeyzer.monitor.engine.rule.threshold.global.GlobalDiffThreshold;
import org.jeyzer.monitor.engine.rule.threshold.global.GlobalPatternThreshold;
import org.jeyzer.monitor.engine.rule.threshold.global.GlobalSignalThreshold;
import org.jeyzer.monitor.engine.rule.threshold.global.GlobalValueThreshold;
import org.jeyzer.monitor.engine.rule.threshold.session.SessionApplicativeThreshold;
import org.jeyzer.monitor.engine.rule.threshold.session.SessionCustomThreshold;
import org.jeyzer.monitor.engine.rule.threshold.session.SessionCustomWithContextThreshold;
import org.jeyzer.monitor.engine.rule.threshold.session.SessionDiffThreshold;
import org.jeyzer.monitor.engine.rule.threshold.session.SessionPatternThreshold;
import org.jeyzer.monitor.engine.rule.threshold.session.SessionPublisherThreshold;
import org.jeyzer.monitor.engine.rule.threshold.session.SessionSignalThreshold;
import org.jeyzer.monitor.engine.rule.threshold.session.SessionValueThreshold;

import com.google.common.collect.Multimap;

public abstract class MonitorSessionRule extends MonitorRule{

	protected List<MonitorSessionThreshold> infoSessionThresholds = new ArrayList<>();
	protected List<MonitorSessionThreshold> warnSessionThresholds = new ArrayList<>();
	protected List<MonitorSessionThreshold> critSessionThresholds = new ArrayList<>();	

	protected List<MonitorSessionThreshold> infoGlobalThresholds = new ArrayList<>();
	protected List<MonitorSessionThreshold> warnGlobalThresholds = new ArrayList<>();
	protected List<MonitorSessionThreshold> critGlobalThresholds = new ArrayList<>();	
	
	public MonitorSessionRule(ConfigMonitorRule def, String ruleName, String conditionDescription) throws JzrInitializationException {
		super(def, ruleName, conditionDescription);
		loadThresholds(def.getConfigMonitorThresholds());
	}
	
	private void loadThresholds(List<ConfigMonitorThreshold> configMonitorThresholds) throws JzrInitializationException {
		for (ConfigMonitorThreshold thCfg : configMonitorThresholds){
			if (SessionSignalThreshold.THRESHOLD_NAME.equals(thCfg.getName())){
				addSessionThreshold(new SessionSignalThreshold(thCfg, this.getDefaultSubLevel()));
			}
			else if (SessionValueThreshold.THRESHOLD_NAME.equals(thCfg.getName())){
				addSessionThreshold(new SessionValueThreshold(thCfg, this.getDefaultSubLevel()));
			}
			else if (SessionPatternThreshold.THRESHOLD_NAME.equals(thCfg.getName())){
				addSessionThreshold(new SessionPatternThreshold(thCfg, this.getDefaultSubLevel()));
			}
			else if (SessionDiffThreshold.THRESHOLD_NAME.equals(thCfg.getName())){
				addSessionThreshold(new SessionDiffThreshold(thCfg, this.getDefaultSubLevel()));
			}
			else if (SessionCustomThreshold.THRESHOLD_NAME.equals(thCfg.getName())){
				addSessionThreshold(new SessionCustomThreshold(thCfg, this.getDefaultSubLevel()));
			}
			else if (SessionCustomWithContextThreshold.THRESHOLD_NAME.equals(thCfg.getName())){
				addSessionThreshold(new SessionCustomWithContextThreshold(thCfg, this.getDefaultSubLevel()));
			}
			else if (GlobalSignalThreshold.THRESHOLD_NAME.equals(thCfg.getName())){
				addGlobalThreshold(new GlobalSignalThreshold(thCfg, this.getDefaultSubLevel()));
			}
			else if (GlobalDiffThreshold.THRESHOLD_NAME.equals(thCfg.getName())){
				addGlobalThreshold(new GlobalDiffThreshold(thCfg, this.getDefaultSubLevel()));
			}
			else if (GlobalPatternThreshold.THRESHOLD_NAME.equals(thCfg.getName())){
				addGlobalThreshold(new GlobalPatternThreshold(thCfg, this.getDefaultSubLevel()));
			}
			else if (GlobalValueThreshold.THRESHOLD_NAME.equals(thCfg.getName())){
				addGlobalThreshold(new GlobalValueThreshold(thCfg, this.getDefaultSubLevel()));
			}
			else if (GlobalCustomThreshold.THRESHOLD_NAME.equals(thCfg.getName())){
				addGlobalThreshold(new GlobalCustomThreshold(thCfg, this.getDefaultSubLevel()));
			}
			else if (SessionApplicativeThreshold.THRESHOLD_NAME.equals(thCfg.getName())) {
				addSessionThreshold(new SessionApplicativeThreshold(thCfg));
			}
			else if (SessionPublisherThreshold.THRESHOLD_NAME.equals(thCfg.getName())) {
				addSessionThreshold(new SessionPublisherThreshold(thCfg));
			}
			else{
				throw new JzrInitializationException("Session threshold not supported : " + thCfg.getName());
			}
		}
	}
		
	protected void addSessionThreshold(MonitorSessionThreshold threshold) {
		if (Level.INFO.equals(threshold.getLevel()))
			this.infoSessionThresholds.add(threshold);
		else if (Level.WARNING.equals(threshold.getLevel()))
			this.warnSessionThresholds.add(threshold);
		else if (Level.CRITICAL.equals(threshold.getLevel()))
			this.critSessionThresholds.add(threshold);
	}
	
	private void addGlobalThreshold(MonitorSessionThreshold threshold) {
		if (Level.INFO.equals(threshold.getLevel()))
			this.infoGlobalThresholds.add(threshold);
		else if (Level.WARNING.equals(threshold.getLevel()))
			this.warnGlobalThresholds.add(threshold);
		else if (Level.CRITICAL.equals(threshold.getLevel()))
			this.critGlobalThresholds.add(threshold);
	}	
	
	public void applyCandidacy(JzrMonitorSession session, Multimap<String, MonitorSessionEvent> events) {
		if (!this.enabled)
			return; // rule disabled by sticker		
		
		// session
		applyCandidacy(this.critSessionThresholds, session, events);
		applyCandidacy(this.warnSessionThresholds, session, events);
		applyCandidacy(this.infoSessionThresholds, session, events);
		
		// global
		applyCandidacy(this.critGlobalThresholds, session, events);
		applyCandidacy(this.warnGlobalThresholds, session, events);
		applyCandidacy(this.infoGlobalThresholds, session, events);
	}
	
	private void applyCandidacy(List<MonitorSessionThreshold> thresholds, JzrMonitorSession session, Multimap<String, MonitorSessionEvent> events) {
		for (MonitorSessionThreshold threshold : thresholds){
			threshold.applyCandidacy(this, session, events);
		}
	}
	
	public void applyElection(JzrMonitorSession session, Multimap<String, MonitorSessionEvent> events) throws JzrMonitorException {
		if (!this.enabled)
			return; // rule disabled by sticker		
		
		// session
		applyElection(this.infoSessionThresholds, session, events);
		applyElection(this.warnSessionThresholds, session, events);
		applyElection(this.critSessionThresholds, session, events);
		
		// global
		applyElection(this.infoGlobalThresholds, session, events);
		applyElection(this.warnGlobalThresholds, session, events);
		applyElection(this.critGlobalThresholds, session, events);
	}

	private void applyElection(List<MonitorSessionThreshold> thresholds, JzrMonitorSession session, Multimap<String, MonitorSessionEvent> events) throws JzrMonitorException {
		for (MonitorSessionThreshold threshold : thresholds){
			threshold.applyElection(this, events);
		}
		
		// remove duplicate elected events (same start and end date, but of different category)
		// do it once for each threshold level category
		if (!thresholds.isEmpty()){
			// remove 
			thresholds.get(0).removeElectedDuplicates(this, events);
		}
	}

	public abstract MonitorSessionEvent createSessionEvent(MonitorEventInfo info, ThreadDump td);
	
	@Override
	public List<MonitorThreshold> getThresholds(){
		List<MonitorThreshold> thresholds = new ArrayList<>();
		
		thresholds.addAll(this.infoSessionThresholds);
		thresholds.addAll(this.warnSessionThresholds);
		thresholds.addAll(this.critSessionThresholds);
		
		thresholds.addAll(this.infoGlobalThresholds);
		thresholds.addAll(this.warnGlobalThresholds);
		thresholds.addAll(this.critGlobalThresholds);
		
		return thresholds;
	}
}
