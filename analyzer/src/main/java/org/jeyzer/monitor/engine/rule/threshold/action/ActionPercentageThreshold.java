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




import org.jeyzer.analyzer.data.ThreadAction;
import org.jeyzer.analyzer.data.stack.ThreadStack;
import org.jeyzer.analyzer.math.FormulaHelper;
import org.jeyzer.analyzer.session.JzrMonitorSession;
import org.jeyzer.monitor.config.engine.ConfigMonitorTaskThreshold;
import org.jeyzer.monitor.config.engine.ConfigMonitorThreshold;
import org.jeyzer.monitor.engine.event.MonitorTaskEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;
import org.jeyzer.monitor.engine.event.info.Scope;
import org.jeyzer.monitor.engine.event.info.SubLevel;
import org.jeyzer.monitor.engine.rule.MonitorTaskRule;
import org.jeyzer.monitor.engine.rule.threshold.MonitorTaskThreshold;

import com.google.common.collect.Multimap;

public abstract class ActionPercentageThreshold extends MonitorTaskThreshold{

	public ActionPercentageThreshold(ConfigMonitorThreshold thCfg, Scope scope, MatchType matchType, SubLevel defaultSubLevel) {
		super(thCfg, scope, matchType, defaultSubLevel);
	}

	@Override
	public void applyCandidacy(MonitorTaskRule rule, JzrMonitorSession session, ThreadAction action, Multimap<String, MonitorTaskEvent> events) {
		if (action.size() < this.getCount())
			return;
		
		// Create the event only once the percentage is known
		
		ThreadStack sampleStack = null;
		int i = 0;
		int hit = 0; 
		while(i<action.size()){
			ThreadStack stack = action.getThreadStack(i);
			if (matchCondition(rule, stack)){
				hit++;
				if (sampleStack == null)
					sampleStack = stack; 
			}
			i++;
		}
		
		int percent = FormulaHelper.percentRound(hit, action.size());
		if (percent < ((ConfigMonitorTaskThreshold)this.cfg).getPercentageInAction())
			return;
		
		MonitorTaskEvent event = null;
		// Add the threshold as rule suffix to distinguish from pure pattern thresholds 
		String eventId = buildEventId(rule.getRef(), buildSuffixId(action), this.cfg.getLevel());
		
		event = fetchPreviousEvent(events, eventId, action.getStartDate(), session.getThreadDumpPeriod());
			
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
					this.cfg.getMessage() + " Presence : " + percent + "%",
					rule.getTicket()
					);
			event = rule.createTaskEvent(info, action, sampleStack.getStackHandler());
			events.put(eventId, event);
			event.setProgressStatus(session.getEndDate());
			event.updateContext(sampleStack);
			event.forceTime(action.getMinDuration()); // set full time as percentage takes priority. Time is used as minimum limit here.
			// on consecutive monitoring sessions, the in-progress event end date will be updated
			// at the beginning of the next monitoring session. 
			// Thread id must be used to make the link.
			
			// Need to increase count of the event for each stack after the 1st one
			for (int j=1; j<action.size(); j++)
				event.updateCount();
		}else{
			for (int j=0; j<action.size(); j++)
				event.updateCount();
		}
	}
	
	protected abstract boolean matchCondition(MonitorTaskRule rule, ThreadStack stack);
	
}
