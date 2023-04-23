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




import org.jeyzer.analyzer.session.JzrMonitorSession;
import org.jeyzer.analyzer.session.JzrSession;
import org.jeyzer.monitor.config.engine.ConfigMonitorThreshold;
import org.jeyzer.monitor.engine.event.MonitorSystemEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;
import org.jeyzer.monitor.engine.event.info.Scope;
import org.jeyzer.monitor.engine.event.info.SubLevel;
import org.jeyzer.monitor.engine.rule.MonitorSystemRule;
import org.jeyzer.monitor.engine.rule.condition.system.ValueSystemProvider;
import org.jeyzer.monitor.engine.rule.threshold.MonitorSystemThreshold;

import com.google.common.collect.Multimap;

public class SystemValueThreshold extends MonitorSystemThreshold{

	public static final String THRESHOLD_NAME = "system value";	
	
	public SystemValueThreshold(ConfigMonitorThreshold thCfg, SubLevel defaultSubLevel) {
		super(thCfg, Scope.SYSTEM, MatchType.VALUE, defaultSubLevel);
	}

	@Override
	public void applyCandidacy(MonitorSystemRule rule, JzrMonitorSession session, Multimap<String, MonitorSystemEvent> events) {
		ValueSystemProvider valueProvider = (ValueSystemProvider) rule;

		MonitorSystemEvent event = null;
		JzrSession jzrSession = (JzrSession) session;
		
		if (valueProvider.matchValue(jzrSession, this.cfg.getValue())){
				
			String eventId = buildEventId(rule.getRef(), null, this.cfg.getLevel());
			
			event = fetchPreviousUniqueEvent(events, eventId, session.getThreadDumpPeriod());

			if (event == null){
				// create candidate that will be automatically elected afterwards
				MonitorEventInfo info = new MonitorEventInfo(
						eventId,
						rule.getRef() + this.getRef(),
						Scope.SYSTEM,
						this.cfg.getLevel(),
						this.subLevel,
						session.getStartDate(),
						session.getEndDate(),
						this.cfg.getMessage(),
						rule.getTicket()
						);
				event = rule.createSystemEvent(info);
				events.put(eventId, event);
			}
			else{
				event.updateEnd(jzrSession.getEndDate());
			}

			
			event.updateContext(jzrSession);
			// will always be in progress
			event.setProgressStatus(session.getEndDate());
		}
	}

	@Override
	public String getDisplayCondition() {
		return "Value = " + this.cfg.getValue();
	}

}
