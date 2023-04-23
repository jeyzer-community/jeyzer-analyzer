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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

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
import org.jeyzer.monitor.impl.event.session.MultiDistinctFunctionContentionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.primitives.Ints;

public class MultiDistinctFunctionContentionRule extends MonitorSessionRule implements CustomSessionProvider{
	
	protected static final Logger logger = LoggerFactory.getLogger(MultiDistinctFunctionContentionRule.class);
	
	public static final String RULE_NAME = "Multi function contention";
	
	public static final String FUNCTIONS_PARAM_NAME = "functions";
	public static final String FUNCTIONS_APPEARANCE_THRESHOLDS_PARAM_NAME = "function_appearance_thresholds";
	
	public static final String RULE_CONDITION_DESCRIPTION = "(functions) are seen respectively in more than (function_appearance_thresholds) in parallel threads.";
	public static final String RULE_NARRATIVE = "The " + RULE_NAME + " rule permits to detect parallel distinct functions. "
			+ "It is useful to detect particular intensive activities based on distinct function names which are known as problematic when acting together.";
	
	// can be null
	private String caseName;
	
	private Map<ConfigMonitorThreshold, List<String>> functionsCache = new HashMap<>();
	private Map<ConfigMonitorThreshold, List<Integer>> thresholdsCache = new HashMap<>();
	
	public MultiDistinctFunctionContentionRule(ConfigMonitorRule def) throws JzrInitializationException {
		super(def, RULE_NAME + " : " + def.getExtraInfo(), RULE_CONDITION_DESCRIPTION);
		caseName = def.getExtraInfo();
	}

	@Override
	public boolean matchCustomParameters(ThreadDump dump, ConfigMonitorThreshold thresholdConfig) {
		List<String> functions = getFunctionNames(thresholdConfig);
		if (functions == null || functions.isEmpty())
			return false;
		
		// there must be a value
		List<Integer> thresholds = this.thresholdsCache.get(thresholdConfig);
		if (thresholds == null || thresholds.isEmpty())
			return false; // so should not happen
		
		
		for (int i=0; i<functions.size(); i++){
			int count = getFunctionMatchCount(dump, functions.get(i));
			if (count < thresholds.get(i))
				return false;
		}
		
		return true;
	}
	
	private int getFunctionMatchCount(ThreadDump dump, String functionTarget) {
		int count = 0;
		
		for (ThreadStack stack : dump.getWorkingThreads()){
			for (String function : stack.getFunctionTags()){
				if (functionTarget.equals(function)){ // exact match
					count++;
					break;
				}
			}
		}
		return count;
	}	

	@Override
	public MonitorSessionEvent createSessionEvent(MonitorEventInfo info, ThreadDump td) {
		return new MultiDistinctFunctionContentionEvent(info, td, caseName);
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
	
	private List<String> getFunctionNames(ConfigMonitorThreshold thresholdConfig) {
		List<String> functions = this.functionsCache.get(thresholdConfig);
		if (functions == null)
			// nasty workaround. Should be done in ConfigMonitorThreshold child, but not easy to achieve
			// Or rule should be transformed in Composite rule of FunctionParallelContentionRule rules
			return validateAndCache(thresholdConfig);
		return functions;
	}
	
	private List<String> validateAndCache(ConfigMonitorThreshold thresholdConfig) {
		List<String> functions = new ArrayList<>();
		List<Integer> thresholds = new ArrayList<>();
		
		String paramValue = thresholdConfig.getCustomParameter(FUNCTIONS_PARAM_NAME);
		if (paramValue == null){
			logger.warn(FUNCTIONS_PARAM_NAME + " is missing on the " + RULE_NAME + " monitoring rule. Please add it. Rule threshold will be ignored.");
			this.functionsCache.put(thresholdConfig, functions); // empty list
			this.thresholdsCache.put(thresholdConfig, thresholds); // empty list
			return functions;
		}
		
		// validate and create functions AND thresholds
		StringTokenizer tokenizer = new StringTokenizer(paramValue, ",");
		while (tokenizer.hasMoreTokens()){
			String function = tokenizer.nextToken().trim();
			if (!function.isEmpty())
				functions.add(function);
		}
		
		if (functions.isEmpty()){
			logger.warn(FUNCTIONS_PARAM_NAME + " in the " + RULE_NAME + " monitoring rule contains an empty list of functions. Please set it correctly. Rule threshold will be ignored.");
			this.functionsCache.put(thresholdConfig, functions); // empty list
			this.thresholdsCache.put(thresholdConfig, thresholds); // empty list
			return functions;
		}
		
		paramValue = thresholdConfig.getCustomParameter(FUNCTIONS_APPEARANCE_THRESHOLDS_PARAM_NAME);
		if (paramValue == null){
			logger.warn(FUNCTIONS_APPEARANCE_THRESHOLDS_PARAM_NAME + " is missing on the " + RULE_NAME + " monitoring rule. Please add it. Rule threshold will be ignored.");
			functions.clear(); // invalidate
			this.functionsCache.put(thresholdConfig, functions); // empty list
			this.thresholdsCache.put(thresholdConfig, thresholds); // empty list
			return functions;
		}
		
		tokenizer = new StringTokenizer(paramValue, ",");
		while (tokenizer.hasMoreTokens()){
			String value = tokenizer.nextToken().trim();
			if (!value.isEmpty()){
				Integer threshold = Ints.tryParse(value);
				if (threshold == null){
					logger.warn(FUNCTIONS_APPEARANCE_THRESHOLDS_PARAM_NAME + " contains an invalid threshold value : " + value + ". Please set it correctly. Rule threshold will be ignored.");
					functions.clear(); // invalidate
					this.functionsCache.put(thresholdConfig, functions); // empty list
					this.thresholdsCache.put(thresholdConfig, thresholds); // empty list
					return functions;
				}
				thresholds.add(threshold);
			}
		}
		
		if (thresholds.isEmpty()){
			logger.warn(FUNCTIONS_APPEARANCE_THRESHOLDS_PARAM_NAME + " in the " + RULE_NAME + " monitoring rule contains an empty list of thresholds. Please set it correctly. Rule threshold will be ignored.");
			functions.clear(); // invalidate
			this.functionsCache.put(thresholdConfig, functions); // empty list
			this.thresholdsCache.put(thresholdConfig, thresholds); // empty list
			return functions;
		}
		
		if (functions.size() != thresholds.size()){
			logger.warn(FUNCTIONS_APPEARANCE_THRESHOLDS_PARAM_NAME + " in the " + RULE_NAME + " monitoring rule must match exactly the number of related function items. Please set it correctly. Rule threshold will be ignored.");
			functions.clear(); // invalidate
			thresholds.clear(); // invalidate
			this.functionsCache.put(thresholdConfig, functions); // empty list
			this.thresholdsCache.put(thresholdConfig, thresholds); // empty list
			return functions;
		}
		
		this.functionsCache.put(thresholdConfig, functions); // list with values
		this.thresholdsCache.put(thresholdConfig, thresholds); // list with values
		
		return functions;
	}
}
