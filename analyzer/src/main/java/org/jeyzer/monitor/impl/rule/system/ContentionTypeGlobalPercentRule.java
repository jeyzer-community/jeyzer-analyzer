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

import org.jeyzer.analyzer.data.tag.ContentionTypeTag;
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
import org.jeyzer.monitor.impl.event.system.ContentionTypeGlobalPercentEvent;
import org.jeyzer.monitor.util.Operator;

import com.google.common.collect.Multiset;

public class ContentionTypeGlobalPercentRule extends MonitorSystemRule implements ValueSystemProvider{

	public static final String RULE_NAME = "Contention type global percentage";
	
	public static final String RULE_NARRATIVE = "The " + RULE_NAME + " rule permits to detect global contentions. "
			+ "It is particularely useful to detect for example unusual database access. ";
	
	private static final int TRIGGER_LIMIT = 50;
	
	private String contentionType;
	private String paramDisplay;
	private Operator operator;
	
	public ContentionTypeGlobalPercentRule(ConfigMonitorRule def) throws JzrInitializationException {
		super(def, RULE_NAME + " : " + ((ConfigParamMonitorRule)def).getDisplayName(), 
				Operator.getEventDescription((ConfigParamMonitorRule) def));
		ConfigParamMonitorRule mxDef = (ConfigParamMonitorRule) def;
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
		Multiset<Tag> contentionTypeTags = session.getContentionTypeSet();
		Tag contentionTypeTag = new ContentionTypeTag(contentionType);
    	int tagCount = contentionTypeTags.count(contentionTypeTag);
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
		return new ContentionTypeGlobalPercentEvent(
				this.paramDisplay,
				info,
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
