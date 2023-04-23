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




import static org.jeyzer.monitor.config.engine.ConfigMonitorThreshold.THRESHOLD_GLOBAL_CUSTOM;
import static org.jeyzer.monitor.config.engine.ConfigMonitorThreshold.THRESHOLD_SESSION_CUSTOM;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.analyzer.data.stack.ThreadStack;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.monitor.config.engine.ConfigMonitorRule;
import org.jeyzer.monitor.config.engine.ConfigMonitorThreshold;
import org.jeyzer.monitor.engine.event.MonitorSessionEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;
import org.jeyzer.monitor.engine.event.info.SubLevel;
import org.jeyzer.monitor.engine.rule.MonitorSessionRule;
import org.jeyzer.monitor.engine.rule.condition.session.CustomSessionProvider;
import org.jeyzer.monitor.impl.event.session.FunctionAndOperationParallelContentionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FunctionAndOperationParallelContentionRule extends MonitorSessionRule implements CustomSessionProvider{
	
	protected static final Logger logger = LoggerFactory.getLogger(FunctionAndOperationParallelContentionRule.class);
	
	public static final String RULE_NAME = "Function and operation parallel contention";
	
	public static final String OPERATION_PATTERN_PARAM_NAME = "operation_pattern";
	
	public static final String RULE_CONDITION_DESCRIPTION = "Function name regex (pattern) and operation name regex (pattern) are seen in more than (value) parallel threads.";
	public static final String RULE_NARRATIVE = "The " + RULE_NAME + " rule permits to detect parallel similar actions by looking at both functions and operations of the action. "
			+ "It is useful to detect particular intensive activities which are known as problematic. ";
	
	// can be null
	private String functionName;
	
	private Pattern operationPattern;
	private boolean invalidOperationPattern = false;
	
	public FunctionAndOperationParallelContentionRule(ConfigMonitorRule def) throws JzrInitializationException {
		super(def, RULE_NAME + " : " + def.getExtraInfo(), RULE_CONDITION_DESCRIPTION);
		functionName = def.getExtraInfo();
	}

	@Override
	public boolean matchCustomParameters(ThreadDump dump, ConfigMonitorThreshold thresholdConfig) {
		int count = getFunctionMatchCount(dump, thresholdConfig);
		return count >= thresholdConfig.getValue();
	}

	private int getFunctionMatchCount(ThreadDump dump, ConfigMonitorThreshold thresholdConfig) {
		int count = 0;
		
		if (invalidOperationPattern)
			return 0;
		
		for (ThreadStack stack : dump.getWorkingThreads()){
			for (String function : stack.getFunctionTags()){
				Matcher matcher = thresholdConfig.getPattern().matcher(function);
				if (matcher.find()){
					count += getOperationMatchCount(stack, thresholdConfig);
					break;
				}
			}
		}
		return count;
	}
	
	private int getOperationMatchCount(ThreadStack stack, ConfigMonitorThreshold thresholdConfig) {
		if (this.operationPattern == null){
			this.operationPattern = validateAndCache(thresholdConfig);
			if (invalidOperationPattern)
				return 0;
		}
		
		for (String operation : stack.getOperationTags()){
			Matcher matcher = this.operationPattern.matcher(operation);
			if (matcher.find())
				return 1;
		}
		
		return 0;
	}	

	@Override
	public MonitorSessionEvent createSessionEvent(MonitorEventInfo info, ThreadDump td) {
		return new FunctionAndOperationParallelContentionEvent(info, td, functionName);
	}

	@Override
	public boolean isAdvancedMonitoringBased() {
		return false;
	}

	@Override
	protected List<String> getSupportedThresholdTypes() {
		return Arrays.asList(THRESHOLD_SESSION_CUSTOM, THRESHOLD_GLOBAL_CUSTOM);
	}
	
	@Override
	public SubLevel getDefaultSubLevel() {
		return SubLevel.DEFAULT_LOW;
	}
	
	@Override
	public String getDefaultNarrative() {
		return RULE_NARRATIVE;
	}
	
	private Pattern validateAndCache(ConfigMonitorThreshold thresholdConfig) {
		if (operationPattern != null)
			return operationPattern;
		
		String paramValue = thresholdConfig.getCustomParameter(OPERATION_PATTERN_PARAM_NAME);
		if (paramValue == null || paramValue.isEmpty()){
			logger.warn(OPERATION_PATTERN_PARAM_NAME + " is missing on the " + RULE_NAME + " monitoring rule. Please add it. Rule threshold will be ignored.");
			invalidOperationPattern = true;
			return null;
		}
		
		return Pattern.compile(paramValue);
	}
}
