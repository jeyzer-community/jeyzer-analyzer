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
import java.util.List;

import org.jeyzer.analyzer.error.JzrMonitorException;
import org.jeyzer.analyzer.session.JzrMonitorSession;
import org.jeyzer.monitor.config.engine.ConfigMonitorThreshold;
import org.jeyzer.monitor.engine.event.MonitorSystemEvent;
import org.jeyzer.monitor.engine.event.info.Level;
import org.jeyzer.monitor.engine.event.info.Scope;
import org.jeyzer.monitor.engine.event.info.SubLevel;
import org.jeyzer.monitor.engine.rule.MonitorSystemRule;

import com.google.common.collect.Multimap;

public abstract class MonitorSystemThreshold extends MonitorThreshold {

	public MonitorSystemThreshold(ConfigMonitorThreshold thCfg, Scope scope, MatchType matchType, SubLevel defaultSubLevel) {
		super(thCfg, scope, matchType, defaultSubLevel);
	}
	
	public abstract void applyCandidacy(MonitorSystemRule rule, JzrMonitorSession session, Multimap<String, MonitorSystemEvent> events);

	public void applyElection(MonitorSystemRule rule, Multimap<String, MonitorSystemEvent> events) throws JzrMonitorException {
		String eventId = buildEventId(rule.getRef(), null, this.cfg.getLevel());
		
		for (MonitorSystemEvent event : events.get(eventId)){
			if (event.isElected())
				continue; // event in progress from previous session : should not happen as system type is not supported on monitoring
			
			// always elect
			event.elect();
			hit();
		}
	}
	
	public void removeElectedDuplicates(MonitorSystemRule sessionRule, Multimap<String, MonitorSystemEvent> events) {
		if (Level.CRITICAL.equals(this.cfg.getLevel())){
			removeDuplicates(sessionRule, events, Level.WARNING);
			removeDuplicates(sessionRule, events, Level.INFO);
		}
		else if (Level.WARNING.equals(this.cfg.getLevel())){
			removeDuplicates(sessionRule, events, Level.INFO);
		}
	}
	
	private void removeDuplicates(MonitorSystemRule systemRule, Multimap<String, MonitorSystemEvent> events, Level category) {
		for (MonitorSystemEvent highEvent : events.get(buildEventId(systemRule.getRef(), null, this.cfg.getLevel()))){
			if (!highEvent.isElected())
				continue;
				
			String eventTypeId = buildEventId(systemRule.getRef(), null, category);
			Collection<MonitorSystemEvent> lowEvents = events.get(eventTypeId);
			if (lowEvents == null)
				continue; // no events of lower category
				
			MonitorSystemEvent eventToRemove = null;
			for (MonitorSystemEvent lowEvent : lowEvents){
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

	protected MonitorSystemEvent fetchPreviousUniqueEvent(Multimap<String, MonitorSystemEvent> events, String eventId, int period) {
		List<MonitorSystemEvent> sameIdEvents = (List<MonitorSystemEvent>) events.get(eventId);
		
		// no other event of same type
		if (sameIdEvents.isEmpty())
			return null;

		// get latest created one
		MonitorSystemEvent event = sameIdEvents.get(sameIdEvents.size()-1); // there should be only one
		
		if (period == -1)  // case Monitor session error : create new event
			return null;
		
		return event;
	}
	
}
