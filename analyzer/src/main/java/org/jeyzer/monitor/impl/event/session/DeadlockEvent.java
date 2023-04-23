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

public class DeadlockEvent extends MonitorSessionEvent {
	
	public static final String EVENT_NAME = "Deadlock";
	
	private int deadlockCount = 0;
	
	public DeadlockEvent(MonitorEventInfo info, ThreadDump dump) {
		super(EVENT_NAME, info);
	}

	public void update(ThreadDump td){
		this.info.updateEnd(td.getTimestamp());
	}

	@Override
	public void updateContext(ThreadDump dump) {
		if (dump.hasDeadLock() > this.deadlockCount)
			this.deadlockCount = dump.hasDeadLock();
	}

	@Override
	public void addPrintableExtraParameters(List<String> params) {
		params.add("Max deadlocks detected");
		params.add(Long.toString(this.deadlockCount));
	}

	@Override
	protected void dumpExtraParameters(StringBuilder msg) {
		msg.append("Max deadlocks detected :" + this.deadlockCount + "\n");
	}
}
