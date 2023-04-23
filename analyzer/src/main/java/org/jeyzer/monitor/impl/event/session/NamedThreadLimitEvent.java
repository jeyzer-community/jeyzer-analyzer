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

public class NamedThreadLimitEvent extends MonitorSessionEvent implements MonitorSessionCustomEvent{

	public static final String EVENT_NAME = "Named thread limit reached";		

	private int maxThreadCount;
	private String threadName; // can be null
	
	public NamedThreadLimitEvent(MonitorEventInfo info, ThreadDump dump, String threadName) {
		super(EVENT_NAME + " : " + threadName, info);
		this.threadName = threadName;
	}

	@Override
	public void updateContext(ThreadDump dump) {
		// do nothing
	}

	@Override
	public void updateContext(ThreadDump dump, ConfigMonitorThreshold thresholdConfig) {
		List<ThreadStack> namedThreads = getNamedThreads(dump, thresholdConfig.getPattern());
		if (namedThreads.size() >= maxThreadCount)
			maxThreadCount = namedThreads.size();
	}
	
	/**
	 * Get the list of threads matching a thread name pattern
	 */
	private List<ThreadStack> getNamedThreads(ThreadDump td, Pattern pattern){
		List<ThreadStack> namedThreads = new ArrayList<>();
		
		for (ThreadStack ts : td.getThreads()){
			Matcher matcher = pattern.matcher(ts.getName());
			if (matcher.find())
				namedThreads.add(ts);
		}
		
		return namedThreads;
	}
	
	@Override
	public String getNameExtraInfo(){
		return threadName;
	}

	@Override
	public void addPrintableExtraParameters(List<String> params) {
		params.add("Max number of active and inactive threads detected");
		params.add(Integer.toString(this.maxThreadCount));
	}

	@Override
	protected void dumpExtraParameters(StringBuilder msg) {
		msg.append("Max number of active and inactive threads detected : " + this.maxThreadCount + "\n");
	}

}
