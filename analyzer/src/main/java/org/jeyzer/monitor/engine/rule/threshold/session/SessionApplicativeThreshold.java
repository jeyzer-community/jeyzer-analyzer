package org.jeyzer.monitor.engine.rule.threshold.session;

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

import org.jeyzer.analyzer.data.event.ExternalEvent;
import org.jeyzer.analyzer.session.JzrMonitorSession;
import org.jeyzer.monitor.config.engine.ConfigMonitorThreshold;
import org.jeyzer.monitor.engine.event.MonitorSessionEvent;
import org.jeyzer.monitor.engine.event.info.ApplicativeEventInfo;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;
import org.jeyzer.monitor.engine.event.info.Scope;
import org.jeyzer.monitor.engine.event.info.SubLevel;
import org.jeyzer.monitor.engine.rule.MonitorSessionRule;
import org.jeyzer.monitor.engine.rule.condition.applicative.ApplicativeEventProvider;
import org.jeyzer.monitor.engine.rule.threshold.MonitorSessionThreshold;

import com.google.common.collect.Multimap;

public class SessionApplicativeThreshold extends MonitorSessionThreshold{

	public static final String THRESHOLD_NAME = "session applicative";
	
	public SessionApplicativeThreshold(ConfigMonitorThreshold thCfg) {
		super(thCfg, Scope.SESSION, MatchType.APPLICATIVE, SubLevel.DEFAULT_MEDIUM);
	}

	@Override
	// Must always be called after the ActionApplicativeThreshold, in order to handle the leftover task applicative events
	public void applyCandidacy(MonitorSessionRule rule, JzrMonitorSession session, Multimap<String, MonitorSessionEvent> events) {
		ApplicativeEventProvider eventProvider = (ApplicativeEventProvider) rule;
		
		MonitorSessionEvent event = null;
		
		for (ExternalEvent extEvent : eventProvider.getApplicativeEvents()) {
			if (extEvent.hasApplicativeRuleHit())
				continue; // manage the case action event got processed by action rule first
			
			String eventId = buildEventId(rule.getRef(), null, this.cfg.getLevel());
			
			MonitorEventInfo info = new ApplicativeEventInfo(
					eventId,
					rule.getRef() + this.getRef(),
					Scope.SESSION,
					this.cfg.getLevel(),
					this.subLevel,
					extEvent.getSnapshotStart(),
					extEvent.getSnapshotEnd(),
					extEvent.getMessage(), // instead of this.cfg.getMessage(),
					extEvent.getTicket(),
					extEvent.getId(),
					extEvent.getStart(),
					extEvent.getEnd(),
					extEvent.getThreadId(), // can be set if action not found
					extEvent.isOneshot()
					);
			event = rule.createSessionEvent(info, null);
			events.put(eventId, event);

			if (extEvent.getEnd() != null)
				event.setProgressStatus(new Date(0)); // force event closure
			else
				event.setProgressStatus(session.getEndDate()); // in progress	
		}
		
		for (ExternalEvent extEvent : eventProvider.getApplicativeEvents()) {
			// reset the hit as rules can be used again
			extEvent.resetHit();
		}
	}
	
	@Override
	public String getDisplayCondition() {
		return "See condition";
	}
}
