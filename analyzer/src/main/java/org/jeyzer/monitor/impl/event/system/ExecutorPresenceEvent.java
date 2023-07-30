package org.jeyzer.monitor.impl.event.system;

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

import org.jeyzer.analyzer.session.JzrSession;
import org.jeyzer.monitor.engine.event.MonitorSystemEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;

import com.google.common.collect.Multiset;

public class ExecutorPresenceEvent extends MonitorSystemEvent {

	private String executor;
	private long maxCount;
	
	public ExecutorPresenceEvent(String eventName, MonitorEventInfo info, String executor) {
		super(eventName, info);
		this.executor = executor;
		maxCount = 0;  
	}
	
	@Override
	public void updateContext(JzrSession session) {
		Multiset<String> executors = session.getExecutorSet();
    	int tagCount = executors.count(this.executor);
    	if (tagCount > maxCount)
    		maxCount = tagCount;
	}

	@Override
	protected void dumpExtraParameters(StringBuilder msg) {
		msg.append(executor + " :" + this.maxCount + "\n");
	}
	
	@Override
	public void addPrintableExtraParameters(List<String> params) {
		params.add(executor);
		params.add(Long.toString(this.maxCount));
	}

}
