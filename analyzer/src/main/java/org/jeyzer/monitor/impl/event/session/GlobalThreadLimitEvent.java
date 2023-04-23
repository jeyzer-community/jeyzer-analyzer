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


public class GlobalThreadLimitEvent extends MonitorSessionEvent {

	private static final String EVENT_NAME = "Global thread limit reached";	
	
	private int threadNumber;
	
	public GlobalThreadLimitEvent(MonitorEventInfo info, ThreadDump dump) {
		super(EVENT_NAME, info);
		this.threadNumber = dump.size();
	}

	@Override
	public void updateContext(ThreadDump dump) {
		if (dump.size() > this.threadNumber)
			this.threadNumber = dump.size();
	}

	@Override
	public void addPrintableExtraParameters(List<String> params) {
		params.add("Max number of active threads detected");
		params.add(Integer.toString(this.threadNumber));
	}

	@Override
	protected void dumpExtraParameters(StringBuilder msg) {
		msg.append("Max number of active threads detected :" + this.threadNumber + "\n");
	}

}
