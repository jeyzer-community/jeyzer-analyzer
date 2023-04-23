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

public class OperationParallelContentionEvent extends MonitorSessionEvent implements MonitorSessionCustomEvent{

	public static final String EVENT_NAME = "Operation parallel contention found";

	private int maxOperationCount;
	private String operationName; // can be null
	
	public OperationParallelContentionEvent(MonitorEventInfo info, ThreadDump dump, String operationName) {
		super(EVENT_NAME + " : " + operationName, info);
		this.operationName = operationName;
	}

	@Override
	public void updateContext(ThreadDump dump) {
		// do nothing
	}

	@Override
	public void updateContext(ThreadDump dump, ConfigMonitorThreshold thresholdConfig) {
		int count = getOperationMatchCount(dump, thresholdConfig.getPattern());
		if (count >= maxOperationCount)
			maxOperationCount = count;
	}
	
	private int getOperationMatchCount(ThreadDump dump, Pattern pattern) {
		int count = 0;
		
		for (ThreadStack stack : dump.getWorkingThreads()){
			for (String operation : stack.getOperationTags()){
				Matcher matcher = pattern.matcher(operation);
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
		return operationName;
	}

	@Override
	public void addPrintableExtraParameters(List<String> params) {
		params.add("Max number of threads detected");
		params.add(Integer.toString(this.maxOperationCount));
	}

	@Override
	protected void dumpExtraParameters(StringBuilder msg) {
		msg.append("Max number of threads detected :" + this.maxOperationCount + "\n");
	}

}
