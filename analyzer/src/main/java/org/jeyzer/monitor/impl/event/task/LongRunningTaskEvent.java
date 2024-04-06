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
import org.jeyzer.monitor.engine.event.MonitorTaskEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;


public class LongRunningTaskEvent extends MonitorTaskEvent {

	public static final String EVENT_NAME = "Long running task";
	
	public LongRunningTaskEvent(MonitorEventInfo info, ThreadAction action) {
		super(EVENT_NAME, info, action);
	}
	
	@Override
	public void updateContext(ThreadStack stack) {
		// Update the operation : take the latest one
		this.operation = stack.getPrincipalOperation();
		if (this.operation == null || this.operation.isEmpty()){
			this.operation = "OTBI";
		}
		
		// Update the contention type
		this.contentionType = stack.getPrincipalContentionType();
		if (this.contentionType == null || this.contentionType.isEmpty()){
			this.contentionType = "CTTBI";
		}
	}

	@Override
	public void addPrintableExtraParameters(List<String> params) {
		// Nothing to do
	}

	@Override
	protected void dumpExtraParameters(StringBuilder msg) {
		// Nothing to do
	}
	
}