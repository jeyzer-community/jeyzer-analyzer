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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jeyzer.analyzer.data.ThreadAction;
import org.jeyzer.analyzer.data.stack.ThreadStack;
import org.jeyzer.analyzer.data.stack.ThreadStack.ThreadStackHandler;
import org.jeyzer.monitor.engine.event.MonitorTaskEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;
import org.jeyzer.monitor.engine.event.info.MonitorEventWithContextInfo;


public class VirtualThreadLeakTaskEvent extends MonitorTaskEvent {

	private static final String EVENT_NAME = "Virtual thread leak suspicion";
	
	public static final String CONTEXT_KEY_TID_LEAK_SUSPECTS = "TidLeakSuspects";
	
	private Set<String> leakSuspects;
	
	public VirtualThreadLeakTaskEvent(MonitorEventInfo info, ThreadAction action, ThreadStackHandler stack) {
		super(EVENT_NAME, info, action, stack);
	}	

	@SuppressWarnings("unchecked")
	@Override
	public void updateContext(ThreadStack stack) {
		MonitorEventWithContextInfo infoCtx = (MonitorEventWithContextInfo)this.info;
		
		Map<String, Object> context = infoCtx.getContext();
		if (context == null)
			return;

		// take the latest version of leak suspects
		leakSuspects = (Set<String>)context.get(CONTEXT_KEY_TID_LEAK_SUSPECTS);
	}
	
	@Override
	public void addPrintableExtraParameters(List<String> params) {
		params.add("Unmounted virtual thread leak suspects");
		params.add(getOrderedSuspects());
	}

	@Override
	protected void dumpExtraParameters(StringBuilder msg) {
		msg.append("Unmounted virtual thread leak suspects : " + getOrderedSuspects() + "\n");
	}
	
	private String getOrderedSuspects(){
		if (leakSuspects == null || leakSuspects.isEmpty())
			throw new RuntimeException("Virtual Thread Leak Task event should have leak suspects");

		List<String> suspects = new ArrayList<>();
		suspects.addAll(leakSuspects);
		Collections.sort(suspects);
		
		String display = suspects.toString();
		display = display.replace('[',' ');
		display = display.trim();
		display = display.replace(']',' ');
		
		return display;
	}
}

