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

import org.jeyzer.analyzer.data.event.JzrPublisherEvent;
import org.jeyzer.analyzer.session.JzrMonitorSession;
import org.jeyzer.monitor.config.engine.ConfigMonitorThreshold;
import org.jeyzer.monitor.engine.event.MonitorSessionEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;
import org.jeyzer.monitor.engine.event.info.PublisherEventInfo;
import org.jeyzer.monitor.engine.event.info.Scope;
import org.jeyzer.monitor.engine.event.info.SubLevel;
import org.jeyzer.monitor.engine.rule.MonitorSessionRule;
import org.jeyzer.monitor.engine.rule.condition.applicative.PublisherEventProvider;
import org.jeyzer.monitor.engine.rule.threshold.MonitorSessionThreshold;

import com.google.common.collect.Multimap;

public class SessionPublisherThreshold extends MonitorSessionThreshold{

	public static final String THRESHOLD_NAME = "session publisher";
	
	public SessionPublisherThreshold(ConfigMonitorThreshold thCfg) {
		super(thCfg, Scope.SESSION, MatchType.PUBLISHER, SubLevel.DEFAULT_MEDIUM);
	}

	@Override
	public void applyCandidacy(MonitorSessionRule rule, JzrMonitorSession session, Multimap<String, MonitorSessionEvent> events) {
		PublisherEventProvider eventProvider = (PublisherEventProvider) rule;
		
		MonitorSessionEvent event = null;
		
		for (JzrPublisherEvent pubEvent : eventProvider.getPublisherEvents()) {
			String eventId = buildEventId(rule.getRef(), null, this.cfg.getLevel());
			
			MonitorEventInfo info = new PublisherEventInfo(
					eventId,
					rule.getRef() + this.getRef(),
					Scope.SESSION,
					this.cfg.getLevel(),
					this.subLevel,
					pubEvent.getSnapshotStart(),
					pubEvent.getSnapshotEnd(),
					this.cfg.getMessage(),
					pubEvent.getStart()
					);
			event = rule.createSessionEvent(info, null);
			events.put(eventId, event);

			event.setProgressStatus(new Date(0)); // force event closure
		}
	}
	
	@Override
	public String getDisplayCondition() {
		return "See condition";
	}
}
