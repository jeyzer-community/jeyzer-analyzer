package org.jeyzer.monitor.impl.rule.task;

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
import org.jeyzer.analyzer.data.stack.ThreadStack.ThreadStackHandler;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.monitor.config.engine.ConfigMonitorRule;
import org.jeyzer.monitor.engine.event.MonitorTaskEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;
import org.jeyzer.monitor.engine.event.info.SubLevel;
import org.jeyzer.monitor.engine.rule.MonitorTaskRule;
import org.jeyzer.monitor.engine.rule.condition.task.PatternTaskProvider;
import org.jeyzer.monitor.impl.event.task.ExecutionPatternTaskEvent;


public class ExecutionPatternTaskRule extends MonitorTaskRule implements PatternTaskProvider {

	public static final String RULE_NAME = "Task execution pattern";
	
	public static final String RULE_CONDITION_DESCRIPTION = "Code regex (pattern) is detected within the active task.";
	public static final String RULE_NARRATIVE = "The " + RULE_NAME + " rule permits to detect a particular executed method occurence at thread level. "
			+ "For performance reasons, prefer translating the method into profile functions/operations to use their related " + FunctionTaskRule.RULE_NAME + " and " + OperationTaskRule.RULE_NAME + " rules. "
			+ "Rule can focus on a specific function name if specified at threshold level. "
			+ "Matching can also be based on appearance percentage within an action when specified at threshold level.";
	
	private String executionPatternDisplayName; // mandatory
	
	public ExecutionPatternTaskRule(ConfigMonitorRule def) throws JzrInitializationException {
		super(def, RULE_NAME, RULE_CONDITION_DESCRIPTION);
		this.executionPatternDisplayName = def.getExtraInfo();
		if (this.executionPatternDisplayName == null)
			throw new JzrInitializationException("The extra_info attribute is missing on the " + RULE_NAME + " rule. Please add it.");
	}

	@Override
	public boolean matchPattern(ThreadStack stack, Pattern pattern) {
		String codeStack = stack.getStackHandler().getJzrFilteredText().getText();
		return pattern.matcher(codeStack).find();
	}

	@Override
	public MonitorTaskEvent createTaskEvent(MonitorEventInfo info, ThreadAction action, ThreadStackHandler stack) {
		return new ExecutionPatternTaskEvent(
				info,
				action,
				stack,
				executionPatternDisplayName
		);
	}

	@Override
	public boolean isAdvancedMonitoringBased() {
		return false;
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
