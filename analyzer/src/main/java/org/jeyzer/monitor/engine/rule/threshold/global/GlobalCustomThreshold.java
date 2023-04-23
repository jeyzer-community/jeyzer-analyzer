package org.jeyzer.monitor.engine.rule.threshold.global;

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
import org.jeyzer.monitor.engine.rule.condition.session.CustomSessionProvider;
import org.jeyzer.monitor.engine.rule.threshold.MonitorSessionCustomThreshold;

import com.google.common.collect.Multimap;

public class GlobalCustomThreshold extends MonitorSessionCustomThreshold{

	public static final String THRESHOLD_NAME = "global custom";
	
	public GlobalCustomThreshold(ConfigMonitorThreshold thCfg, SubLevel defaultSubLevel) {
		super(thCfg, Scope.GLOBAL, defaultSubLevel);
	}

	@Override
	public void applyCandidacy(MonitorSessionRule rule, JzrMonitorSession session, Multimap<String, MonitorSessionEvent> events) {
		CustomSessionProvider customProvider = (CustomSessionProvider) rule;

		MonitorSessionEvent event = null;
		
		for (ThreadDump dump : session.getDumps()){
			if (customProvider.matchCustomParameters(dump, this.cfg)){
				
				String eventId = buildEventId(rule.getRef(), null, this.cfg.getLevel());
					
				event = fetchPreviousUniqueEvent(events, eventId, dump.getTimestamp(), session.getThreadDumpPeriod());
					
				if (event == null){
					// create candidate
					MonitorEventInfo info = new MonitorEventInfo(
							eventId,
							rule.getRef() + this.getRef(),
							Scope.GLOBAL,
							this.cfg.getLevel(),
							this.subLevel,
							dump.getTimestamp(),
							dump.getTimestamp(),
							this.cfg.getMessage(),
							rule.getTicket()
							);
					event = rule.createSessionEvent(info, dump);
					events.put(eventId, event);
				}
				else{
					// Continuity of global session event
					// update the event end date
					event.updateEnd(dump.getTimestamp());
					event.updateCount();
				}

				updateContext(event, dump);
				event.setProgressStatus(session.getEndDate());			
			}
		}
	}

}
