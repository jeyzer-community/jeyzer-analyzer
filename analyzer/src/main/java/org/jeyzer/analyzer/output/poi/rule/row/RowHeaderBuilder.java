package org.jeyzer.analyzer.output.poi.rule.row;

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
import org.jeyzer.analyzer.session.JzrSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RowHeaderBuilder {
	
	private static final Logger logger = LoggerFactory.getLogger(RowHeaderBuilder.class);
	
	private static final RowHeaderBuilder builder = new RowHeaderBuilder();
	
	private RowHeaderBuilder(){
	}
	
	public static RowHeaderBuilder newInstance(){
		return builder;
	}

	public List<RowHeader> buildRowHeaders(ConfigSheetRowHeaders rowHeaderConfigSets, SheetDisplayContext displayContext, JzrSession session) throws JzrReportException {
		List<RowHeader> rules = new ArrayList<>();
		RowHeader rule;
		
	    for (ConfigDisplay headerCfg : rowHeaderConfigSets.getHeaderConfigs()){
	    	rule = null;

	    	if (ThreadIdRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new ThreadIdRule(headerCfg, displayContext);
	    	else if (ThreadNameRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new ThreadNameRule(headerCfg, displayContext);
	    	else if (ExecutorRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new ExecutorRule(headerCfg, displayContext);
	    	else if (ActionSizeRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new ActionSizeRule(headerCfg, displayContext);
	    	else if (ActionStackSizeRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new ActionStackSizeRule(headerCfg, displayContext);
	    	else if (ActionDurationRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new ActionDurationRule(headerCfg, displayContext);
	    	else if (ActionConsumedMemoryRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName())){
	    		if (session.isMemoryInfoAvailable())
	    			rule = new ActionConsumedMemoryRule(headerCfg, displayContext);
	    		// otherwise ignore
	    	}
	    	else if (FunctionPrincipalRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new FunctionPrincipalRule(headerCfg, displayContext);
	    	else if (OperationPrincipalRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new OperationPrincipalRule(headerCfg, displayContext);
	    	else if (ContentionTypePrincipalRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new ContentionTypePrincipalRule(headerCfg, displayContext);
	    	else if (ActionStartTimeRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new ActionStartTimeRule(headerCfg, displayContext);
	    	else if (LockStateRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new LockStateRule(headerCfg, displayContext);
	    	else if (ActionConsumedCPURule.RULE_NAME.equalsIgnoreCase(headerCfg.getName())){
	    		if (session.isCPUInfoAvailable())
	    			rule = new ActionConsumedCPURule(headerCfg, displayContext);
	    		// otherwise ignore
	    	}
	    	else if (FrozenCodeStateRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new FrozenCodeStateRule(headerCfg, displayContext);
	    	else if (CPURunnableRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new CPURunnableRule(headerCfg, displayContext);	    	
	    	else if (JeyzerMXUserRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName())){
	    		if (session.isJeyzerMXInfoAvailable())
	    			rule = new JeyzerMXUserRule(headerCfg, displayContext);
	    	}
		    else if (JeyzerMXActionStartTimeRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName())){
		    	if (session.isJeyzerMXInfoAvailable())
		    		rule = new JeyzerMXActionStartTimeRule(headerCfg, displayContext);
		    }
		    else if (JeyzerMXContextIdRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName())){
		    	if (session.isJeyzerMXInfoAvailable())
		    		rule = new JeyzerMXContextIdRule(headerCfg, displayContext);
		    }
		    else if (JeyzerMXActionPrincipalRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName())){
		    	if (session.isJeyzerMXInfoAvailable())
		    		rule = new JeyzerMXActionPrincipalRule(headerCfg, displayContext);
		    }
		    else if (JeyzerMXContextParamNumberRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName())){
		    	if (session.isJeyzerMXInfoAvailable())
		    		rule = new JeyzerMXContextParamNumberRule(headerCfg, displayContext);
		    }
		    else if (JeyzerMXContextParamStringRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName())){
		    	if (session.isJeyzerMXInfoAvailable())
		    		rule = new JeyzerMXContextParamStringRule(headerCfg, displayContext);
		    }
		    else if (JeyzerMXAllContextParamsRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName())){
		    	if (session.isJeyzerMXInfoAvailable())
		    		rule = new JeyzerMXAllContextParamsRule(headerCfg, displayContext);
		    }
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
