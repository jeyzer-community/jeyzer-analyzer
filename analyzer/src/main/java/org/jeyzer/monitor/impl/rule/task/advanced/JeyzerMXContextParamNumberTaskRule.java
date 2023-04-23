package org.jeyzer.monitor.impl.rule.task.advanced;

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



import static org.jeyzer.monitor.config.engine.ConfigMonitorThreshold.THRESHOLD_ACTION_VALUE;
import static org.jeyzer.monitor.config.engine.ConfigMonitorThreshold.THRESHOLD_STACK_VALUE;




import java.util.Arrays;
import java.util.List;

import org.jeyzer.analyzer.data.ThreadAction;
import org.jeyzer.analyzer.data.stack.ThreadStack;
import org.jeyzer.analyzer.data.stack.ThreadStackJeyzerMXInfo;
import org.jeyzer.analyzer.data.stack.ThreadStack.ThreadStackHandler;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.monitor.config.engine.ConfigMonitorRule;
import org.jeyzer.monitor.config.engine.ConfigParamMonitorRule;
import org.jeyzer.monitor.engine.event.MonitorTaskEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;
import org.jeyzer.monitor.engine.event.info.SubLevel;
import org.jeyzer.monitor.engine.rule.MonitorTaskRule;
import org.jeyzer.monitor.engine.rule.condition.task.ValueTaskProvider;
import org.jeyzer.monitor.impl.event.task.advanced.JeyzerMXContextParamNumberTaskEvent;
import org.jeyzer.monitor.util.Operator;

import com.google.common.primitives.Longs;

public class JeyzerMXContextParamNumberTaskRule extends MonitorTaskRule implements ValueTaskProvider{

	public static final String RULE_NAME = "Task Jeyzer MX context parameter number";
	
	public static final String RULE_NARRATIVE = "The " + RULE_NAME + " rule permits to detect limit exceeds upon an applicative numeric figure at thread level. "
			+ "Rule can focus on a specific function name if specified at threshold level. "
			+ "Matching can also be based on appearance percentage within an action when specified at threshold level.";

	private String paramName;
	private String paramDisplay;
	private Operator operator;
	
	public JeyzerMXContextParamNumberTaskRule(ConfigMonitorRule def) throws JzrInitializationException {
		super(def, RULE_NAME + " : " + ((ConfigParamMonitorRule)def).getDisplayName(), 
				Operator.getEventDescription((ConfigParamMonitorRule) def));
		ConfigParamMonitorRule mxDef = (ConfigParamMonitorRule) def;
		this.paramName = mxDef.getParamName();
		this.paramDisplay = mxDef.getDisplayName();
		this.operator = mxDef.getOperator();
	}

	@Override
	public MonitorTaskEvent createTaskEvent(MonitorEventInfo info, ThreadAction action, ThreadStackHandler stack) {
		return new JeyzerMXContextParamNumberTaskEvent(
				this.paramDisplay, 
				info,
				action,
				this.paramName,
				action.getThreadStackJeyzerMXInfo().getUser(),
				action.getThreadStackJeyzerMXInfo().getId(),
				operator
			);
	}

	@Override
	public boolean isAdvancedMonitoringBased() {
		return true;
	}
	
	@Override
	protected List<String> getSupportedThresholdTypes(){
		return Arrays.asList(THRESHOLD_ACTION_VALUE, THRESHOLD_STACK_VALUE);
	}

	@Override
	public boolean matchValue(ThreadStack stack, long thresholdValue) {
		ThreadStackJeyzerMXInfo info = stack.getThreadStackJeyzerMXInfo();
		if (info == null)
			return false;
		
		String paramValue = info.getContextParam(paramName);
		if (paramValue == null || paramValue.isEmpty())
			return false;
		
		Long paramLongValue = Longs.tryParse(paramValue);
		if (paramLongValue == null)
			return false;
		
		switch(operator){
		case LOWER_OR_EQUAL:
			return paramLongValue <= thresholdValue;
		default:
			return paramLongValue >= thresholdValue;
		}
	}

	@Override
	public SubLevel getDefaultSubLevel() {
		return SubLevel.DEFAULT_VERY_LOW;
	}
	
	@Override
	public String getDefaultNarrative() {
		return RULE_NARRATIVE;
	}
}
