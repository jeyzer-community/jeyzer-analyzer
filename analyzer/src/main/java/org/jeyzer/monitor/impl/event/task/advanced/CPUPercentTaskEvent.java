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

public class CPUPercentTaskEvent extends MonitorTaskEvent {
	
	public static final String EVENT_NAME = "CPU consuming task";
	
	protected double maxCpu = 0;
	
	public CPUPercentTaskEvent(MonitorEventInfo info, ThreadAction action, ThreadStackHandler stack) {
		super(EVENT_NAME, info, action, stack);
	}

	@Override
	public void updateContext(ThreadStack stack) {
		if (stack.getCpuInfo().getCpuUsage() > this.maxCpu)
			this.maxCpu = stack.getCpuInfo().getCpuUsage();
	}
	
	@Override
	public void addPrintableExtraParameters(List<String> params) {
		params.add("Max CPU %");
		params.add(Integer.toString((int)Math.round(this.maxCpu)));
	}

	@Override
	protected void dumpExtraParameters(StringBuilder msg) {
		msg.append("Max CPU : " + this.maxCpu + " %\n");		
	}	
}
