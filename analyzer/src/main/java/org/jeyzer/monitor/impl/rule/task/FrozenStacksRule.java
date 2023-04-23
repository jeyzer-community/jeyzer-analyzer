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



import static org.jeyzer.monitor.config.engine.ConfigMonitorThreshold.THRESHOLD_ACTION_SIGNAL;
import static org.jeyzer.monitor.config.engine.ConfigMonitorThreshold.THRESHOLD_STACK_SIGNAL;




import java.util.Arrays;
import java.util.List;

import org.jeyzer.analyzer.data.ThreadAction;
import org.jeyzer.analyzer.data.stack.ThreadStack;
import org.jeyzer.analyzer.data.stack.ThreadStack.STACK_CODE_STATE;
import org.jeyzer.analyzer.data.stack.ThreadStack.ThreadStackHandler;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.monitor.config.engine.ConfigMonitorRule;
import org.jeyzer.monitor.engine.event.MonitorTaskEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;
import org.jeyzer.monitor.engine.event.info.SubLevel;
import org.jeyzer.monitor.engine.rule.MonitorTaskRule;
import org.jeyzer.monitor.engine.rule.condition.task.SignalTaskProvider;
import org.jeyzer.monitor.impl.event.task.FrozenCodeTaskEvent;

public class FrozenStacksRule extends MonitorTaskRule implements SignalTaskProvider{

	public static final String RULE_NAME = "Frozen stacks";
	
	public static final String RULE_CONDITION_DESCRIPTION = "Thread stack is identical to previous one.";
	public static final String RULE_NARRATIVE = "The " + RULE_NAME + " rule permits to detect threads that seem frozen. "
			+ "Freeze must not be taken strictu senso as the thread may have executed other methods between two recording snapshots. "
			+ "It is however very interesting to highlight thread contentions."
			+ "Rule can focus on a specific function name if specified at threshold level. "
			+ "Matching can also be based on appearance percentage within an action when specified at threshold level.";
	
	public FrozenStacksRule(ConfigMonitorRule def) throws JzrInitializationException {
		super(def, RULE_NAME, RULE_CONDITION_DESCRIPTION);
	}
	
	@Override
	protected List<String> getSupportedThresholdTypes(){
		return Arrays.asList(THRESHOLD_ACTION_SIGNAL, THRESHOLD_STACK_SIGNAL);
	}

	@Override
	public boolean isAdvancedMonitoringBased() {
		return false;
	}

	@Override
	public boolean matchSignal(ThreadStack stack) {
		return stack.isFrozenStackCode();
	}

	@Override
	public boolean matchBeginSignal(ThreadStack stack) {
		return STACK_CODE_STATE.FREEZE_BEGIN.equals(stack.getStackCodeState());
	}

	@Override
	public MonitorTaskEvent createTaskEvent(MonitorEventInfo info, ThreadAction action, ThreadStackHandler stack) {
		return new FrozenCodeTaskEvent(
				info,
				action,
				stack
				);
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
