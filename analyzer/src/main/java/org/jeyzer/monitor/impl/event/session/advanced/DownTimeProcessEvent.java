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
import org.jeyzer.monitor.engine.event.MonitorSessionEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;

public class DownTimeProcessEvent extends MonitorSessionEvent {

	public static final String EVENT_NAME = "High Process down time";

	private Date restartDate = null;
	private long downTime;
	
	public DownTimeProcessEvent(MonitorEventInfo info, ThreadDump dump) {
		super(EVENT_NAME, info);
		restartDate = dump.getTimestamp();
		downTime = dump.getTimeSlice();
	}

	@Override
	public void updateContext(ThreadDump dump) {
		// do nothing
	}

	@Override
	public void addPrintableExtraParameters(List<String> params) {
		params.add("Restart at");
		params.add(getPrintableDate(this.restartDate));
		params.add("Process down for");
		params.add(getPrintableDuration(downTime));
	}

	@Override
	protected void dumpExtraParameters(StringBuilder msg) {
		msg.append("Restart at       :" + getPrintableDate(this.restartDate) + "\n");
		msg.append("Process down for :" + getPrintableDuration(downTime) + "\n");
	}
}
