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







import java.util.List;

import org.jeyzer.analyzer.data.tag.FunctionTag;
import org.jeyzer.analyzer.data.tag.Tag;
import org.jeyzer.analyzer.math.FormulaHelper;
import org.jeyzer.analyzer.session.JzrSession;
import org.jeyzer.monitor.engine.event.MonitorSystemEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;
import org.jeyzer.monitor.util.Operator;

import com.google.common.collect.Multiset;

public class FunctionGlobalPercentEvent extends MonitorSystemEvent {

	private String function;
	private long maxValue;
	private Operator operator;
	
	public FunctionGlobalPercentEvent(String eventName, MonitorEventInfo info, String function, Operator operator) {
		super(eventName, info);
		this.function = function;
		this.operator = operator;
		maxValue = Operator.LOWER_OR_EQUAL.equals(operator) ? Integer.MAX_VALUE : Integer.MIN_VALUE;  
	}
	
	@Override
	public void updateContext(JzrSession session) {
		Multiset<Tag> functionTags = session.getFunctionSet();
		Tag functionTag = new FunctionTag(function);
    	int tagCount = functionTags.count(functionTag);
    	if (tagCount == 0)
    		return;
    	
    	// get percentage
		// total number of active stacks
    	int globalActionsStackSize = session.getActionsStackSize();
    	int tagPercent = FormulaHelper.percentRound(tagCount, globalActionsStackSize);
    	
		if (Operator.LOWER_OR_EQUAL.equals(operator) && tagPercent <= maxValue)
			maxValue = tagPercent;
		else if (!Operator.LOWER_OR_EQUAL.equals(operator) && tagPercent >= maxValue)
			maxValue = tagPercent;
	}

	@Override
	protected void dumpExtraParameters(StringBuilder msg) {
		msg.append(operator.getBoundPrefix() + function + " : " + this.maxValue + "\n");
	}
	
	@Override
	public void addPrintableExtraParameters(List<String> params) {
		params.add(operator.getBoundPrefix() + function);
		params.add(Long.toString(this.maxValue) + "%");
	}

}
