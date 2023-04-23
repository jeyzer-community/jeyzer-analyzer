package org.jeyzer.monitor.engine.rule.threshold.action;

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




import java.util.Date;

import org.jeyzer.analyzer.data.ThreadAction;
import org.jeyzer.analyzer.data.event.ExternalEvent;
import org.jeyzer.analyzer.session.JzrMonitorSession;
import org.jeyzer.monitor.config.engine.ConfigMonitorThreshold;
import org.jeyzer.monitor.engine.event.MonitorTaskEvent;
import org.jeyzer.monitor.engine.event.info.ApplicativeEventInfo;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;
import org.jeyzer.monitor.engine.event.info.Scope;
import org.jeyzer.monitor.engine.event.info.SubLevel;
import org.jeyzer.monitor.engine.rule.MonitorTaskRule;
import org.jeyzer.monitor.engine.rule.condition.applicative.ApplicativeEventProvider;
import org.jeyzer.monitor.engine.rule.threshold.MonitorTaskThreshold;

import com.google.common.collect.Multimap;

public class ActionApplicativeThreshold extends MonitorTaskThreshold{

	public static final String THRESHOLD_NAME = "action applicative";
	
	public ActionApplicativeThreshold(ConfigMonitorThreshold thCfg) {
		super(thCfg, Scope.ACTION, MatchType.APPLICATIVE, SubLevel.DEFAULT_MEDIUM);
	}
	
	@Override
	// Must always be called before the SessionApplicativeThreshold, in order to handle first any task applicative event
	public void applyCandidacy(MonitorTaskRule rule, JzrMonitorSession session, ThreadAction action, Multimap<String, MonitorTaskEvent> events) {
		ApplicativeEventProvider eventProvider = (ApplicativeEventProvider) rule;
		
		for (ExternalEvent extEvent : eventProvider.getApplicativeEvents()) {
			if (extEvent.hasApplicativeRuleHit())
				continue; // manage the case event got processed by session rule first, but should never be the case by design
			
			if (extEvent.getThreadId().equals(action.getThreadId())) {
				if (haveOverlappingTimeRanges(extEvent, action)) {
					matchAction(session, rule, extEvent, action, events);
				}
			}
		}
	}
	
	private void matchAction(JzrMonitorSession session, MonitorTaskRule rule, ExternalEvent extEvent, ThreadAction action, Multimap<String, MonitorTaskEvent> events) {
		MonitorTaskEvent event = null;
		
		extEvent.hitApplicativeRule();
		
		// create event
		String eventId = buildEventId(rule.getRef(), buildSuffixId(action), this.cfg.getLevel());
		
		// create candidate
		MonitorEventInfo info = new ApplicativeEventInfo(
				eventId,
				rule.getRef() + this.getRef(),
				Scope.ACTION,
				this.cfg.getLevel(),
				this.subLevel,
				extEvent.getSnapshotStart(),
				extEvent.getSnapshotEnd(),
				extEvent.getMessage(),
				extEvent.getTicket(),
				extEvent.getId(),
				extEvent.getStart(),
				extEvent.getEnd(),
				extEvent.getThreadId(),
				extEvent.isOneshot()
				);
		event = rule.createTaskEvent(info, action, action.getThreadStack(0).getStackHandler());
		events.put(eventId, event);
		
		if (extEvent.getEnd() != null)
			event.setProgressStatus(new Date(0)); // force event closure
		else
			event.setProgressStatus(session.getEndDate()); // in progress
				
		// on consecutive monitoring sessions, the in-progress event end date will be updated
		// at the beginning of the next monitoring session. 
		// Thread id must be used to make the link.
	}

	private boolean haveOverlappingTimeRanges(ExternalEvent extEvent, ThreadAction action) {
		// could also use the action min start date and max end date, but would not be 100% sure of the overlapping
		return !(extEvent.getStart().after(action.getEndDate())) 
				&& !(extEvent.getEnd() != null && extEvent.getEnd().before(action.getStartDate()));
	}

	@Override
	public String getDisplayCondition() {
		return "See condition";
	}
	
}
