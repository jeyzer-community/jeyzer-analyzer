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



import static org.jeyzer.monitor.config.engine.ConfigMonitorThreshold.THRESHOLD_ACTION_VALUE;




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
import org.jeyzer.monitor.impl.event.task.LongRunningTaskEvent;

public class LongRunningTaskRule extends MonitorTaskRule implements ValueTaskProvider {

	public static final String RULE_NAME = "Long running task";
	
	public static final String RULE_CONDITION_DESCRIPTION = "Task is longer than (time) in sec or detected in (count) consecutive recording snapshots.";
	public static final String RULE_NARRATIVE = "The " + RULE_NAME + " rule permits to detect long running actions. "
			+ "It is useful to catch up actions that take higher time than expected. "
			+ "Rule can be tuned by specifying a function name.";
	
	public LongRunningTaskRule(ConfigMonitorRule def) throws JzrInitializationException {
		super(def, RULE_NAME, RULE_CONDITION_DESCRIPTION);
	}

	@Override
	public boolean matchValue(ThreadStack stack, long value) {
		return true;  // always true, the election will filter on the time/count
	}

	@Override
	public MonitorTaskEvent createTaskEvent(MonitorEventInfo info, ThreadAction action, ThreadStackHandler stack) {
		return new LongRunningTaskEvent(info,action);
	}

	@Override
	public boolean isAdvancedMonitoringBased() {
		return false;
	}

	@Override
	protected List<String> getSupportedThresholdTypes(){
		return Arrays.asList(THRESHOLD_ACTION_VALUE);
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
