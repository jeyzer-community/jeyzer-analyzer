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
import org.jeyzer.analyzer.data.tag.OperationTag;
import org.jeyzer.analyzer.data.tag.Tag;
import org.jeyzer.analyzer.math.FormulaHelper;
import org.jeyzer.analyzer.session.JzrSession;
import org.jeyzer.monitor.engine.event.MonitorSystemEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;
import org.jeyzer.monitor.util.Operator;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;

public class OperationInPrincipalPercentEvent extends MonitorSystemEvent {

	private String principal;
	private String operation;
	private long maxValue;
	private Operator operator;
	
	public OperationInPrincipalPercentEvent(String eventName, MonitorEventInfo info, String principal, String operation, Operator operator) {
		super(eventName, info);
		this.principal = principal;
		this.operation = operation;
		this.operator = operator;
		this.maxValue = Operator.LOWER_OR_EQUAL.equals(operator) ? Integer.MAX_VALUE : Integer.MIN_VALUE;
	}
	
	@Override
	public void updateContext(JzrSession session) {
		Multimap<String, Tag> operationTagsPerFunctionPrincipal = session.getOperationSetPerFunctionPrincipal();
		Multimap<String, ThreadAction> actionsPerFunctionPrincipal = session.getActionSetPerFunctionPrincipal();
		
		Collection<Tag> operationTags = operationTagsPerFunctionPrincipal.get(principal);
		if (operationTags == null || operationTags.isEmpty())
			return;

		Collection<ThreadAction> actions = actionsPerFunctionPrincipal.get(principal);
		if (actions == null || actions.isEmpty())
			return;
		
		Tag operationTag = new OperationTag(operation);
    	if (!operationTags.contains(operationTag))
    		return;
    	
		// use multi set to get it ordered by appearance
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
    	
		if (Operator.LOWER_OR_EQUAL.equals(operator) && tagPercent <= maxValue)
			maxValue = tagPercent;
		else if (!Operator.LOWER_OR_EQUAL.equals(operator) && tagPercent >= maxValue)
			maxValue = tagPercent;
	}

	@Override
	protected void dumpExtraParameters(StringBuilder msg) {
		msg.append(this.principal + " / " + " : " + this.maxValue + "\n");
	}
	
	@Override
	public void addPrintableExtraParameters(List<String> params) {
		params.add(this.principal + " / " +  operation);
		params.add(Long.toString(this.maxValue) + "%");
	}

}
