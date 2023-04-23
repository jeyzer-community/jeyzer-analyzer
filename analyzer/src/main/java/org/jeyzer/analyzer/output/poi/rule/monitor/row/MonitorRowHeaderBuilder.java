package org.jeyzer.analyzer.output.poi.rule.monitor.row;

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
import java.util.List;

import org.jeyzer.analyzer.config.report.ConfigDisplay;
import org.jeyzer.analyzer.config.report.headers.ConfigSheetRowHeaders;
import org.jeyzer.analyzer.output.poi.context.SheetDisplayContext;
import org.jeyzer.analyzer.session.JzrSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MonitorRowHeaderBuilder {
	
	private static final Logger logger = LoggerFactory.getLogger(MonitorRowHeaderBuilder.class);
	
	private static final MonitorRowHeaderBuilder builder = new MonitorRowHeaderBuilder();
	
	private MonitorRowHeaderBuilder(){
	}
	
	public static MonitorRowHeaderBuilder newInstance(){
		return builder;
	}

	public List<MonitorRowHeader> buildRowHeaders(ConfigSheetRowHeaders rowHeaderConfigSets, SheetDisplayContext displayContext, JzrSession session) {
		List<MonitorRowHeader> rules = new ArrayList<>();
		MonitorRowHeader rule;
		
	    for (ConfigDisplay headerCfg : rowHeaderConfigSets.getHeaderConfigs()){
	    	rule = null;

	    	if (EventIdRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new EventIdRule(headerCfg, displayContext);
	    	else if (EventNameRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new EventNameRule(headerCfg, displayContext);
	    	else if (EventRefRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new EventRefRule(headerCfg, displayContext);
	    	else if (EventLevelRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new EventLevelRule(headerCfg, displayContext);
	    	else if (EventSubLevelRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new EventSubLevelRule(headerCfg, displayContext);
	    	else if (EventRankRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new EventRankRule(headerCfg, displayContext);
	    	else if (EventStartDateRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new EventStartDateRule(headerCfg, displayContext);
	    	else if (EventScopeRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new EventScopeRule(headerCfg, displayContext);
	    	else if (EventSizeRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new EventSizeRule(headerCfg, displayContext);
	    	else if (EventDurationRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new EventDurationRule(headerCfg, displayContext);
	    	else
	    		logger.warn("Could not instanciate monitoring row header rule for configuration node : {}", headerCfg.getName());
	    	
	    	if (rule != null)
	    		rules.add(rule);
	    }
		
		return rules;
	}
}
