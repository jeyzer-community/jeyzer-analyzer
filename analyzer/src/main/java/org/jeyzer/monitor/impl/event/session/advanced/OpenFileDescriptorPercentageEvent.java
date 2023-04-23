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

public class OpenFileDescriptorPercentageEvent extends MonitorSessionEvent {

	public static final String EVENT_NAME = "High open file descriptor usage";
	
	protected long maxOpenFilePercentage = 0;
	
	public OpenFileDescriptorPercentageEvent(MonitorEventInfo info, ThreadDump dump) {
		super(EVENT_NAME, info);
		maxOpenFilePercentage = dump.getProcessOpenFileDescriptorUsage();
	}

	@Override
	public void updateContext(ThreadDump dump) {
		if (dump.getProcessOpenFileDescriptorUsage() > this.maxOpenFilePercentage)
			this.maxOpenFilePercentage = dump.getProcessOpenFileDescriptorUsage();
	}

	@Override
	public void addPrintableExtraParameters(List<String> params) {
		params.add("Highest open file descriptor percentage :");
		params.add(Long.toString(this.maxOpenFilePercentage));
	}

	@Override
	protected void dumpExtraParameters(StringBuilder msg) {
		msg.append("Highest open file descriptor percentage :" + this.maxOpenFilePercentage + "\n");
	}

}
