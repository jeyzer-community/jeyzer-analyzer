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

import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.monitor.engine.event.MonitorSessionEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;

public class SuspendedThreadsEvent extends MonitorSessionEvent {
	
	public static final String EVENT_NAME = "Suspended threads";
	
	private int suspendedThreadsCount = 0;
	
	public SuspendedThreadsEvent(MonitorEventInfo info, ThreadDump dump) {
		super(EVENT_NAME, info);
	}

	public void update(ThreadDump td){
		this.info.updateEnd(td.getTimestamp());
	}

	@Override
	public void updateContext(ThreadDump dump) {
		if (dump.hasSuspendedThreads() > this.suspendedThreadsCount)
			this.suspendedThreadsCount = dump.hasSuspendedThreads();
	}

	@Override
	public void addPrintableExtraParameters(List<String> params) {
		params.add("Max suspended threads detected");
		params.add(Long.toString(this.suspendedThreadsCount));
	}

	@Override
	protected void dumpExtraParameters(StringBuilder msg) {
		msg.append("Max suspended threads detected :" + this.suspendedThreadsCount + "\n");
	}
}
