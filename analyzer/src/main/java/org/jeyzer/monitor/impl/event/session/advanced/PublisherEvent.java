package org.jeyzer.monitor.impl.event.session.advanced;

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



import java.util.Date;





import java.util.List;

import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.monitor.engine.event.MonitorApplicativeEvent;
import org.jeyzer.monitor.engine.event.MonitorSessionEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;
import org.jeyzer.monitor.engine.event.info.PublisherEventInfo;

public class PublisherEvent extends MonitorSessionEvent implements MonitorApplicativeEvent {
	
	public static final String EVENT_NAME = "Jeyzer publisher";
	
	public PublisherEvent(MonitorEventInfo info) {
		super(EVENT_NAME, info);
	}
	
	@Override
	public Date getApplicativeStartDate() {
		PublisherEventInfo pubEventinfo = (PublisherEventInfo)this.info;
		return pubEventinfo.getPublisherEventTime();
	}
	
	@Override
	public Date getApplicativeEndDate() {
		PublisherEventInfo pubEventinfo = (PublisherEventInfo)this.info;
		return pubEventinfo.getPublisherEventTime();
	}

	@Override
	public void updateContext(ThreadDump dump) {
		// do nothing
	}

	@Override
	public void addPrintableExtraParameters(List<String> params) {
		PublisherEventInfo pubEventinfo = (PublisherEventInfo)this.info;
		params.add("Applicative event start date");
		params.add(getPrintableDate(pubEventinfo.getPublisherEventTime()));
	}

	@Override
	protected void dumpExtraParameters(StringBuilder msg) {
		PublisherEventInfo pubEventinfo = (PublisherEventInfo)this.info;
		msg.append("Applicative event start date  : " + getPrintableDate(pubEventinfo.getPublisherEventTime()));
	}
}
