package org.jeyzer.monitor.impl.event.task;

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


public class LockerThreadEvent extends MonitorTaskEvent {
	
	private static final String EVENT_NAME = "Contention lock";

	private int maxThreadLocks;
	
	public LockerThreadEvent(MonitorEventInfo info, ThreadAction action, ThreadStackHandler stack){
		super(EVENT_NAME, info, action, stack);
	}

	@Override
	public void updateContext(ThreadStack stack) {
		if (stack.getLockedThreads().size() >= maxThreadLocks)
			this.maxThreadLocks = stack.getLockedThreads().size();
	}

	@Override
	public void addPrintableExtraParameters(List<String> params) {
		params.add("Max number of locked threads detected");
		params.add(Integer.toString(this.maxThreadLocks));
	}

	@Override
	protected void dumpExtraParameters(StringBuilder msg) {
		msg.append("Max number of locked threads detected : " + this.maxThreadLocks + "\n");
	}

}
