package org.jeyzer.monitor.engine.rule.threshold.analyzer;

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
import org.jeyzer.monitor.config.engine.ConfigMonitorAnalyzerThreshold;
import org.jeyzer.monitor.engine.event.MonitorAnalyzerEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;
import org.jeyzer.monitor.engine.event.info.Scope;
import org.jeyzer.monitor.engine.event.info.SubLevel;
import org.jeyzer.monitor.engine.rule.MonitorAnalyzerRule;
import org.jeyzer.monitor.engine.rule.condition.analyzer.SignalAnalyzerProvider;
import org.jeyzer.monitor.engine.rule.threshold.MonitorThreshold;

import com.google.common.collect.Multimap;

public class AnalyzerExceptionThreshold extends MonitorThreshold{

	public AnalyzerExceptionThreshold(ConfigMonitorAnalyzerThreshold thCfg, Scope scope, SubLevel defaultSubLevel) {
		super(thCfg, scope, MatchType.EXCEPTION, defaultSubLevel);
	}
	
	public void applyCandidacy(MonitorAnalyzerRule rule, JzrMonitorSession session, Multimap<String, MonitorAnalyzerEvent> events) {
		SignalAnalyzerProvider provider = (SignalAnalyzerProvider) rule;
		
		if (provider.matchSignal(session)){
			String eventId = this.buildEventId(rule.getRef(), null, this.cfg.getLevel());
			
			MonitorEventInfo info = new MonitorEventInfo(
					eventId,
					rule.getRef() + this.getRef(),
					this.getScope(),
					this.getLevel(),
					this.getSubLevel(),
					session.getStartDate(),
					session.getEndDate(),
					this.cfg.getMessage(),
					null
					);
			MonitorAnalyzerEvent event = rule.createAnalyzerEvent(info);
			
			events.put(eventId, event);
			event.setProgressStatus(session.getEndDate());
		}
	}

	public void applyElection(MonitorAnalyzerRule rule, Multimap<String, MonitorAnalyzerEvent> events) {
		String eventId = buildEventId(rule.getRef(), null, this.cfg.getLevel());
		
		// always elect
		for (MonitorAnalyzerEvent event : events.get(eventId)){
			event.elect();
			hit();
		}
	}
	
	@Override
	public String getDisplayCondition() {
		return "Exception catched";
	}

}
