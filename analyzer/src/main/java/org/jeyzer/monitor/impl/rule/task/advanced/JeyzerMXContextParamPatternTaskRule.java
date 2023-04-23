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



import static org.jeyzer.monitor.config.engine.ConfigMonitorThreshold.THRESHOLD_ACTION_PATTERN;
import static org.jeyzer.monitor.config.engine.ConfigMonitorThreshold.THRESHOLD_STACK_PATTERN;




import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

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
import org.jeyzer.monitor.engine.rule.condition.task.PatternTaskProvider;
import org.jeyzer.monitor.impl.event.task.advanced.JeyzerMXContextParamPatternTaskEvent;

public class JeyzerMXContextParamPatternTaskRule extends MonitorTaskRule implements PatternTaskProvider {

	private String paramName;
	private String paramDisplay;
	
	public static final String RULE_NAME = "Task Jeyzer MX context parameter pattern";
	
	public static final String RULE_NARRATIVE = "The " + RULE_NAME + " rule permits to detect signals upon applicative data parsing at thread level. "
			+ "Rule can focus on a specific function name if specified at threshold level. "
			+ "Matching can also be based on appearance percentage within an action when specified at threshold level.";
	
	public JeyzerMXContextParamPatternTaskRule(ConfigMonitorRule def) throws JzrInitializationException {
		super(def, RULE_NAME + " : " + ((ConfigParamMonitorRule)def).getDisplayName(), 
				((ConfigParamMonitorRule)def).getDisplayName() + " contains the (pattern) regex.");
		ConfigParamMonitorRule mxDef = (ConfigParamMonitorRule) def;
		this.paramName = mxDef.getParamName();
		this.paramDisplay = mxDef.getDisplayName();
	}

	@Override
	public boolean matchPattern(ThreadStack stack, Pattern pattern) {
		ThreadStackJeyzerMXInfo info = stack.getThreadStackJeyzerMXInfo();
		if (info == null)
			return false;
		
		String paramValue = info.getContextParam(paramName);
		if (paramValue == null || paramValue.isEmpty())
			return false;
		
		return pattern.matcher(paramValue).find();
	}

	@Override
	public MonitorTaskEvent createTaskEvent(MonitorEventInfo info, ThreadAction action, ThreadStackHandler stack) {
		return new JeyzerMXContextParamPatternTaskEvent(
				this.paramDisplay,
				info,
				action,
				this.paramName,
				action.getThreadStackJeyzerMXInfo().getUser(),
				action.getThreadStackJeyzerMXInfo().getId()
			);
	}

	@Override
	public boolean isAdvancedMonitoringBased() {
		return true;
	}
	
	@Override
	protected List<String> getSupportedThresholdTypes(){
		return Arrays.asList(THRESHOLD_ACTION_PATTERN, THRESHOLD_STACK_PATTERN);
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
