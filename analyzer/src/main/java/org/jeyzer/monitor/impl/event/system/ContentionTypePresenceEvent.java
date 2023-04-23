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

import org.jeyzer.analyzer.data.tag.ContentionTypeTag;
import org.jeyzer.analyzer.data.tag.Tag;
import org.jeyzer.analyzer.session.JzrSession;
import org.jeyzer.monitor.engine.event.MonitorSystemEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;

import com.google.common.collect.Multiset;

public class ContentionTypePresenceEvent extends MonitorSystemEvent {

	private String contentionType;
	private long maxCount;
	
	public ContentionTypePresenceEvent(String eventName, MonitorEventInfo info, String operation) {
		super(eventName, info);
		this.contentionType = operation;
		this.maxCount = 0;
	}
	
	@Override
	public void updateContext(JzrSession session) {
		Multiset<Tag> contentionTypeTags = session.getContentionTypeSet();
		Tag contentionTypeTag = new ContentionTypeTag(contentionType);
    	int tagCount = contentionTypeTags.count(contentionTypeTag);
    	if (tagCount > maxCount)
    		maxCount = tagCount;
	}

	@Override
	protected void dumpExtraParameters(StringBuilder msg) {
		msg.append(contentionType + " :" + this.maxCount + "\n");
	}
	
	@Override
	public void addPrintableExtraParameters(List<String> params) {
		params.add(contentionType);
		params.add(Long.toString(this.maxCount));
	}

}
