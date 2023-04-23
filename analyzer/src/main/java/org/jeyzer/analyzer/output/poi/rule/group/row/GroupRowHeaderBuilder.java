package org.jeyzer.analyzer.output.poi.rule.group.row;

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
import org.jeyzer.analyzer.error.JzrReportException;
import org.jeyzer.analyzer.output.poi.context.SheetDisplayContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GroupRowHeaderBuilder {
	
	private static final Logger logger = LoggerFactory.getLogger(GroupRowHeaderBuilder.class);
	
	private static final GroupRowHeaderBuilder builder = new GroupRowHeaderBuilder();
	
	private GroupRowHeaderBuilder(){
	}
	
	public static GroupRowHeaderBuilder newInstance(){
		return builder;
	}

	public List<GroupRowHeader> buildRowHeaders(ConfigSheetRowHeaders rowHeaderConfigSets, SheetDisplayContext displayContext) throws JzrReportException {
		List<GroupRowHeader> rules = new ArrayList<>();
		GroupRowHeader rule;
		
	    for (ConfigDisplay headerCfg : rowHeaderConfigSets.getHeaderConfigs()){
	    	rule = null;

	    	if (GroupExecutorRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new GroupExecutorRule(headerCfg, displayContext);
	    	else if (GroupIdRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new GroupIdRule(headerCfg, displayContext);
	    	else if (GroupStacksSizeRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new GroupStacksSizeRule(headerCfg, displayContext);
	    	else if (GroupActionDurationRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new GroupActionDurationRule(headerCfg, displayContext);
	    	else if (GroupFunctionPrincipalRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new GroupFunctionPrincipalRule(headerCfg, displayContext);
	    	else if (GroupOperationPrincipalRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new GroupOperationPrincipalRule(headerCfg, displayContext);
	    	else if (GroupContentionTypePrincipalRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new GroupContentionTypePrincipalRule(headerCfg, displayContext);
	    	else if (GroupActionStartTimeRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new GroupActionStartTimeRule(headerCfg, displayContext);
	    	else if (GroupLockStateRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new GroupLockStateRule(headerCfg, displayContext);
	    	else
	    		logger.warn("Could not instanciate row header rule for configuration node : {}", headerCfg.getName());
	    	
	    	if (rule != null)
	    		rules.add(rule);
	    }
	    
	    if (rules.isEmpty())
	    	throw new JzrReportException("Failed to generate the JZR report : the row headers cannot be empty. Please review the JZR report configuration");
		
		return rules;
	}
}
