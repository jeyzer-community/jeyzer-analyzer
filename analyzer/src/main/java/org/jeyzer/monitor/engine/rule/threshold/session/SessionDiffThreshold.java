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
import org.jeyzer.monitor.engine.rule.condition.session.DiffSessionProvider;
import org.jeyzer.monitor.engine.rule.threshold.MonitorSessionThreshold;

import com.google.common.collect.Multimap;

public class SessionDiffThreshold  extends MonitorSessionThreshold{

	public static final String THRESHOLD_NAME = "session diff";		
	
	public SessionDiffThreshold(ConfigMonitorThreshold thCfg, SubLevel defaultSubLevel) {
		super(thCfg, Scope.SESSION, MatchType.DIFF, defaultSubLevel);
	}

	@Override
	public void applyCandidacy(MonitorSessionRule rule, JzrMonitorSession session, Multimap<String, MonitorSessionEvent> events) {
		DiffSessionProvider diffProvider = (DiffSessionProvider) rule;

		MonitorSessionEvent event = null;
		
		ThreadDump prevDump = null;
		for (ThreadDump dump : session.getDumps()){
			
			if (prevDump == null){
				prevDump = dump;
				continue; // First dump. Move to next dump : if not available, then no diff possible and therefore no diff event.
			}
			
			if (diffProvider.matchSignal(prevDump, dump, session.getThreadDumpPeriod())){
				
				String eventId = buildEventId(rule.getRef(), null, this.cfg.getLevel());
					
				event = fetchPreviousEvent(events, eventId, prevDump.getTimestamp(), session.getThreadDumpPeriod());
					
				if (event == null){
					// create candidate
					MonitorEventInfo info = new MonitorEventInfo(
							eventId,
							rule.getRef() + this.getRef(),
							Scope.SESSION,
							this.cfg.getLevel(),
							this.subLevel,
							prevDump.getTimestamp(),  // start
							dump.getTimestamp(),      // end
							this.cfg.getMessage(),
							rule.getTicket()
							);
					event = rule.createSessionEvent(info, prevDump);
					events.put(eventId, event);
				}
				else{
					// Continuity of a previous session
					// update the event end date
					event.updateEnd(dump.getTimestamp());
					event.updateCount();
				}

				event.updateContext(dump);
				event.setProgressStatus(session.getEndDate());
			}
			prevDump = dump;
		}
	}
	
	@Override
	public String getDisplayCondition() {
		return "See condition";
	}
	
}
