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
import org.jeyzer.analyzer.data.tag.ContentionTypeTag;
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
import org.jeyzer.monitor.impl.event.system.ContentionTypeInPrincipalPercentEvent;
import org.jeyzer.monitor.util.Operator;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;

public class ContentionTypeInPrincipalPercentRule extends MonitorSystemRule implements ValueSystemProvider{

	public static final String RULE_NAME = "Contention type in principal percentage";
	
	public static final String RULE_NARRATIVE = "The " + RULE_NAME + " rule permits to detect contentions within function principals. "
			+ "It is useful to detect for example unusual database access within particular actions.";
	
	private String principal;
	private String contentionType;
	private String paramDisplay;
	private Operator operator;
	
	public ContentionTypeInPrincipalPercentRule(ConfigMonitorRule def) throws JzrInitializationException {
		super(def, RULE_NAME + " : " + ((ConfigParamMonitorRule)def).getDisplayName(), 
				Operator.getEventDescription((ConfigParamMonitorRule) def));
		ConfigPrincipalMonitorRule mxDef = (ConfigPrincipalMonitorRule) def;
		this.principal = mxDef.getPrincipal();
		this.contentionType = mxDef.getParamName();
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
		Multimap<String, Tag> contentionTypeTagsPerFunctionPrincipal = session.getContentionTypeSetPerFunctionPrincipal();
		Multimap<String, ThreadAction> actionsPerFunctionPrincipal = session.getActionSetPerFunctionPrincipal();
		
		Collection<Tag> contentionTypeTags = contentionTypeTagsPerFunctionPrincipal.get(principal);
		if (contentionTypeTags == null || contentionTypeTags.isEmpty())
			return false;

		Collection<ThreadAction> actions = actionsPerFunctionPrincipal.get(principal);
		if (actions == null || actions.isEmpty())
			return false;
		
		Tag contentionTypeTag = new ContentionTypeTag(contentionType);
    	if (!contentionTypeTags.contains(contentionTypeTag))
    		return false;
    	
		// use multi set to get it order by appearance
		Multiset<Tag> contentionTypeTagMultiSet = HashMultiset.create();
		for (Tag tag : contentionTypeTags){
			contentionTypeTagMultiSet.add(tag);
		}
    	
    	// get percentage
		int actionStackCount = 0;
		for (ThreadAction action : actions)
			actionStackCount += action.size();
    	
		int tagContentionTypeStackCount = contentionTypeTagMultiSet.count(contentionTypeTag);
    	int tagPercent = FormulaHelper.percentRound(tagContentionTypeStackCount, actionStackCount);
    	
		switch(operator){
		case LOWER_OR_EQUAL:
			return tagPercent <= value;
		default:
			return tagPercent >= value;
		}
	}

	@Override
	public MonitorSystemEvent createSystemEvent(MonitorEventInfo info) {
		return new ContentionTypeInPrincipalPercentEvent(
				this.paramDisplay,
				info,
				this.principal,
				this.contentionType,
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
