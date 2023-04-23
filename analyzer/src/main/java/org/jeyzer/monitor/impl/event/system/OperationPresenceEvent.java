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

import org.jeyzer.analyzer.data.tag.OperationTag;
import org.jeyzer.analyzer.data.tag.Tag;
import org.jeyzer.analyzer.session.JzrSession;
import org.jeyzer.monitor.engine.event.MonitorSystemEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;

import com.google.common.collect.Multiset;

public class OperationPresenceEvent extends MonitorSystemEvent {

	private String operation;
	private long maxCount;
	
	public OperationPresenceEvent(String eventName, MonitorEventInfo info, String operation) {
		super(eventName, info);
		this.operation = operation;
		this.maxCount = 0;
	}
	
	@Override
	public void updateContext(JzrSession session) {
		Multiset<Tag> operationTags = session.getOperationSet();
		Tag operationTag = new OperationTag(operation);
    	int tagCount = operationTags.count(operationTag);
    	if (tagCount > maxCount)
    		maxCount = tagCount;
	}

	@Override
	protected void dumpExtraParameters(StringBuilder msg) {
		msg.append(operation + " :" + this.maxCount + "\n");
	}
	
	@Override
	public void addPrintableExtraParameters(List<String> params) {
		params.add(operation);
		params.add(Long.toString(this.maxCount));
	}

}
