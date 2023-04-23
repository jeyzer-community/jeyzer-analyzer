package org.jeyzer.monitor.impl.event.task.advanced;

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

import org.jeyzer.analyzer.data.ThreadAction;
import org.jeyzer.analyzer.data.stack.ThreadStack;
import org.jeyzer.analyzer.data.stack.ThreadStack.ThreadStackHandler;
import org.jeyzer.monitor.engine.event.MonitorTaskEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;

public class MemoryPercentTaskEvent extends MonitorTaskEvent {
	
	public static final String EVENT_NAME = "Memory consuming task";
	
	protected double maxMemory = 0;
	
	public MemoryPercentTaskEvent(MonitorEventInfo info, ThreadAction action, ThreadStackHandler stack) {
		super(EVENT_NAME, info, action, stack);
	}
	
	@Override
	public void updateContext(ThreadStack stack) {
		if (stack.getMemoryInfo().getHeapAllocationPercentage() > this.maxMemory)
			this.maxMemory = stack.getMemoryInfo().getHeapAllocationPercentage();
	}

	@Override
	public void addPrintableExtraParameters(List<String> params) {
		params.add("Max heap usage %");
		params.add(Long.toString(Math.round(this.maxMemory)));
	}

	@Override
	protected void dumpExtraParameters(StringBuilder msg) {
		msg.append("Max heap usage : " + Long.toString(Math.round(this.maxMemory)) + " %\n");
	}
}
