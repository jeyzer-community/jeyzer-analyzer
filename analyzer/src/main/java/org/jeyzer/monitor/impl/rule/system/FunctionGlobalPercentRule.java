package org.jeyzer.monitor.impl.rule.system;

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



import static org.jeyzer.monitor.config.engine.ConfigMonitorThreshold.THRESHOLD_SYSTEM_VALUE;




import java.util.Arrays;
import java.util.List;

import org.jeyzer.analyzer.data.tag.FunctionTag;
import org.jeyzer.analyzer.data.tag.Tag;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.analyzer.math.FormulaHelper;
import org.jeyzer.analyzer.session.JzrSession;
import org.jeyzer.monitor.config.engine.ConfigMonitorRule;
import org.jeyzer.monitor.config.engine.ConfigParamMonitorRule;
import org.jeyzer.monitor.engine.event.MonitorSystemEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;
import org.jeyzer.monitor.engine.event.info.SubLevel;
import org.jeyzer.monitor.engine.rule.MonitorSystemRule;
import org.jeyzer.monitor.engine.rule.condition.system.ValueSystemProvider;
import org.jeyzer.monitor.impl.event.system.FunctionGlobalPercentEvent;
import org.jeyzer.monitor.util.Operator;

import com.google.common.collect.Multiset;

public class FunctionGlobalPercentRule extends MonitorSystemRule implements ValueSystemProvider{

	public static final String RULE_NAME = "Function global percentage";
	
	public static final String RULE_NARRATIVE = "The " + RULE_NAME + " rule permits to detect any high applicative activity based on functions. "
			+ "It is useful to track intensive activities based on function names which are known as problematic.";
	
	private static final int TRIGGER_LIMIT = 50;
	
	private String function;
	private String paramDisplay;
	private Operator operator;
	
	public FunctionGlobalPercentRule(ConfigMonitorRule def) throws JzrInitializationException {
		super(def, RULE_NAME + " : " + ((ConfigParamMonitorRule)def).getDisplayName(), 
				Operator.getEventDescription((ConfigParamMonitorRule) def));
		ConfigParamMonitorRule mxDef = (ConfigParamMonitorRule) def;
		this.function = mxDef.getParamName();
		this.paramDisplay = mxDef.getDisplayName();
		this.operator = mxDef.getOperator();
	}

	@Override
	public boolean isAdvancedMonitoringBased() {
		return false;
	}
	
	@Override
	protected List<String> getSupportedThresholdTypes(){
		return Arrays.asList(THRESHOLD_SYSTEM_VALUE);
	}

	@Override
	public boolean matchValue(JzrSession session, long value) {
		Multiset<Tag> functionTags = session.getFunctionSet();
		Tag functionTag = new FunctionTag(function);
    	int tagCount = functionTags.count(functionTag);
    	if (tagCount == 0)
    		return false;
    	
    	// get percentage
		// total number of active stacks
    	int globalActionsStackSize = session.getActionsStackSize();
    	// Set must be representative	
    	if (globalActionsStackSize < TRIGGER_LIMIT)
    		return false;
    	
    	int tagPercent = FormulaHelper.percentRound(tagCount, globalActionsStackSize);
    	
		switch(operator){
		case LOWER_OR_EQUAL:
			return tagPercent <= value;
		default:
			return tagPercent >= value;
		}
	}

	@Override
	public MonitorSystemEvent createSystemEvent(MonitorEventInfo info) {
		return new FunctionGlobalPercentEvent(
				this.paramDisplay,
				info,
				this.function,
				this.operator
			);
	}
	
	@Override
	public SubLevel getDefaultSubLevel() {
		return SubLevel.DEFAULT_LOW;
	}
	
	@Override
	public String getDefaultNarrative() {
		return RULE_NARRATIVE;
	}
}
