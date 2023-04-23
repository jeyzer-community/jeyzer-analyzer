package org.jeyzer.analyzer.session;

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







import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jeyzer.analyzer.data.ThreadAction;
import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.analyzer.data.TimeZoneInfo;
import org.jeyzer.analyzer.error.JzrMonitorException;
import org.jeyzer.analyzer.util.SystemHelper;
import org.jeyzer.monitor.JeyzerMonitor;
import org.jeyzer.monitor.config.applicative.MonitorApplicativeRuleFilter;
import org.jeyzer.monitor.engine.event.MonitorAnalyzerEvent;
import org.jeyzer.monitor.engine.event.MonitorSessionEvent;
import org.jeyzer.monitor.engine.event.MonitorSystemEvent;
import org.jeyzer.monitor.engine.event.MonitorTaskEvent;
import org.jeyzer.monitor.engine.rule.MonitorAnalyzerRule;
import org.jeyzer.monitor.engine.rule.MonitorRule;
import org.jeyzer.monitor.engine.rule.MonitorSessionRule;
import org.jeyzer.monitor.engine.rule.MonitorSystemRule;
import org.jeyzer.monitor.engine.rule.MonitorTaskRule;
import org.jeyzer.monitor.sticker.Sticker;

import com.google.common.collect.Multimap;

public class JzrMonitorErrorSession implements JzrMonitorSession{
	
	private Date start;
	private Date end;
	private Exception ex;
	private TimeZoneInfo timeZoneInfo;
	
	// empty
	private List<ThreadDump> dumps = new ArrayList<>(0);
	private Map<Date,Set<ThreadAction>> actionHistory = new HashMap<>();
	
	public JzrMonitorErrorSession(Date start, Date end, Exception ex){
		this.start = (SystemHelper.getUnixEpochDate().equals(start)) ? end : start;
		this.end = end;
		this.ex = ex;
		this.timeZoneInfo = new TimeZoneInfo();
	}
	
	@Override
	public Date getStartDate(){
		return this.start;
	}

	@Override
	public Date getEndDate(){
		return this.end;
	}
	
	@Override
	public int getThreadDumpPeriod(){
		return -1; 
	}

	public Exception getException(){
		return this.ex;
	}
	
	/**
	 * 
	 * @return empty list
	 */
	@Override
	public List<ThreadDump> getDumps(){
		return this.dumps;
	}
	
	/**
	 * 
	 * @return empty map
	 */
	public Map<Date,Set<ThreadAction>> getActionHistory(){
		return actionHistory;
	}
	
	@Override
	public void applyMonitorStickers(List<? extends MonitorRule> rules, Map<String, Sticker> stickers) {
		// do nothing
	}

	@Override
	public void applyMonitorTaskRules(List<MonitorTaskRule> rules, Multimap<String, MonitorTaskEvent> events, MonitorApplicativeRuleFilter appRuleFilter) throws JzrMonitorException {
		// do nothing
	}

	@Override
	public void applyMonitorSystemRules(List<MonitorSystemRule> rules, Multimap<String, MonitorSystemEvent> events, MonitorApplicativeRuleFilter appRuleFilter) throws JzrMonitorException {
		// do nothing
	}
	
	@Override
	public void applyMonitorSessionRules(List<MonitorSessionRule> rules, Multimap<String, MonitorSessionEvent> events, MonitorApplicativeRuleFilter appRuleFilter, boolean includePublisherRules) throws JzrMonitorException {
		JeyzerMonitor.logger.info("Applying Monitoring session rules");
		
		for (MonitorSessionRule sessionRule : rules){
			// apply rules to ourselves
			sessionRule.applyCandidacy(this, events);
		}
	}

	@Override
	public void applyMonitorAnalyzerRules(List<MonitorAnalyzerRule> rules, Multimap<String, MonitorAnalyzerEvent> events) throws JzrMonitorException {
		for (MonitorAnalyzerRule rule : rules){
			// apply rules to ourselves
			rule.applyCandidacy(this, events);
		}
		JeyzerMonitor.logger.info("Number of candidate analyzer monitoring events : " + events.size());
		
		// 2 - apply elected events
		for (MonitorAnalyzerRule rule : rules){
			// apply rules to ourselves
			rule.applyElection(this, events);
		}
		
	}

	@Override
	public void close() {
		// do nothing
	}

	@Override
	public TimeZoneInfo getDisplayTimeZoneInfo() {
		return timeZoneInfo;
	}
	
}
