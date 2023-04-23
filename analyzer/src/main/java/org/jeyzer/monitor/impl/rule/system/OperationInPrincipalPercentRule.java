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
import java.util.Collection;
import java.util.List;

import org.jeyzer.analyzer.data.ThreadAction;
import org.jeyzer.analyzer.data.tag.OperationTag;
import org.jeyzer.analyzer.data.tag.Tag;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.analyzer.math.FormulaHelper;
import org.jeyzer.analyzer.session.JzrSession;
import org.jeyzer.monitor.config.engine.ConfigMonitorRule;
import org.jeyzer.monitor.config.engine.ConfigParamMonitorRule;
import org.jeyzer.monitor.config.engine.ConfigPrincipalMonitorRule;
import org.jeyzer.monitor.engine.event.MonitorSystemEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;
import org.jeyzer.monitor.engine.event.info.SubLevel;
import org.jeyzer.monitor.engine.rule.MonitorSystemRule;
import org.jeyzer.monitor.engine.rule.condition.system.ValueSystemProvider;
import org.jeyzer.monitor.impl.event.system.OperationInPrincipalPercentEvent;
import org.jeyzer.monitor.util.Operator;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;

public class OperationInPrincipalPercentRule extends MonitorSystemRule implements ValueSystemProvider{

	public static final String RULE_NAME = "Operation in principal percentage";
	
	public static final String RULE_NARRATIVE = "The " + RULE_NAME + " rule permits to detect any particular high applicative activity (function principal) based on operations. "
			+ "It is useful to track intensive activities, within specific actions identified by their function principal, based on operation names which are known as problematic.";
	
	private String principal;
	private String operation;
	private String paramDisplay;
	private Operator operator;
	
	public OperationInPrincipalPercentRule(ConfigMonitorRule def) throws JzrInitializationException {
		super(def, RULE_NAME + " : " + ((ConfigParamMonitorRule)def).getDisplayName(), 
				Operator.getEventDescription((ConfigParamMonitorRule) def));
		ConfigPrincipalMonitorRule mxDef = (ConfigPrincipalMonitorRule) def;
		this.principal = mxDef.getPrincipal();
		this.operation = mxDef.getParamName();
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
		Multimap<String, Tag> operationTagsPerFunctionPrincipal = session.getOperationSetPerFunctionPrincipal();
		Multimap<String, ThreadAction> actionsPerFunctionPrincipal = session.getActionSetPerFunctionPrincipal();
		
		Collection<Tag> operationTags = operationTagsPerFunctionPrincipal.get(principal);
		if (operationTags == null || operationTags.isEmpty())
			return false;

		Collection<ThreadAction> actions = actionsPerFunctionPrincipal.get(principal);
		if (actions == null || actions.isEmpty())
			return false;
		
		Tag operationTag = new OperationTag(operation);
    	if (!operationTags.contains(operationTag))
    		return false;
    	
		// use multi set to get it order by appearance
		Multiset<Tag> operationTagMultiSet = HashMultiset.create();
		for (Tag tag : operationTags){
			operationTagMultiSet.add(tag);
		}
    	
    	// get percentage
		int actionStackCount = 0;
		for (ThreadAction action : actions)
			actionStackCount += action.size();
    	
		int tagOperationStackCount = operationTagMultiSet.count(operationTag);
    	int tagPercent = FormulaHelper.percentRound(tagOperationStackCount, actionStackCount);
    	
		switch(operator){
		case LOWER_OR_EQUAL:
			return tagPercent <= value;
		default:
			return tagPercent >= value;
		}
	}

	@Override
	public MonitorSystemEvent createSystemEvent(MonitorEventInfo info) {
		return new OperationInPrincipalPercentEvent(
				this.paramDisplay,
				info,
				this.principal,
				this.operation,
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
