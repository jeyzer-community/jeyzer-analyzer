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







import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.analyzer.data.stack.ThreadStack;
import org.jeyzer.monitor.config.engine.ConfigMonitorThreshold;
import org.jeyzer.monitor.engine.event.MonitorSessionCustomEvent;
import org.jeyzer.monitor.engine.event.MonitorSessionEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;

public class ActiveNamedThreadLimitEvent extends MonitorSessionEvent implements MonitorSessionCustomEvent{

	public static final String EVENT_NAME = "Active named thread limit reached";		

	private int maxNamedThreadCount;
	private String threadName; // can be null
	
	public ActiveNamedThreadLimitEvent(MonitorEventInfo info, ThreadDump dump, String poolName) {
		super(EVENT_NAME + " : " + poolName, info);
		this.threadName = poolName;
	}

	@Override
	public void updateContext(ThreadDump dump) {
		// do nothing
	}

	@Override
	public void updateContext(ThreadDump dump, ConfigMonitorThreshold thresholdConfig) {
		List<ThreadStack> activePoolThreads = getActivePoolThreads(dump, thresholdConfig.getPattern());
		if (activePoolThreads.size() >= maxNamedThreadCount)
			maxNamedThreadCount = activePoolThreads.size();
	}
	
	/**
	 * Get the list of active threads matching a thread name pattern (thread pool)
	 */
	private List<ThreadStack> getActivePoolThreads(ThreadDump td, Pattern pattern){
		List<ThreadStack> activeNamedThreads = new ArrayList<>();
		
		for (ThreadStack ts : td.getWorkingThreads()){
			Matcher matcher = pattern.matcher(ts.getName());
			if (matcher.find())
				activeNamedThreads.add(ts);
		}
		
		return activeNamedThreads;
	}
	
	@Override
	public String getNameExtraInfo(){
		return threadName;
	}

	@Override
	public void addPrintableExtraParameters(List<String> params) {
		params.add("Max number of active threads detected");
		params.add(Integer.toString(this.maxNamedThreadCount));
	}

	@Override
	protected void dumpExtraParameters(StringBuilder msg) {
		msg.append("Max number of active threads detected :" + this.maxNamedThreadCount + "\n");
	}

}
