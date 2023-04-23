package org.jeyzer.monitor.engine.rule.threshold.system;

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




import org.jeyzer.analyzer.data.event.ExternalEvent;
import org.jeyzer.analyzer.session.JzrMonitorSession;
import org.jeyzer.monitor.config.engine.ConfigMonitorThreshold;
import org.jeyzer.monitor.engine.event.MonitorSystemEvent;
import org.jeyzer.monitor.engine.event.info.ApplicativeEventInfo;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;
import org.jeyzer.monitor.engine.event.info.Scope;
import org.jeyzer.monitor.engine.event.info.SubLevel;
import org.jeyzer.monitor.engine.rule.MonitorSystemRule;
import org.jeyzer.monitor.engine.rule.condition.applicative.ApplicativeEventProvider;
import org.jeyzer.monitor.engine.rule.threshold.MonitorSystemThreshold;

import com.google.common.collect.Multimap;

public class SystemApplicativeThreshold extends MonitorSystemThreshold{

	public static final String THRESHOLD_NAME = "system applicative";
	
	public SystemApplicativeThreshold(ConfigMonitorThreshold thCfg) {
		super(thCfg, Scope.SYSTEM, MatchType.APPLICATIVE, SubLevel.DEFAULT_MEDIUM);
	}

	@Override
	public void applyCandidacy(MonitorSystemRule rule, JzrMonitorSession session, Multimap<String, MonitorSystemEvent> events) {
		ApplicativeEventProvider eventProvider = (ApplicativeEventProvider) rule;
		
		MonitorSystemEvent event = null;
		
		for (ExternalEvent extEvent : eventProvider.getApplicativeEvents()) {
			
			String eventId = buildEventId(rule.getRef(), null, this.cfg.getLevel());
			
			// create candidate that will be automatically elected after
			MonitorEventInfo info = new ApplicativeEventInfo(
					eventId,
					rule.getRef() + this.getRef(),
					Scope.SYSTEM,
					this.cfg.getLevel(),
					this.subLevel,
					extEvent.getSnapshotStart(),
					session.getEndDate(),  // till the end
					extEvent.getMessage(), // instead of this.cfg.getMessage(),
					extEvent.getTicket(),
					extEvent.getId(),
					extEvent.getStart(),
					extEvent.getEnd(),
					extEvent.isOneshot()
					);
			event = rule.createSystemEvent(info);
			events.put(eventId, event);

			// will always be in progress
			event.setProgressStatus(session.getEndDate());
		}
	}

	@Override
	public String getDisplayCondition() {
		return "See condition";
	}

}
