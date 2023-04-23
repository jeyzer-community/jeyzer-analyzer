package org.jeyzer.analyzer.output.poi.rule.monitor.cell;

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
import org.jeyzer.analyzer.output.poi.context.SequenceSheetDisplayContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MonitorDisplayRuleBuilder {
	
	private static final Logger logger = LoggerFactory.getLogger(MonitorDisplayRuleBuilder.class);
	
	private static final MonitorDisplayRuleBuilder builder = new MonitorDisplayRuleBuilder();
	
	private MonitorDisplayRuleBuilder(){
	}
	
	public static MonitorDisplayRuleBuilder newInstance(){
		return builder;
	}
	
	public List<MonitorDisplayRule> buildRules(List<ConfigDisplay> displayCfgs, SequenceSheetDisplayContext context){
		List<MonitorDisplayRule> rules = new ArrayList<>();
		MonitorDisplayRule rule;

	    for (ConfigDisplay displayCfg : displayCfgs){
	    	rule = null;
	    	
	    	if (EventNameRule.RULE_NAME.equalsIgnoreCase(displayCfg.getName()))
    			rule = new EventNameRule(displayCfg, context);
	    	else if (EventMessageRule.RULE_NAME.equalsIgnoreCase(displayCfg.getName()))
    			rule = new EventMessageRule(displayCfg, context);
	    	else if (EventLevelRule.RULE_NAME.equalsIgnoreCase(displayCfg.getName()))
    			rule = new EventLevelRule(displayCfg, context);
	    	else if (EventLinkRule.RULE_NAME.equalsIgnoreCase(displayCfg.getName()))
    			rule = new EventLinkRule(displayCfg, context);
	    	else
	    		logger.warn("Could not instanciate monitoring display rule for configuration node : {}", displayCfg.getName());
	    	

	    	if (rule != null)
	    		rules.add(rule);
	    }
		
		return rules;
	}

}
