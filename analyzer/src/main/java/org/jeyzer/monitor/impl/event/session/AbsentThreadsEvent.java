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

public class AbsentThreadsEvent extends MonitorSessionEvent implements MonitorSessionCustomEvent{

	public static final String EVENT_NAME = "Absent threads";		

	private int threadsCount = Integer.MAX_VALUE;
	private String threadFamily; // can be null
	
	public AbsentThreadsEvent(MonitorEventInfo info, ThreadDump dump, String threadFamily) {
		super(EVENT_NAME + " : " + threadFamily, info);
		this.threadFamily = threadFamily;
	}

	@Override
	public void updateContext(ThreadDump dump) {
		// do nothing
	}

	@Override
	public void updateContext(ThreadDump dump, ConfigMonitorThreshold thresholdConfig) {
		List<ThreadStack> threads = getThreads(dump, thresholdConfig.getPattern());
		if (threads.size() <= threadsCount)
			threadsCount = threads.size();
	}
	
	/**
	 * Get the list of threads matching a thread name pattern
	 */
	private List<ThreadStack> getThreads(ThreadDump td, Pattern pattern){
		List<ThreadStack> threads = new ArrayList<>();
		
		for (ThreadStack ts : td.getThreads()){
			Matcher matcher = pattern.matcher(ts.getName());
			if (matcher.find())
				threads.add(ts);
		}
		
		return threads;
	}
	
	@Override
	public String getNameExtraInfo(){
		return threadFamily;
	}

	@Override
	public void addPrintableExtraParameters(List<String> params) {
		params.add("Lowest number of threads detected");
		params.add(Integer.toString(this.threadsCount));
	}

	@Override
	protected void dumpExtraParameters(StringBuilder msg) {
		msg.append("Lowest number of threads detected :" + this.threadsCount + "\n");
	}

}
