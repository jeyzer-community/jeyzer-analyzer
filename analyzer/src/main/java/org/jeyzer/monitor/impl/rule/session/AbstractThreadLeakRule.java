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



import static org.jeyzer.monitor.config.engine.ConfigMonitorThreshold.THRESHOLD_SESSION_CUSTOM_WITH_CONTEXT;







import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.monitor.config.engine.ConfigMonitorRule;
import org.jeyzer.monitor.config.engine.ConfigMonitorThreshold;
import org.jeyzer.monitor.engine.rule.MonitorSessionRule;
import org.jeyzer.monitor.engine.rule.condition.session.CustomWithContextSessionProvider;

public abstract class AbstractThreadLeakRule extends MonitorSessionRule implements CustomWithContextSessionProvider {
	
	public static final String DELTA_X_PARAM_NAME = "delta_x";
	public static final String DELTA_Y_PARAM_NAME = "delta_y";
	
	public static final String CONTEXT_THREAD_DUMP_COUNT = "thread count";
	public static final String CONTEXT_NEXT_THREAD_REF_SIZE = "next thread ref size";
	
	public static final int DEFAULT_X_VALUE = 10;
	public static final int DEFAULT_Y_VALUE = 5;
	
	public AbstractThreadLeakRule(ConfigMonitorRule def, String ruleName, String conditionDescription) throws JzrInitializationException {
		super(def, ruleName, conditionDescription);
	}
	
	public abstract int getThreadsCount(ThreadDump dump, ConfigMonitorThreshold thresholdConfig);

	@Override
	public boolean matchCustomParametersWithContext(ThreadDump dump, ConfigMonitorThreshold thresholdConfig, Map<String, Object> context) {
		int deltaX = getValue(thresholdConfig, DELTA_X_PARAM_NAME, DEFAULT_X_VALUE);
		int deltaY = getValue(thresholdConfig, DELTA_Y_PARAM_NAME, DEFAULT_Y_VALUE);
		int threadsCount = getThreadsCount(dump, thresholdConfig);
		boolean result = threadsCount >= thresholdConfig.getValue();

		if (!result)
			return noMatch(context);
		
		// check now the context
		Integer tdCount = (Integer)context.get(CONTEXT_THREAD_DUMP_COUNT);
		Integer nextRefSize = (Integer)context.get(CONTEXT_NEXT_THREAD_REF_SIZE);
		
		if (tdCount == null) // first time
			return firstMatch(context, threadsCount + deltaY);

		if ((tdCount % deltaX) == 0)  // time to check that value has increased over deltaX thread dumps
			if (threadsCount >= nextRefSize)
				updateContext(context, tdCount, threadsCount + deltaY); // next y to check will be higher
			else
				return noMatch(context);
		else
			context.put(CONTEXT_THREAD_DUMP_COUNT, ++tdCount); // simply increment the thread dump count
		
		return true;
	}

	private void updateContext(Map<String, Object> context, Integer tdCount, Integer nextRefCount) {
		tdCount++;
		context.put(CONTEXT_THREAD_DUMP_COUNT, tdCount);
		context.put(CONTEXT_NEXT_THREAD_REF_SIZE, nextRefCount);
	}

	private boolean noMatch(Map<String, Object> context) {
		context.put(CONTEXT_THREAD_DUMP_COUNT, null);
		context.put(CONTEXT_NEXT_THREAD_REF_SIZE, null);
		return false;
	}
	
	private boolean firstMatch(Map<String, Object> context, Integer nextRefCount) {
		Integer tdCount = 1;
		context.put(CONTEXT_THREAD_DUMP_COUNT, tdCount);
		context.put(CONTEXT_NEXT_THREAD_REF_SIZE, nextRefCount);
		return true;
	}	

	@Override
	public boolean isAdvancedMonitoringBased() {
		return false;
	}

	@Override
	protected List<String> getSupportedThresholdTypes(){
		return Arrays.asList(THRESHOLD_SESSION_CUSTOM_WITH_CONTEXT);
	}
	
	private int getValue(ConfigMonitorThreshold thresholdConfig, String paramName, int defaultValue) {
		int value;
		
		String paramValue = thresholdConfig.getCustomParameter(paramName);
		if (paramValue == null)
			return defaultValue;
		
		try {
			value = Integer.parseInt(paramValue);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
		
		return value > 0 ? value : defaultValue;
	}

}
