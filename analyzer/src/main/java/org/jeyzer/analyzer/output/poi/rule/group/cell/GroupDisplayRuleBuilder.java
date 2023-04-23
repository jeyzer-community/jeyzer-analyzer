package org.jeyzer.analyzer.output.poi.rule.group.cell;

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
import org.jeyzer.analyzer.output.poi.rule.cell.ATBIOfInterestRule;
import org.jeyzer.analyzer.output.poi.rule.cell.AdvancedContentionTypeRule;
import org.jeyzer.analyzer.output.poi.rule.cell.AdvancedFunctionRule;
import org.jeyzer.analyzer.output.poi.rule.cell.AdvancedOperationRule;
import org.jeyzer.analyzer.output.poi.rule.cell.BiasedLockRule;
import org.jeyzer.analyzer.output.poi.rule.cell.ContentionTypeRule;
import org.jeyzer.analyzer.output.poi.rule.cell.DisplayRule;
import org.jeyzer.analyzer.output.poi.rule.cell.FunctionRule;
import org.jeyzer.analyzer.output.poi.rule.cell.LockStateRule;
import org.jeyzer.analyzer.output.poi.rule.cell.LongStackRule;
import org.jeyzer.analyzer.output.poi.rule.cell.LongTaskRule;
import org.jeyzer.analyzer.output.poi.rule.cell.OTBIOfInterestRule;
import org.jeyzer.analyzer.output.poi.rule.cell.OperationRule;
import org.jeyzer.analyzer.output.poi.rule.cell.StackRule;
import org.jeyzer.analyzer.output.poi.rule.cell.StackSizeRule;
import org.jeyzer.analyzer.output.poi.rule.cell.StateRule;
import org.jeyzer.analyzer.output.poi.rule.cell.TextWrapRule;
import org.jeyzer.analyzer.session.JzrSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GroupDisplayRuleBuilder {
	
	private static final Logger logger = LoggerFactory.getLogger(GroupDisplayRuleBuilder.class);
	
	private static final GroupDisplayRuleBuilder builder = new GroupDisplayRuleBuilder();
	
	private GroupDisplayRuleBuilder(){
	}
	
	public static GroupDisplayRuleBuilder newInstance(){
		return builder;
	}
	
	public List<DisplayRule> buildRules(List<ConfigDisplay> displayCfgs, SequenceSheetDisplayContext context, JzrSession session){
		List<DisplayRule> rules = new ArrayList<>();
		DisplayRule rule;

	    for (ConfigDisplay displayCfg : displayCfgs){
	    	rule = null;
	    	
	    	if (FunctionRule.RULE_NAME.equalsIgnoreCase(displayCfg.getName()))
    			rule = new FunctionRule(displayCfg, context);
	    	
	    	else if (LockStateRule.RULE_NAME.equalsIgnoreCase(displayCfg.getName()))
    			rule = new LockStateRule(displayCfg, context);

	    	else if (StateRule.RULE_NAME.equalsIgnoreCase(displayCfg.getName()))
    			rule = new StateRule(displayCfg, context);
	    	
	    	else if (LongTaskRule.RULE_NAME.equalsIgnoreCase(displayCfg.getName()))
    			rule = new LongTaskRule(displayCfg, context);

	    	else if (StackRule.RULE_NAME.equalsIgnoreCase(displayCfg.getName())){
	    		boolean optimized = false;
	    		if (context.getSetupManager().getReportOptimizeStacksThreshold() != -1)
	    			optimized = session.getActionsStackSize() > context.getSetupManager().getReportOptimizeStacksThreshold(); 
    			rule = new StackRule(displayCfg, context, optimized);
	    	}

	    	else if (OperationRule.RULE_NAME.equalsIgnoreCase(displayCfg.getName()))
    			rule = new OperationRule(displayCfg, context);
	    	
	    	else if (ContentionTypeRule.RULE_NAME.equalsIgnoreCase(displayCfg.getName()))
    			rule = new ContentionTypeRule(displayCfg, context);
	    	
	    	else if (AdvancedFunctionRule.RULE_NAME.equalsIgnoreCase(displayCfg.getName()))
    			rule = new AdvancedFunctionRule(displayCfg, context);

	    	else if (AdvancedOperationRule.RULE_NAME.equalsIgnoreCase(displayCfg.getName()))
    			rule = new AdvancedOperationRule(displayCfg, context);

	    	else if (AdvancedContentionTypeRule.RULE_NAME.equalsIgnoreCase(displayCfg.getName()))
    			rule = new AdvancedContentionTypeRule(displayCfg, context);

	    	else if (TextWrapRule.RULE_NAME.equalsIgnoreCase(displayCfg.getName()))
    			rule = new TextWrapRule(displayCfg, context);

	    	else if (BiasedLockRule.RULE_NAME.equalsIgnoreCase(displayCfg.getName())){
	    		if (session.isBiasedInfoAvailable())
	    			rule = new BiasedLockRule(displayCfg, context);
	    		// otherwise ignore
	    	}
	    	else if (ATBIOfInterestRule.RULE_NAME.equalsIgnoreCase(displayCfg.getName())){
    			rule = new ATBIOfInterestRule(displayCfg, context);
	    	}

	    	else if (OTBIOfInterestRule.RULE_NAME.equalsIgnoreCase(displayCfg.getName())){
    			rule = new OTBIOfInterestRule(displayCfg, context);
	    	}

	    	else if (StackSizeRule.RULE_NAME.equalsIgnoreCase(displayCfg.getName())){
    			rule = new StackSizeRule(displayCfg, context);
	    	}
	    	
	    	else if (LongStackRule.RULE_NAME.equalsIgnoreCase(displayCfg.getName())){
    			rule = new LongStackRule(displayCfg, context);
	    	}
	    	
	    	else if (GroupSizeRule.RULE_NAME.equalsIgnoreCase(displayCfg.getName())){
    			rule = new GroupSizeRule(displayCfg, context);
	    	}
	    	
	    	else
	    		logger.warn("Could not instanciate display rule for configuration node : {}", displayCfg.getName());
	    	

	    	if (rule != null)
	    		rules.add(rule);
	    }
		
		return rules;
	}

}
