package org.jeyzer.monitor.impl.event.session.advanced;

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

import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.monitor.engine.event.MonitorSessionEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;

public class VirtualThreadsCPUPercentEvent extends MonitorSessionEvent {
	
	public static final String EVENT_NAME = "Virtual threads high CPU usage";
	
	protected double maxCPU = 0;
	
	public VirtualThreadsCPUPercentEvent(MonitorEventInfo info, ThreadDump dump) {
		super(EVENT_NAME, info);
	}
	
	@Override
	public void updateContext(ThreadDump dump) {
		double cpuUsage = dump.getVirtualThreads().getMountedCpuUsagePercent();
		if (cpuUsage > this.maxCPU)
			this.maxCPU = cpuUsage;
	}

	@Override
	public void addPrintableExtraParameters(List<String> params) {
		params.add("Max CPU usage");
		params.add(Math.round(this.maxCPU) + "%");
	}

	@Override
	protected void dumpExtraParameters(StringBuilder msg) {
		msg.append("Max CPU usage :" + Math.round(this.maxCPU) + "\n");
	}
	
}
