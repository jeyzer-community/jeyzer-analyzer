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
import java.util.Comparator;
import java.util.List;

import org.jeyzer.analyzer.data.ThreadAction;
import org.jeyzer.analyzer.data.stack.ThreadStack.ThreadStackHandler;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.analyzer.error.JzrMonitorException;
import org.jeyzer.analyzer.session.JzrMonitorSession;
import org.jeyzer.monitor.config.engine.ConfigMonitorRule;
import org.jeyzer.monitor.config.engine.ConfigMonitorTaskThreshold;
import org.jeyzer.monitor.config.engine.ConfigMonitorThreshold;
import org.jeyzer.monitor.engine.event.MonitorTaskEvent;
import org.jeyzer.monitor.engine.event.info.Level;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;
import org.jeyzer.monitor.engine.rule.threshold.MonitorTaskThreshold;
import org.jeyzer.monitor.engine.rule.threshold.MonitorThreshold;
import org.jeyzer.monitor.engine.rule.threshold.action.ActionApplicativeThreshold;
import org.jeyzer.monitor.engine.rule.threshold.action.ActionPatternPercentageThreshold;
import org.jeyzer.monitor.engine.rule.threshold.action.ActionPatternThreshold;
import org.jeyzer.monitor.engine.rule.threshold.action.ActionSignalPercentageThreshold;
import org.jeyzer.monitor.engine.rule.threshold.action.ActionSignalThreshold;
import org.jeyzer.monitor.engine.rule.threshold.action.ActionValuePercentageThreshold;
import org.jeyzer.monitor.engine.rule.threshold.action.ActionValueThreshold;
import org.jeyzer.monitor.engine.rule.threshold.stack.StackPatternThreshold;
import org.jeyzer.monitor.engine.rule.threshold.stack.StackSignalThreshold;
import org.jeyzer.monitor.engine.rule.threshold.stack.StackSignalWithContextThreshold;
import org.jeyzer.monitor.engine.rule.threshold.stack.StackValueThreshold;

import com.google.common.collect.Multimap;

public abstract class MonitorTaskRule extends MonitorRule {
	
	protected List<MonitorTaskThreshold> infoStackThresholds = new ArrayList<>();
	protected List<MonitorTaskThreshold> warnStackThresholds = new ArrayList<>();
	protected List<MonitorTaskThreshold> critStackThresholds = new ArrayList<>();
	
	protected List<MonitorTaskThreshold> infoActionThresholds = new ArrayList<>();
	protected List<MonitorTaskThreshold> warnActionThresholds = new ArrayList<>();
	protected List<MonitorTaskThreshold> critActionThresholds = new ArrayList<>();
	
	public MonitorTaskRule(ConfigMonitorRule def, String ruleName, String conditionDescription) throws JzrInitializationException {
		super(def, ruleName, conditionDescription);
		loadThresholds(def.getConfigMonitorThresholds());
	}

	public abstract MonitorTaskEvent createTaskEvent(MonitorEventInfo info, ThreadAction action, ThreadStackHandler stack);
	
	private void loadThresholds(List<ConfigMonitorThreshold> configMonitorThresholds) throws JzrInitializationException {
		for (ConfigMonitorThreshold thCfg : configMonitorThresholds){
			ConfigMonitorTaskThreshold actionThresholdCfg = (ConfigMonitorTaskThreshold) thCfg;
			if (StackSignalThreshold.THRESHOLD_NAME.equals(thCfg.getName())){
				addStackThreshold(new StackSignalThreshold(thCfg, this.getDefaultSubLevel()));
			}
			else if (StackSignalWithContextThreshold.THRESHOLD_NAME.equals(thCfg.getName())){
				addStackThreshold(new StackSignalWithContextThreshold(thCfg, this.getDefaultSubLevel()));
			}
			else if (ActionSignalThreshold.THRESHOLD_NAME.equals(thCfg.getName())
					&& !actionThresholdCfg.hasPercentage()){
				addActionThreshold(new ActionSignalThreshold(thCfg, this.getDefaultSubLevel()));
			}
			else if (ActionSignalThreshold.THRESHOLD_NAME.equals(thCfg.getName())
					&& actionThresholdCfg.hasPercentage()){
				addActionThreshold(new ActionSignalPercentageThreshold(thCfg, this.getDefaultSubLevel()));
			}
			else if (StackValueThreshold.THRESHOLD_NAME.equals(thCfg.getName())){
				addStackThreshold(new StackValueThreshold(thCfg, this.getDefaultSubLevel()));
			}
			else if (ActionValueThreshold.THRESHOLD_NAME.equals(thCfg.getName())
					&& !actionThresholdCfg.hasPercentage()){
				addActionThreshold(new ActionValueThreshold(thCfg, this.getDefaultSubLevel()));
			}
			else if (ActionValueThreshold.THRESHOLD_NAME.equals(thCfg.getName())
					&& actionThresholdCfg.hasPercentage()){
				addActionThreshold(new ActionValuePercentageThreshold(thCfg, this.getDefaultSubLevel()));
			}
			else if (StackPatternThreshold.THRESHOLD_NAME.equals(thCfg.getName())){
				addStackThreshold(new StackPatternThreshold(thCfg, this.getDefaultSubLevel()));
			}
			else if (ActionPatternThreshold.THRESHOLD_NAME.equals(thCfg.getName())
					&& !actionThresholdCfg.hasPercentage()){
				addActionThreshold(new ActionPatternThreshold(thCfg, this.getDefaultSubLevel()));
			}
			else if (ActionPatternThreshold.THRESHOLD_NAME.equals(thCfg.getName())
					&& actionThresholdCfg.hasPercentage()){
				addActionThreshold(new ActionPatternPercentageThreshold(thCfg, this.getDefaultSubLevel()));
			}
			else if (ActionApplicativeThreshold.THRESHOLD_NAME.equals(thCfg.getName())) {
				addActionThreshold(new ActionApplicativeThreshold(thCfg));
			}
			else{
				throw new JzrInitializationException("Task threshold not supported : " + thCfg.getName());
			}
		}
		
		MonitorTaskThresholdFunctionComparator comparator = new MonitorTaskThresholdFunctionComparator();
		infoStackThresholds.sort(comparator);
		warnStackThresholds.sort(comparator);
		critStackThresholds.sort(comparator);

		infoActionThresholds.sort(comparator);
		warnActionThresholds.sort(comparator);
		critActionThresholds.sort(comparator);
	}
	
	private void addStackThreshold(MonitorTaskThreshold threshold) {
		if (Level.INFO.equals(threshold.getLevel()))
			this.infoStackThresholds.add(threshold);
		else if (Level.WARNING.equals(threshold.getLevel()))
			this.warnStackThresholds.add(threshold);
		else if (Level.CRITICAL.equals(threshold.getLevel()))
			this.critStackThresholds.add(threshold);
	}
	
	protected void addActionThreshold(MonitorTaskThreshold threshold) {
		if (Level.INFO.equals(threshold.getLevel()))
			this.infoActionThresholds.add(threshold);
		else if (Level.WARNING.equals(threshold.getLevel()))
			this.warnActionThresholds.add(threshold);
		else if (Level.CRITICAL.equals(threshold.getLevel()))
			this.critActionThresholds.add(threshold);
	}	
	
	public void applyCandidacy(JzrMonitorSession session, ThreadAction action, Multimap<String, MonitorTaskEvent> events) {
		if (!this.enabled)
			return; // rule disabled by sticker		
		
		//stacks
		applyCandidacy(this.critStackThresholds, session, action, events);
		applyCandidacy(this.warnStackThresholds, session, action, events);
		applyCandidacy(this.infoStackThresholds, session, action, events);
		
		// actions
		applyCandidacy(this.critActionThresholds, session, action, events);
		applyCandidacy(this.warnActionThresholds, session, action, events);
		applyCandidacy(this.infoActionThresholds, session, action, events);
	}

	private void applyCandidacy(List<MonitorTaskThreshold> thresholds, JzrMonitorSession session, ThreadAction action, Multimap<String, MonitorTaskEvent> events) {
		for (MonitorTaskThreshold threshold : thresholds){
			if (threshold.accept(action)){
				threshold.applyCandidacy(this, session, action, events);
				return; // only one threshold applied per category. Function based thresholds first
			}
		}
	}

	public void applyElection(JzrMonitorSession session, ThreadAction action, Multimap<String, MonitorTaskEvent> events) throws JzrMonitorException {
		if (!this.enabled)
			return; // rule disabled by sticker		
		
		// stacks
		applyElection(this.infoStackThresholds, session, action, events);
		applyElection(this.warnStackThresholds, session, action, events);
		applyElection(this.critStackThresholds, session, action, events);
		
		// actions
		applyElection(this.infoActionThresholds, session, action, events);
		applyElection(this.warnActionThresholds, session, action, events);
		applyElection(this.critActionThresholds, session, action, events);
	}

	private void applyElection(List<MonitorTaskThreshold> thresholds, JzrMonitorSession session, ThreadAction action, Multimap<String, MonitorTaskEvent> events) throws JzrMonitorException {
		for (MonitorTaskThreshold threshold : thresholds){
			if (threshold.accept(action)){
				threshold.applyElection(this, action, events);
				break;
			}
		}
		
		// remove duplicate elected events (same start and end date, but of different category and scope)
		// do it once for each threshold level category
		if (!thresholds.isEmpty()){
			// remove 
			thresholds.get(0).removeElectedDuplicates(this, action, events);
		}
	}

	public class MonitorTaskThresholdFunctionComparator implements Comparator<MonitorTaskThreshold>{

		@Override
		public int compare(MonitorTaskThreshold th0, MonitorTaskThreshold th1) {
			if (th0.hasFunction() && th1.hasFunction())
				return 0;
			else if (!th0.hasFunction() && th1.hasFunction())
				return 1;
			else
				return -1;
		}
	}
	
	@Override
	public List<MonitorThreshold> getThresholds(){
		List<MonitorThreshold> thresholds = new ArrayList<>();
		
		thresholds.addAll(this.infoStackThresholds);
		thresholds.addAll(this.warnStackThresholds);
		thresholds.addAll(this.critStackThresholds);
		
		thresholds.addAll(this.infoActionThresholds);
		thresholds.addAll(this.warnActionThresholds);
		thresholds.addAll(this.critActionThresholds);
		
		return thresholds;
	}
}
