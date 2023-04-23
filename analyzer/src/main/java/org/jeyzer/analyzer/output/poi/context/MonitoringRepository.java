package org.jeyzer.analyzer.output.poi.context;

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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jeyzer.analyzer.config.report.ConfigMonitoringSheet;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.analyzer.session.JzrSession;
import org.jeyzer.analyzer.setup.MonitorSetupManager;
import org.jeyzer.monitor.config.ConfigMonitor;
import org.jeyzer.monitor.config.sticker.ConfigStickers;
import org.jeyzer.monitor.engine.event.MonitorSessionEvent;
import org.jeyzer.monitor.engine.event.MonitorSystemEvent;
import org.jeyzer.monitor.engine.event.MonitorTaskEvent;
import org.jeyzer.monitor.engine.rule.MonitorRule;
import org.jeyzer.monitor.engine.rule.MonitorSessionRule;
import org.jeyzer.monitor.engine.rule.MonitorSystemRule;
import org.jeyzer.monitor.engine.rule.MonitorTaskRule;
import org.jeyzer.monitor.impl.rule.MonitorRuleBuilder;
import org.jeyzer.monitor.impl.rule.MonitorRuleBuilder.RuleScope;
import org.jeyzer.monitor.sticker.Sticker;
import org.jeyzer.monitor.sticker.StickerBuilder;
import org.jeyzer.service.location.JzrLocationResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

public class MonitoringRepository {
	
	protected static final Logger logger = LoggerFactory.getLogger(MonitoringRepository.class);

	private static final String TASK_SUFFIX = "@Task";
	private static final String SESSION_SUFFIX = "@Session";
	private static final String SYSTEM_SUFFIX = "@System";
	
	private Map<String, Sticker> stickers;
	private List<Sticker> stickerList;
	
	// Key is set of rule groups 
	private Map<String, List<MonitorTaskRule>>    taskRules = new HashMap<>();
	private Map<String, List<MonitorSessionRule>> sessionRules = new HashMap<>();
	private Map<String, List<MonitorSystemRule>>  systemRules = new HashMap<>();
	private Multimap<String, String> sheetNames =  LinkedListMultimap.create();
	
	private Map<String, Multimap<String, MonitorTaskEvent>>    taskEvents = new HashMap<>();
	private Map<String, Multimap<String, MonitorSessionEvent>> sessionEvents = new HashMap<>();
	private Map<String, Multimap<String, MonitorSystemEvent>>  systemEvents = new HashMap<>();
	
	public Map<String, Sticker> getStickers(ConfigStickers stickersCfg, JzrSession session, JzrLocationResolver jzrLocationResolver, MonitorSetupManager monitorSetupManager){
		if (stickers == null){
			if (stickersCfg != null){
				StickerBuilder stickerBuilder = new StickerBuilder(stickersCfg);
				stickerBuilder.addStickers(monitorSetupManager.getStickers(session, jzrLocationResolver));
				stickerBuilder.loadDynamicStickers(session, jzrLocationResolver);
				stickers = stickerBuilder.getStickers();
			}else{
				stickers = monitorSetupManager.getStickers(session, jzrLocationResolver);
			}
		}
		
		return stickers;
	}
	
	public List<Sticker> getStickerList(ConfigStickers stickersCfg, JzrSession session, JzrLocationResolver jzrLocationResolver, MonitorSetupManager monitorSetupManager){
		if (stickerList == null){
			if (stickersCfg != null){
				StickerBuilder stickerBuilder = new StickerBuilder(stickersCfg);
				stickerBuilder.addStickerList(monitorSetupManager.getStickerList(session, jzrLocationResolver));
				stickerBuilder.loadDynamicStickers(session, jzrLocationResolver);
				stickerList = stickerBuilder.getStickerList();
			}else{
				stickerList = monitorSetupManager.getStickerList(session, jzrLocationResolver);
			}
		}
		
		return stickerList;
	}
	
	public List<MonitorTaskRule> getTaskRules(ConfigMonitoringSheet cfg, JzrSession session) throws JzrInitializationException{
		String ruleSetId = cfg.getConfigMonitorRules().getId();
		
		List<MonitorTaskRule> rules = this.taskRules.get(ruleSetId + TASK_SUFFIX);
		if (rules == null){
			// load the monitoring rules
			ConfigMonitor.setDateFormat(cfg.getDateFormat());
			MonitorRuleBuilder ruleBuilder = new MonitorRuleBuilder(cfg.getConfigMonitorRules(), RuleScope.TASK);
			ruleBuilder.loadDynamicRules(session, cfg.getJzrLocationResolver(), RuleScope.TASK);
			rules = ruleBuilder.getTaskRules();
			this.taskRules.put(ruleSetId + TASK_SUFFIX, rules);
		}
		
		this.sheetNames.put(ruleSetId + TASK_SUFFIX, cfg.getName());
		
		// Keep the original list intact. Permits to not being impacted by the applicative rules that may be added to the returned list.
		return new ArrayList<MonitorTaskRule>(rules);
	}
	
	public List<MonitorSessionRule> getSessionRules(ConfigMonitoringSheet cfg, JzrSession session) throws JzrInitializationException{
		String ruleSetId = cfg.getConfigMonitorRules().getId();
		
		List<MonitorSessionRule> rules = this.sessionRules.get(ruleSetId + SESSION_SUFFIX);
		if (rules == null){
			// load the monitoring rules
			ConfigMonitor.setDateFormat(cfg.getDateFormat());
			MonitorRuleBuilder ruleBuilder = new MonitorRuleBuilder(cfg.getConfigMonitorRules(), RuleScope.SESSION);
			ruleBuilder.loadDynamicRules(session, cfg.getJzrLocationResolver(), RuleScope.SESSION);
			rules = ruleBuilder.getSessionRules();
			this.sessionRules.put(ruleSetId + SESSION_SUFFIX, rules);
		}
		
		this.sheetNames.put(ruleSetId + SESSION_SUFFIX, cfg.getName());
		
		// Keep the original list intact. Permits to not being impacted by the applicative rules that may be added to the returned list.
		return new ArrayList<MonitorSessionRule>(rules);
	}
	
	public List<MonitorSystemRule> getSystemRules(ConfigMonitoringSheet cfg, JzrSession session) throws JzrInitializationException{
		String ruleSetId = cfg.getConfigMonitorRules().getId();
		
		List<MonitorSystemRule> rules = this.systemRules.get(ruleSetId + SYSTEM_SUFFIX);
		if (rules == null){
			// load the monitoring rules
			ConfigMonitor.setDateFormat(cfg.getDateFormat());
			MonitorRuleBuilder ruleBuilder = new MonitorRuleBuilder(cfg.getConfigMonitorRules(), RuleScope.SYSTEM);
			ruleBuilder.loadDynamicRules(session, cfg.getJzrLocationResolver(), RuleScope.SYSTEM);
			rules = ruleBuilder.getSystemRules();
			this.systemRules.put(ruleSetId + SYSTEM_SUFFIX, rules);
		}
		
		this.sheetNames.put(ruleSetId + SYSTEM_SUFFIX, cfg.getName());
		
		// Keep the original list intact. Permits to not being impacted by the applicative rules that may be added to the returned list.
		return new ArrayList<MonitorSystemRule>(rules);
	}

	public List<MonitorRule> getRules(String ruleSetId){
		if (ruleSetId.endsWith(TASK_SUFFIX))
			return new ArrayList<MonitorRule>(this.taskRules.get(ruleSetId));
		else if (ruleSetId.endsWith(SESSION_SUFFIX))
			return new ArrayList<MonitorRule>(this.sessionRules.get(ruleSetId));
		else if (ruleSetId.endsWith(SYSTEM_SUFFIX))
			return new ArrayList<MonitorRule>(this.systemRules.get(ruleSetId));

		logger.warn("Rules not found for the rule set : " + ruleSetId);
		return null;
	}
	
	public Collection<String> getSheetNames(String ruleSetId){
		return this.sheetNames.get(ruleSetId);
	}	
	
	public Set<String> getRuleSetIds(){
		return this.sheetNames.keySet();
	}

	public Multimap<String, MonitorTaskEvent> getTaskEvents(ConfigMonitoringSheet monitoringConfig) {
		return this.taskEvents.get(monitoringConfig.getConfigMonitorRules().getId());
	}
	
	public void addTaskEvents(ConfigMonitoringSheet monitoringConfig, Multimap<String, MonitorTaskEvent> events){
		this.taskEvents.put(monitoringConfig.getConfigMonitorRules().getId(), events);
	}
	
	public Multimap<String, MonitorSessionEvent> getSessionEvents(ConfigMonitoringSheet monitoringConfig) {
		return this.sessionEvents.get(monitoringConfig.getConfigMonitorRules().getId());
	}
	
	public void addSessionEvents(ConfigMonitoringSheet monitoringConfig, Multimap<String, MonitorSessionEvent> events){
		this.sessionEvents.put(monitoringConfig.getConfigMonitorRules().getId(), events);
	}
	
	public Multimap<String, MonitorSystemEvent> getSystemEvents(ConfigMonitoringSheet monitoringConfig) {
		return this.systemEvents.get(monitoringConfig.getConfigMonitorRules().getId());
	}
	
	public void addSystemEvents(ConfigMonitoringSheet monitoringConfig, Multimap<String, MonitorSystemEvent> events){
		this.systemEvents.put(monitoringConfig.getConfigMonitorRules().getId(), events);
	}
}
