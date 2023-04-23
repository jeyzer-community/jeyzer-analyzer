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
import org.jeyzer.analyzer.data.stack.ThreadStack;
import org.jeyzer.analyzer.session.JzrMonitorSession;
import org.jeyzer.monitor.config.engine.ConfigMonitorThreshold;
import org.jeyzer.monitor.engine.event.MonitorTaskEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;
import org.jeyzer.monitor.engine.event.info.Scope;
import org.jeyzer.monitor.engine.event.info.SubLevel;
import org.jeyzer.monitor.engine.rule.MonitorTaskRule;
import org.jeyzer.monitor.engine.rule.condition.task.ValueTaskProvider;
import org.jeyzer.monitor.engine.rule.threshold.MonitorTaskThreshold;

import com.google.common.collect.Multimap;

public class ActionValueThreshold extends MonitorTaskThreshold {

	public static final String THRESHOLD_NAME = "action value";	
	
	public ActionValueThreshold(ConfigMonitorThreshold thCfg, SubLevel defaultSubLevel) {
		super(thCfg, Scope.ACTION, MatchType.VALUE, defaultSubLevel);
	}
	
	@Override
	public void applyCandidacy(MonitorTaskRule rule, JzrMonitorSession session, ThreadAction action, Multimap<String, MonitorTaskEvent> events) {
		ValueTaskProvider valueProvider = (ValueTaskProvider) rule;

		MonitorTaskEvent event = null;
		
		int i = 0;
		Date previous = null;
		while(i<action.size()){
			ThreadStack stack = action.getThreadStack(i);
			if (valueProvider.matchValue(stack, this.cfg.getValue())){
				
				String eventId = buildEventId(rule.getRef(), buildSuffixId(action), this.cfg.getLevel());
					
				event = fetchPreviousEvent(events, eventId, stack.getTimeStamp(), session.getThreadDumpPeriod());
					
				if (event == null){
					// create candidate
					MonitorEventInfo info = new MonitorEventInfo(
							eventId,
							rule.getRef() + this.getRef(),
							Scope.ACTION,
							this.cfg.getLevel(),
							this.subLevel,
							action.getStartDate(),
							action.getEndDate(),
							this.cfg.getMessage(),
							rule.getTicket()
							);
					event = rule.createTaskEvent(info, action, stack.getStackHandler());
					events.put(eventId, event);
					event.setProgressStatus(session.getEndDate());
				}else{
					event.updateCount();
					// in case of monitoring, take the end date of the event (which corresponds to the end date of the previous session).
					long previousTime = previous != null ? previous.getTime() : event.getEndDate().getTime();
					event.updateTime(stack.getTimeStamp().getTime() - previousTime);
				}
				
				event.updateContext(stack);
				
				// on consecutive monitoring sessions, the in-progress event end date will be updated
				// at the beginning of the next monitoring session. 
				// Thread id must be used to make the link.
			}
			previous = stack.getTimeStamp();
			i++;
		}
	}
	
	@Override
	public String getDisplayCondition() {
		return "Value = " + this.cfg.getValue();
	}

}
