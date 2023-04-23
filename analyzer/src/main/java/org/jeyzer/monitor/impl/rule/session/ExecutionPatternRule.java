package org.jeyzer.monitor.impl.rule.session;

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



import static org.jeyzer.monitor.config.engine.ConfigMonitorThreshold.THRESHOLD_GLOBAL_PATTERN;
import static org.jeyzer.monitor.config.engine.ConfigMonitorThreshold.THRESHOLD_SESSION_PATTERN;





import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.analyzer.data.stack.ThreadStack;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.monitor.config.engine.ConfigMonitorRule;
import org.jeyzer.monitor.engine.event.MonitorSessionEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;
import org.jeyzer.monitor.engine.event.info.SubLevel;
import org.jeyzer.monitor.engine.rule.MonitorSessionRule;
import org.jeyzer.monitor.engine.rule.condition.session.PatternSessionProvider;
import org.jeyzer.monitor.impl.event.session.ExecutionPatternEvent;

public class ExecutionPatternRule extends MonitorSessionRule implements PatternSessionProvider{

	public static final String RULE_NAME = "Session execution pattern";
	
	public static final String RULE_CONDITION_DESCRIPTION = "Code regex (pattern) is detected within any active stack.";
	public static final String RULE_NARRATIVE = "The " + RULE_NAME + " rule permits to detect method calls of interest on active stacks. "
			+ "For performance reasons, it is recommended to transpose those method calls into Jeyzer functions/operations and therefore use the Function and Operation presence rules. ";
	
	// can be null
	private String patternName;	
	
	public ExecutionPatternRule(ConfigMonitorRule def)
			throws JzrInitializationException {
		super(def, RULE_NAME + " : " + def.getExtraInfo(), RULE_CONDITION_DESCRIPTION);
		patternName = def.getExtraInfo();
	}

	@Override
	public boolean matchPattern(ThreadDump dump, Pattern pattern) {
		for (ThreadStack ts : dump.getWorkingThreads()){
			String codeStack = ts.getStackHandler().getJzrFilteredText().getText();
			if (pattern.matcher(codeStack).find()){
				return true;
			}
		}
		return false;
	}

	@Override
	public MonitorSessionEvent createSessionEvent(MonitorEventInfo info, ThreadDump td) {
		return new ExecutionPatternEvent(info, td, patternName);
	}

	@Override
	public boolean isAdvancedMonitoringBased() {
		return false;
	}
 
	@Override
	protected List<String> getSupportedThresholdTypes(){
		return Arrays.asList(THRESHOLD_SESSION_PATTERN, THRESHOLD_GLOBAL_PATTERN);
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
