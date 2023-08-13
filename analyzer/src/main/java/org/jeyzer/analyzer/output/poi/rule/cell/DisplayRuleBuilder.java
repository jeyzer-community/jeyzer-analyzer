package org.jeyzer.analyzer.output.poi.rule.cell;

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
import org.jeyzer.analyzer.session.JzrSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DisplayRuleBuilder {
	
	private static final Logger logger = LoggerFactory.getLogger(DisplayRuleBuilder.class);
	
	private static final DisplayRuleBuilder builder = new DisplayRuleBuilder();
	
	private DisplayRuleBuilder(){
	}
	
	public static DisplayRuleBuilder newInstance(){
		return builder;
	}
	
	public List<DisplayRule> buildRules(List<ConfigDisplay> displayCfgs, SequenceSheetDisplayContext context, JzrSession session){
		List<DisplayRule> rules = new ArrayList<>();
		DisplayRule rule;

		if (session.hasVirtualThreads()) {
			// Handle virtual threads in transparent way
			//  Choice is made to always display the number of virtual threads up front
			//  Not done for GroupDisplayRuleBuilder as it is handled as part of the group feature
			rule = new VirtualThreadCountRule(new ConfigDisplay("Virtual thread count"), context);
			rules.add(rule);
		}
		
	    for (ConfigDisplay displayCfg : displayCfgs){
	    	rule = null;
	    	
	    	if (FunctionRule.RULE_NAME.equalsIgnoreCase(displayCfg.getName()))
    			rule = new FunctionRule(displayCfg, context);
	    	
	    	else if (LockStateRule.RULE_NAME.equalsIgnoreCase(displayCfg.getName()))
    			rule = new LockStateRule(displayCfg, context);

	    	else if (StateRule.RULE_NAME.equalsIgnoreCase(displayCfg.getName()))
    			rule = new StateRule(displayCfg, context);

	    	else if (CPUDetailsRule.RULE_NAME.equalsIgnoreCase(displayCfg.getName()))
    			rule = new CPUDetailsRule(displayCfg, context);

	    	else if (CPUUsageRule.RULE_NAME.equalsIgnoreCase(displayCfg.getName()))
    			rule = new CPUUsageRule(displayCfg, context);

	    	else if (SysUsageRule.RULE_NAME.equalsIgnoreCase(displayCfg.getName()))
    			rule = new SysUsageRule(displayCfg, context);

	    	else if (UsrUsageRule.RULE_NAME.equalsIgnoreCase(displayCfg.getName()))
    			rule = new UsrUsageRule(displayCfg, context);
	    	
	    	else if (ApplicativeActivityUsageRule.RULE_NAME.equalsIgnoreCase(displayCfg.getName()))
    			rule = new ApplicativeActivityUsageRule(displayCfg, context);
	    	
	    	else if (LongTaskRule.RULE_NAME.equalsIgnoreCase(displayCfg.getName()))
    			rule = new LongTaskRule(displayCfg, context);

	    	else if (StackRule.RULE_NAME.equalsIgnoreCase(displayCfg.getName())){
	    		boolean optimized = false;
	    		if (context.getSetupManager().getReportOptimizeStacksThreshold() != -1)
	    			optimized = session.getActionsStackSize(false) > context.getSetupManager().getReportOptimizeStacksThreshold(); 
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

	    	else if (FrozenCodeStateRule.RULE_NAME.equalsIgnoreCase(displayCfg.getName()))
    			rule = new FrozenCodeStateRule(displayCfg, context);
	    	
	    	else if (TextWrapRule.RULE_NAME.equalsIgnoreCase(displayCfg.getName()))
    			rule = new TextWrapRule(displayCfg, context);

	    	else if (ConsumedMemoryRule.RULE_NAME.equalsIgnoreCase(displayCfg.getName()))
    			rule = new ConsumedMemoryRule(displayCfg, context);
	    	
	    	else if (ApplicativeMemoryActivityRule.RULE_NAME.equalsIgnoreCase(displayCfg.getName()))
    			rule = new ApplicativeMemoryActivityRule(displayCfg, context);

	    	else if (BiasedLockRule.RULE_NAME.equalsIgnoreCase(displayCfg.getName())){
	    		if (session.isBiasedInfoAvailable())
	    			rule = new BiasedLockRule(displayCfg, context);
	    		// otherwise ignore
	    	}

	    	else if (JeyzerMXAllContextParamsRule.RULE_NAME.equalsIgnoreCase(displayCfg.getName())){
	    		if (session.isJeyzerMXInfoAvailable())
	    			rule = new JeyzerMXAllContextParamsRule(displayCfg, context);
	    	}

	    	else if (JeyzerMXContextParamNumberRule.RULE_NAME.equalsIgnoreCase(displayCfg.getName())){
	    		if (session.isJeyzerMXInfoAvailable())
	    			rule = new JeyzerMXContextParamNumberRule(displayCfg, context);
	    	}
	    	
	    	else if (JeyzerMXContextParamStringRule.RULE_NAME.equalsIgnoreCase(displayCfg.getName())){
	    		if (session.isJeyzerMXInfoAvailable())
	    			rule = new JeyzerMXContextParamStringRule(displayCfg, context);
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
	    	
	    	else
	    		logger.warn("Could not instanciate display rule for configuration node : {}", displayCfg.getName());
	    	

	    	if (rule != null)
	    		rules.add(rule);
	    }
		
		return rules;
	}

}
