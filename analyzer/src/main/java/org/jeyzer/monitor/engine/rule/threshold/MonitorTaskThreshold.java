package org.jeyzer.monitor.engine.rule.threshold;

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







import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.jeyzer.analyzer.data.ThreadAction;
import org.jeyzer.analyzer.error.JzrMonitorException;
import org.jeyzer.analyzer.session.JzrMonitorSession;
import org.jeyzer.monitor.config.engine.ConfigMonitorTaskThreshold;
import org.jeyzer.monitor.config.engine.ConfigMonitorThreshold;
import org.jeyzer.monitor.engine.event.MonitorTaskEvent;
import org.jeyzer.monitor.engine.event.info.Level;
import org.jeyzer.monitor.engine.event.info.Scope;
import org.jeyzer.monitor.engine.event.info.SubLevel;
import org.jeyzer.monitor.engine.rule.MonitorTaskRule;

import com.google.common.collect.Multimap;

public abstract class MonitorTaskThreshold extends MonitorThreshold{

	// regex
	private Pattern functionPattern = null; 
	
	public MonitorTaskThreshold(ConfigMonitorThreshold thCfg, Scope scope, MatchType matchType, SubLevel defaultSubLevel) {
		super(thCfg, scope, matchType, defaultSubLevel);
		String function = ((ConfigMonitorTaskThreshold)this.cfg).getFunction();
		if (function != null && !function.isEmpty())
			functionPattern = Pattern.compile(function);
	}
	
	public abstract void applyCandidacy(MonitorTaskRule rule, JzrMonitorSession session, ThreadAction action, Multimap<String, MonitorTaskEvent> events);
	
	protected String buildSuffixId(ThreadAction action){
		if (action.getThreadStackJeyzerMXInfo() != null && !action.getThreadStackJeyzerMXInfo().getJzrId().isEmpty())
			return action.getId() + "@" +  action.getThreadStackJeyzerMXInfo().getJzrId() + " / " + action.getName();
		else
			return action.getId() + " / " + action.getName();
	}
	
	protected MonitorTaskEvent fetchPreviousEvent(Multimap<String, MonitorTaskEvent> events, String eventId, Date start, int period){
		List<MonitorTaskEvent> sameIdEvents = (List<MonitorTaskEvent>) events.get(eventId);
		
		// no other event of same type
		if (sameIdEvents.isEmpty())
			return null;

		// get latest created one
		MonitorTaskEvent event = sameIdEvents.get(sameIdEvents.size()-1);
		
		if (period == -1)  // case Monitor session error : create new event
			return null;
		
		// must match time continuity
		if (!isAdjacentEvent(event, start, period))
			return null;
		
		return event;
	}

	public void removeElectedDuplicates(MonitorTaskRule taskRule, ThreadAction action, Multimap<String, MonitorTaskEvent> events) {
		if (Level.CRITICAL.equals(this.cfg.getLevel())){
			removeDuplicates(taskRule, action, events, Level.WARNING);
			removeDuplicates(taskRule, action, events, Level.INFO);
		}
		else if (Level.WARNING.equals(this.cfg.getLevel())){
			removeDuplicates(taskRule, action, events, Level.INFO);
		}
	}

	private void removeDuplicates(MonitorTaskRule taskRule, ThreadAction action, Multimap<String, MonitorTaskEvent> events, Level category) {
		for (MonitorTaskEvent highEvent : events.get(buildEventId(taskRule.getRef(), buildSuffixId(action), this.cfg.getLevel()))){
			if (!highEvent.isElected())
				continue;
				
			String eventTypeId = buildEventId(taskRule.getRef(), buildSuffixId(action), category);
			Collection<MonitorTaskEvent> lowEvents = events.get(eventTypeId);
			if (lowEvents == null)
				continue; // no events of lower category
				
			MonitorTaskEvent eventToRemove = null;
			for (MonitorTaskEvent lowEvent : lowEvents){
				if (!lowEvent.isElected())
					continue;
				if (highEvent.getStartDate().equals(lowEvent.getStartDate())
						&& highEvent.getEndDate().equals(lowEvent.getEndDate())){
					// both events have same start and end date
					eventToRemove = lowEvent;
					break;
				}
			}
				
			// cleanup lower duplicate event
			if (eventToRemove != null)
				lowEvents.remove(eventToRemove);
		}
	}
	
	public boolean hasFunction(){
		return functionPattern != null;
	}

	public boolean accept(ThreadAction action) {
		if (functionPattern == null)
			return true; // default
		return functionPattern.matcher(action.getPrincipalCompositeFunction()).matches();
	}

	public void applyElection(MonitorTaskRule rule, ThreadAction action, Multimap<String, MonitorTaskEvent> events) throws JzrMonitorException {
		String eventId = buildEventId(rule.getRef(), buildSuffixId(action), this.cfg.getLevel());
		
		for (MonitorTaskEvent event : events.get(eventId)){
			if (event.isElected())
				continue; // event in progress from previous session 
			
			if (timeElection(event)){
				event.elect();
				hit();
			}
		}
	}
	
	public String getFunction(){
		if (hasFunction())
			return this.functionPattern.pattern();
		else
			return null;
	}
	
	public int getPercentageInAction(){
		return ((ConfigMonitorTaskThreshold)this.cfg).getPercentageInAction();
	}
	
	@Override
	protected String buildEventId(String ruleRef, String eventId, Level category){
		String id = super.buildEventId(ruleRef, eventId, category);
		id += SEPARATOR + getPercentageInAction();
		if (this.cfg.getPattern() != null)
			id += SEPARATOR + this.cfg.getPattern().toString();
		if (hasFunction())
			id += SEPARATOR + ((ConfigMonitorTaskThreshold)this.cfg).getFunction();
		return id;
	}
}
