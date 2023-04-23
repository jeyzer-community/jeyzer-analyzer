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
import org.jeyzer.analyzer.session.JzrMonitorSession;
import org.jeyzer.monitor.config.engine.ConfigMonitorThreshold;
import org.jeyzer.monitor.engine.event.MonitorTaskEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;
import org.jeyzer.monitor.engine.event.info.Scope;
import org.jeyzer.monitor.engine.event.info.SubLevel;
import org.jeyzer.monitor.engine.rule.MonitorTaskRule;
import org.jeyzer.monitor.engine.rule.condition.task.PatternTaskProvider;
import org.jeyzer.monitor.engine.rule.threshold.MonitorTaskThreshold;

import com.google.common.collect.Multimap;

public class StackPatternThreshold  extends MonitorTaskThreshold{

	public static final String THRESHOLD_NAME = "stack pattern";
	
	public StackPatternThreshold(ConfigMonitorThreshold thCfg, SubLevel defaultSubLevel) {
		super(thCfg, Scope.STACK, MatchType.PATTERN, defaultSubLevel);
	}

	@Override
	public void applyCandidacy(MonitorTaskRule rule, JzrMonitorSession session, ThreadAction action, Multimap<String, MonitorTaskEvent> events) {
		PatternTaskProvider patternProvider = (PatternTaskProvider) rule;

		MonitorTaskEvent event = null;
		
		int i = 0;
		while(i<action.size()){
			ThreadStack stack = action.getThreadStack(i);
			if (patternProvider.matchPattern(stack, this.cfg.getPattern())){
				
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
		return "Pattern = " + this.cfg.getPattern();
	}
	
}
