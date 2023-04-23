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

import org.jeyzer.analyzer.error.JzrMonitorException;
import org.jeyzer.analyzer.session.JzrMonitorSession;
import org.jeyzer.monitor.config.engine.ConfigMonitorThreshold;
import org.jeyzer.monitor.engine.event.MonitorSessionEvent;
import org.jeyzer.monitor.engine.event.info.Level;
import org.jeyzer.monitor.engine.event.info.Scope;
import org.jeyzer.monitor.engine.event.info.SubLevel;
import org.jeyzer.monitor.engine.rule.MonitorSessionRule;

import com.google.common.collect.Multimap;

public abstract class MonitorSessionThreshold extends MonitorThreshold{

	public MonitorSessionThreshold(ConfigMonitorThreshold thCfg, Scope scope, MatchType matchType, SubLevel defaultSubLevel) {
		super(thCfg, scope, matchType, defaultSubLevel);
	}
	
	public abstract void applyCandidacy(MonitorSessionRule rule, JzrMonitorSession session, Multimap<String, MonitorSessionEvent> events);

	protected MonitorSessionEvent fetchPreviousEvent(Multimap<String, MonitorSessionEvent> events, String eventId, Date start, int period){
		List<MonitorSessionEvent> sameIdEvents = (List<MonitorSessionEvent>) events.get(eventId);
		
		// no other event of same type
		if (sameIdEvents.isEmpty())
			return null;

		// get latest created one
		MonitorSessionEvent event = sameIdEvents.get(sameIdEvents.size()-1);
		
		if (period == -1)  // case Monitor session error : create new event
			return null;
		
		// must match time continuity
		if (!isAdjacentEvent(event, start, period))
			return null;
		
		return event;
	}
	
	protected MonitorSessionEvent fetchPreviousUniqueEvent(Multimap<String, MonitorSessionEvent> events, String eventId, Date start, int period){
		List<MonitorSessionEvent> sameIdEvents = (List<MonitorSessionEvent>) events.get(eventId);
		
		// no other event of same type
		if (sameIdEvents.isEmpty())
			return null;

		// get latest created one
		MonitorSessionEvent event = sameIdEvents.get(sameIdEvents.size()-1); // there should be only one
		
		if (period == -1)  // case Monitor session error : create new event
			return null;
		
		return event;
	}	
	
	public void applyElection(MonitorSessionRule rule, Multimap<String, MonitorSessionEvent> events) throws JzrMonitorException {
		String eventId = buildEventId(rule.getRef(), null, this.cfg.getLevel());
		
		for (MonitorSessionEvent event : events.get(eventId)){
			if (event.isElected())
				continue; // event in progress from previous session 
			
			if (timeElection(event)){
				event.elect();
				hit();
			}
		}
	}
	
	public void removeElectedDuplicates(MonitorSessionRule sessionRule, Multimap<String, MonitorSessionEvent> events) {
		if (Level.CRITICAL.equals(this.cfg.getLevel())){
			removeDuplicates(sessionRule, events, Level.WARNING);
			removeDuplicates(sessionRule, events, Level.INFO);
		}
		else if (Level.WARNING.equals(this.cfg.getLevel())){
			removeDuplicates(sessionRule, events, Level.INFO);
		}
	}

	private void removeDuplicates(MonitorSessionRule sessionRule, Multimap<String, MonitorSessionEvent> events, Level category) {
		for (MonitorSessionEvent highEvent : events.get(buildEventId(sessionRule.getRef(), null, this.cfg.getLevel()))){
			if (!highEvent.isElected())
				continue;
				
			String eventTypeId = buildEventId(sessionRule.getRef(), null, category);
			Collection<MonitorSessionEvent> lowEvents = events.get(eventTypeId);
			if (lowEvents == null)
				continue; // no events of lower category
				
			MonitorSessionEvent eventToRemove = null;
			for (MonitorSessionEvent lowEvent : lowEvents){
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
}
