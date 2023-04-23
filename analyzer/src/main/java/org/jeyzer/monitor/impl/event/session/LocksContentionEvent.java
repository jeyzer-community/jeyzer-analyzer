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
import org.jeyzer.analyzer.data.stack.ThreadStack;
import org.jeyzer.monitor.engine.event.MonitorSessionEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;

public class LocksContentionEvent extends MonitorSessionEvent{

	public static final String EVENT_NAME = "Locks contention found";		

	private int maxLocksCount;
	
	public LocksContentionEvent(MonitorEventInfo info, ThreadDump dump) {
		super(EVENT_NAME, info);
		this.maxLocksCount = getLockedThreadsCount(dump);
	}

	@Override
	public void updateContext(ThreadDump dump) {
		int count = getLockedThreadsCount(dump);
		if (count >= maxLocksCount)
			maxLocksCount = count;
	}
	
	private int getLockedThreadsCount(ThreadDump dump) {
		int count = 0;

		for (ThreadStack stack : dump.getWorkingThreads()){
			if (stack.isLocked())
				count++;
		}
		return count;
	}

	@Override
	public void addPrintableExtraParameters(List<String> params) {
		params.add("Max number of locks detected");
		params.add(Integer.toString(this.maxLocksCount));
	}

	@Override
	protected void dumpExtraParameters(StringBuilder msg) {
		msg.append("Max number of locks detected :" + this.maxLocksCount + "\n");
	}

}
