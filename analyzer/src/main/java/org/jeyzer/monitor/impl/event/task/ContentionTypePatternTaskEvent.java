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

public class ContentionTypePatternTaskEvent extends MonitorTaskEvent {

	private static final String EVENT_NAME = "Contention type pattern found : ";
	
	public ContentionTypePatternTaskEvent(MonitorEventInfo info, ThreadAction action, ThreadStackHandler stack, String contentionName) {
		super(EVENT_NAME + contentionName, info, action, stack);
	}	

	@Override
	public void updateContext(ThreadStack stack) {
		// do nothing, not called
	}

	@Override
	public void addPrintableExtraParameters(List<String> params) {
		// nothing to add
	}
	
	@Override
	protected void dumpExtraParameters(StringBuilder msg) {
		// nothing to add
	}
	
}
