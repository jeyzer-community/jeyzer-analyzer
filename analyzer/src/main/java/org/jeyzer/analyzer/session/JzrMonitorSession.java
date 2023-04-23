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







import java.util.Date;
import java.util.List;
import java.util.Map;

import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.analyzer.data.TimeZoneInfo;
import org.jeyzer.analyzer.error.JzrMonitorException;
import org.jeyzer.monitor.config.applicative.MonitorApplicativeRuleFilter;
import org.jeyzer.monitor.engine.event.MonitorAnalyzerEvent;
import org.jeyzer.monitor.engine.event.MonitorSessionEvent;
import org.jeyzer.monitor.engine.event.MonitorSystemEvent;
import org.jeyzer.monitor.engine.event.MonitorTaskEvent;
import org.jeyzer.monitor.engine.rule.MonitorRule;
import org.jeyzer.monitor.sticker.Sticker;

import com.google.common.collect.Multimap;


public interface JzrMonitorSession {

	public Date getStartDate();
	
	public Date getEndDate();
	
	public TimeZoneInfo getDisplayTimeZoneInfo();
	
	public int getThreadDumpPeriod();
	
	public void applyMonitorStickers(List<? extends MonitorRule> rules, Map<String, Sticker> stickers);	
	
	public void applyMonitorTaskRules(List<org.jeyzer.monitor.engine.rule.MonitorTaskRule> rules, Multimap<String, MonitorTaskEvent> events, MonitorApplicativeRuleFilter appRuleFilter) throws JzrMonitorException;
	
	public void applyMonitorSessionRules(List<org.jeyzer.monitor.engine.rule.MonitorSessionRule> rules, Multimap<String, MonitorSessionEvent> events, MonitorApplicativeRuleFilter appRuleFilter, boolean includePublisherRules) throws JzrMonitorException;
	
	public void applyMonitorSystemRules(List<org.jeyzer.monitor.engine.rule.MonitorSystemRule> rules, Multimap<String, MonitorSystemEvent> events, MonitorApplicativeRuleFilter appRuleFilter) throws JzrMonitorException;
	
	public void applyMonitorAnalyzerRules(List<org.jeyzer.monitor.engine.rule.MonitorAnalyzerRule> rules, Multimap<String, MonitorAnalyzerEvent> events) throws JzrMonitorException;
	
	public List<ThreadDump> getDumps();
	
	public void close();
	
}
