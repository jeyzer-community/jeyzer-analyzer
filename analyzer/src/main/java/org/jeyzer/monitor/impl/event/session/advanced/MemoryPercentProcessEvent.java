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

public class MemoryPercentProcessEvent extends MonitorSessionEvent {
	
	public static final String EVENT_NAME = "Memory consuming process";
	
	protected double maxMemory = 0;
	
	public MemoryPercentProcessEvent(MonitorEventInfo info, ThreadDump dump) {
		super(EVENT_NAME, info);
	}
	
	@Override
	public void updateContext(ThreadDump dump) {
		if (dump.getMemoryPools().getHeapUsage() >= maxMemory)
			maxMemory = dump.getMemoryPools().getHeapUsage();
	}

	@Override
	public void addPrintableExtraParameters(List<String> params) {
		params.add("Max memory heap %");
		params.add(Math.round(this.maxMemory) + "%");
	}

	@Override
	protected void dumpExtraParameters(StringBuilder msg) {
		msg.append("Max memory heap % :" + this.maxMemory + "\n");
	}
	
}
