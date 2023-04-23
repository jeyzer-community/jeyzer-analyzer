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
import org.jeyzer.monitor.engine.event.info.ApplicativeEventInfo;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;

public class ApplicativeSessionEvent extends MonitorSessionEvent implements MonitorApplicativeEvent {
	
	public static final String EVENT_NAME = "Applicative session";
	
	public ApplicativeSessionEvent(MonitorEventInfo info) {
		super(EVENT_NAME, info);
	}
	
	@Override
	public Date getApplicativeStartDate() {
		ApplicativeEventInfo appEventinfo = (ApplicativeEventInfo)this.info;
		return appEventinfo.getAppEventStart();
	}
	
	@Override
	public Date getApplicativeEndDate() {
		ApplicativeEventInfo appEventinfo = (ApplicativeEventInfo)this.info;
		return appEventinfo.getAppEventEnd();
	}

	@Override
	public void updateContext(ThreadDump dump) {
		// do nothing
	}

	@Override
	public void addPrintableExtraParameters(List<String> params) {
		ApplicativeEventInfo appEventinfo = (ApplicativeEventInfo)this.info;
		params.add("Applicative event id");
		params.add(appEventinfo.getAppEventId());
		
		if (appEventinfo.isAppEventOneshot()) {
			params.add("Applicative oneshot event date");
			params.add(getPrintableDate(appEventinfo.getAppEventStart()));
			
		}
		else {
			params.add("Applicative event start date");
			params.add(getPrintableDate(appEventinfo.getAppEventStart()));
			params.add("Applicative event end date");
			params.add(appEventinfo.getAppEventEnd() != null ? getPrintableDate(appEventinfo.getAppEventEnd()) : "In progress");
		}
		
		if (appEventinfo.getAppThreadId() != null && !appEventinfo.getAppThreadId().equals("-1")) {
			params.add("Applicative event thread id");
			params.add(appEventinfo.getAppThreadId());
		}
	}

	@Override
	protected void dumpExtraParameters(StringBuilder msg) {
		ApplicativeEventInfo appEventinfo = (ApplicativeEventInfo)this.info;
		msg.append("Applicative event id          : " + appEventinfo.getAppEventId());
		msg.append("Applicative event start date  : " + getPrintableDate(appEventinfo.getAppEventStart()));
		msg.append("Applicative event end date    : " + (appEventinfo.getAppEventEnd() != null ? getPrintableDate(appEventinfo.getAppEventEnd()) : "In progress"));
		if (appEventinfo.getAppThreadId() != null) 
			msg.append("Applicative event thread id   : " + appEventinfo.getAppThreadId());
		msg.append("Applicative event oneshot     : " + appEventinfo.isAppEventOneshot());
	}
}
