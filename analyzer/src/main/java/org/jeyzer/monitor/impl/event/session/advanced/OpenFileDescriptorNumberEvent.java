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

public class OpenFileDescriptorNumberEvent extends MonitorSessionEvent {

	public static final String EVENT_NAME = "High number of open file descriptors";
	
	protected long maxOpenFileDescriptors = 0;
	
	public OpenFileDescriptorNumberEvent(MonitorEventInfo info, ThreadDump dump) {
		super(EVENT_NAME, info);
		maxOpenFileDescriptors = dump.getProcessOpenFileDescriptorCount();
	}

	@Override
	public void updateContext(ThreadDump dump) {
		if (dump.getProcessOpenFileDescriptorCount() > this.maxOpenFileDescriptors)
			this.maxOpenFileDescriptors = dump.getProcessOpenFileDescriptorCount();
	}

	@Override
	public void addPrintableExtraParameters(List<String> params) {
		params.add("Highest open file descriptor number :");
		params.add(Long.toString(this.maxOpenFileDescriptors));
	}

	@Override
	protected void dumpExtraParameters(StringBuilder msg) {
		msg.append("Highest open file descriptor number :" + this.maxOpenFileDescriptors + "\n");
	}

}
