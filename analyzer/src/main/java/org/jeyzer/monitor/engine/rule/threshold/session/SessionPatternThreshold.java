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




import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.analyzer.session.JzrMonitorSession;
import org.jeyzer.monitor.config.engine.ConfigMonitorThreshold;
import org.jeyzer.monitor.engine.event.MonitorSessionEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;
import org.jeyzer.monitor.engine.event.info.Scope;
import org.jeyzer.monitor.engine.event.info.SubLevel;
import org.jeyzer.monitor.engine.rule.MonitorSessionRule;
import org.jeyzer.monitor.engine.rule.condition.session.PatternSessionProvider;
import org.jeyzer.monitor.engine.rule.threshold.MonitorSessionThreshold;

import com.google.common.collect.Multimap;

public class SessionPatternThreshold extends MonitorSessionThreshold{

	public static final String THRESHOLD_NAME = "session pattern";
	
	public SessionPatternThreshold(ConfigMonitorThreshold thCfg, SubLevel defaultSubLevel) {
		super(thCfg, Scope.SESSION, MatchType.PATTERN, defaultSubLevel);
	}

	@Override
	public void applyCandidacy(MonitorSessionRule rule, JzrMonitorSession session, Multimap<String, MonitorSessionEvent> events) {
		PatternSessionProvider patternProvider = (PatternSessionProvider) rule;

		MonitorSessionEvent event = null;
		
		for (ThreadDump td : session.getDumps()){
			if (patternProvider.matchPattern(td, this.cfg.getPattern())){
				
				String eventId = buildEventId(rule.getRef(), null, this.cfg.getLevel());
					
				event = fetchPreviousEvent(events, eventId, td.getTimestamp(), session.getThreadDumpPeriod());
					
				if (event == null){
					// create candidate
					MonitorEventInfo info = new MonitorEventInfo(
							eventId,
							rule.getRef() + this.getRef(),
							Scope.SESSION,
							this.cfg.getLevel(),
							this.subLevel,
							td.getTimestamp(),
							td.getTimestamp(),
							this.cfg.getMessage(),
							rule.getTicket()
							);
					event = rule.createSessionEvent(info, td);
					events.put(eventId, event);
				}
				else{
					// Continuity of a previous session
					// update the event end date
					event.updateEnd(td.getTimestamp());
					event.updateCount();
				}

				event.updateContext(td);
				event.setProgressStatus(session.getEndDate());
			}
		}
	}
	
	@Override
	public String getDisplayCondition() {
		return "Pattern = " + this.cfg.getPattern();
	}
	
}
