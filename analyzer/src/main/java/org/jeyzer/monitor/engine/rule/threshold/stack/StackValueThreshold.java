package org.jeyzer.monitor.engine.rule.threshold.stack;

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




import org.jeyzer.analyzer.data.ThreadAction;
import org.jeyzer.analyzer.data.stack.ThreadStack;
import org.jeyzer.analyzer.error.JzrInitializationException;
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

public class StackValueThreshold  extends MonitorTaskThreshold{

	public static final String THRESHOLD_NAME = "stack value";	
	
	public StackValueThreshold(ConfigMonitorThreshold thCfg, SubLevel defaultSubLevel) throws JzrInitializationException {
		super(thCfg, Scope.STACK, MatchType.VALUE, defaultSubLevel);
		if (!thCfg.isValueBound())
			throw new JzrInitializationException("Threshold " + THRESHOLD_NAME + " configuration is missing value.");
	}
	
	@Override
	public void applyCandidacy(MonitorTaskRule rule, JzrMonitorSession session, ThreadAction action, Multimap<String, MonitorTaskEvent> events) {
		ValueTaskProvider valueProvider = (ValueTaskProvider) rule;

		MonitorTaskEvent event = null;
		
		int i = 0;
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
							Scope.STACK,
							this.cfg.getLevel(),
							this.subLevel,
							stack.getTimeStamp(),
							stack.getTimeStamp(),
							this.cfg.getMessage(),
							rule.getTicket()
							);
					event = rule.createTaskEvent(info, action, stack.getStackHandler());
					events.put(eventId, event);
				}
				else{
					// Continuity of a previous session
					// update the event end date
					event.updateEnd(stack.getTimeStamp());
					event.updateCount();
				}

				event.updateContext(stack);
				event.setProgressStatus(session.getEndDate());
			}
			i++;
		}
	}
	
	@Override
	public String getDisplayCondition() {
		return "Value = " + this.cfg.getValue();
	}
	
	
}
