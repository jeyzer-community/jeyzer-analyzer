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

import org.jeyzer.analyzer.data.tag.FunctionTag;
import org.jeyzer.analyzer.data.tag.Tag;
import org.jeyzer.analyzer.session.JzrSession;
import org.jeyzer.monitor.engine.event.MonitorSystemEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;

import com.google.common.collect.Multiset;

public class FunctionPresenceEvent extends MonitorSystemEvent {

	private String function;
	private long maxCount;
	
	public FunctionPresenceEvent(String eventName, MonitorEventInfo info, String function) {
		super(eventName, info);
		this.function = function;
		maxCount = 0;  
	}
	
	@Override
	public void updateContext(JzrSession session) {
		Multiset<Tag> functionTags = session.getFunctionSet();
		Tag functionTag = new FunctionTag(function);
    	int tagCount = functionTags.count(functionTag);
    	if (tagCount > maxCount)
    		maxCount = tagCount;
	}

	@Override
	protected void dumpExtraParameters(StringBuilder msg) {
		msg.append(function + " :" + this.maxCount + "\n");
	}
	
	@Override
	public void addPrintableExtraParameters(List<String> params) {
		params.add(function);
		params.add(Long.toString(this.maxCount));
	}

}
