package org.jeyzer.monitor.impl.event.system;

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







import java.util.Collection;
import java.util.List;

import org.jeyzer.analyzer.data.ThreadAction;
import org.jeyzer.analyzer.data.tag.FunctionTag;
import org.jeyzer.analyzer.data.tag.Tag;
import org.jeyzer.analyzer.math.FormulaHelper;
import org.jeyzer.analyzer.session.JzrSession;
import org.jeyzer.monitor.engine.event.MonitorSystemEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;
import org.jeyzer.monitor.util.Operator;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;

public class FunctionInPrincipalPercentEvent extends MonitorSystemEvent {

	private String principal;
	private String function;
	private long maxValue;
	private Operator operator;
	
	public FunctionInPrincipalPercentEvent(String eventName, MonitorEventInfo info, String principal, String function, Operator operator) {
		super(eventName, info);
		this.principal = principal;
		this.function = function;
		this.operator = operator;
		this.maxValue = Operator.LOWER_OR_EQUAL.equals(operator) ? Integer.MAX_VALUE : Integer.MIN_VALUE;  
	}
	
	@Override
	public void updateContext(JzrSession session) {
		Multimap<String, Tag> functionTagsPerFunctionPrincipal = session.getFunctionSetPerFunctionPrincipal();
		Multimap<String, ThreadAction> actionsPerFunctionPrincipal = session.getActionSetPerFunctionPrincipal();
		
		Collection<Tag> functionTags = functionTagsPerFunctionPrincipal.get(principal);
		if (functionTags == null || functionTags.isEmpty())
			return;

		Collection<ThreadAction> actions = actionsPerFunctionPrincipal.get(principal);
		if (actions == null || actions.isEmpty())
			return;
		
		Tag functionTag = new FunctionTag(function);
    	if (!functionTags.contains(functionTag))
    		return;
    	
		Multiset<Tag> functionTagMultiSet = HashMultiset.create();
		for (Tag tag : functionTags){
			functionTagMultiSet.add(tag);
		}
    	
    	// get percentage
		int actionStackCount = 0;
		for (ThreadAction action : actions)
			actionStackCount += action.getStackSize();
    	
		int tagfunctionStackCount = functionTagMultiSet.count(functionTag);
    	int tagPercent = FormulaHelper.percentRound(tagfunctionStackCount, actionStackCount);
    	
		if (Operator.LOWER_OR_EQUAL.equals(operator) && tagPercent <= maxValue)
			maxValue = tagPercent;
		else if (!Operator.LOWER_OR_EQUAL.equals(operator) && tagPercent >= maxValue)
			maxValue = tagPercent;
	}

	@Override
	protected void dumpExtraParameters(StringBuilder msg) {
		msg.append(operator.getBoundPrefix() + this.principal + " / " + this.function + " : " + this.maxValue + "\n");
	}
	
	@Override
	public void addPrintableExtraParameters(List<String> params) {
		params.add(operator.getBoundPrefix() + this.principal + " / " +  function);
		params.add(Long.toString(this.maxValue) + "%");
	}

}
