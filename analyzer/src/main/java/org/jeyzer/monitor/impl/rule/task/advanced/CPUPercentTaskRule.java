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
import org.jeyzer.analyzer.data.stack.ThreadStack.ThreadStackHandler;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.monitor.config.engine.ConfigMonitorRule;
import org.jeyzer.monitor.engine.event.MonitorTaskEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;
import org.jeyzer.monitor.engine.event.info.SubLevel;
import org.jeyzer.monitor.engine.rule.MonitorTaskRule;
import org.jeyzer.monitor.engine.rule.condition.task.ValueTaskProvider;
import org.jeyzer.monitor.impl.event.task.advanced.CPUPercentTaskEvent;

public class CPUPercentTaskRule extends MonitorTaskRule implements ValueTaskProvider{

	public static final String RULE_NAME = "CPU consuming task";
	
	public static final String RULE_CONDITION_DESCRIPTION = "Task CPU % is greater or equal to (value).";
	public static final String RULE_NARRATIVE = "The " + RULE_NAME + " rule permits to detect high CPU usage at thread level. "
			+ "Rule can focus on a specific function name if specified at threshold level. "
			+ "Matching can also be based on appearance percentage within an action when specified at threshold level.";

	public CPUPercentTaskRule(ConfigMonitorRule def) throws JzrInitializationException {
		super(def, RULE_NAME, RULE_CONDITION_DESCRIPTION);
	}

	@Override
	public MonitorTaskEvent createTaskEvent(MonitorEventInfo info, ThreadAction action, ThreadStackHandler stack) {
		return new CPUPercentTaskEvent(
				info,
				action,
				stack
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
		return stack.getCpuInfo().getCpuUsage() >= thresholdValue;
	}
	
	@Override
	public SubLevel getDefaultSubLevel() {
		return SubLevel.DEFAULT_MEDIUM;
	}
	
	@Override
	public String getDefaultNarrative() {
		return RULE_NARRATIVE;
	}
}
