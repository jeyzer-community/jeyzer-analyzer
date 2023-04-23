package org.jeyzer.monitor.impl.event.session;

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







import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.analyzer.data.stack.ThreadStack;
import org.jeyzer.monitor.config.engine.ConfigMonitorThreshold;
import org.jeyzer.monitor.engine.event.MonitorSessionCustomEvent;
import org.jeyzer.monitor.engine.event.MonitorSessionEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;

public class FunctionParallelContentionEvent extends MonitorSessionEvent implements MonitorSessionCustomEvent{

	public static final String EVENT_NAME = "Function parallel contention found";		

	private int maxFunctionCount;
	private String functionName; // can be null
	
	public FunctionParallelContentionEvent(MonitorEventInfo info, ThreadDump dump, String functionName) {
		super(EVENT_NAME + " : " + functionName, info);
		this.functionName = functionName;
	}

	@Override
	public void updateContext(ThreadDump dump) {
		// do nothing
	}

	@Override
	public void updateContext(ThreadDump dump, ConfigMonitorThreshold thresholdConfig) {
		int count = getFunctionMatchCount(dump, thresholdConfig.getPattern());
		if (count >= maxFunctionCount)
			maxFunctionCount = count;
	}
	
	private int getFunctionMatchCount(ThreadDump dump, Pattern pattern) {
		int count = 0;
		
		for (ThreadStack stack : dump.getWorkingThreads()){
			for (String function : stack.getFunctionTags()){
				Matcher matcher = pattern.matcher(function);
				if (matcher.find()){
					count++;
					break;
				}
			}
		}
		return count;
	}
	
	@Override
	public String getNameExtraInfo(){
		return functionName;
	}

	@Override
	public void addPrintableExtraParameters(List<String> params) {
		params.add("Max number of threads detected");
		params.add(Integer.toString(this.maxFunctionCount));
	}

	@Override
	protected void dumpExtraParameters(StringBuilder msg) {
		msg.append("Max number of threads detected :" + this.maxFunctionCount + "\n");
	}

}
