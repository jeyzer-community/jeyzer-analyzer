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

public class UpTimeProcessEvent extends MonitorSessionEvent {

	public static final String EVENT_NAME = "Process up time";
	
	private Date upTimeAlertDate = null;
	
	public UpTimeProcessEvent(MonitorEventInfo info, ThreadDump dump) {
		super(EVENT_NAME, info);
		upTimeAlertDate = dump.getTimestamp();
	}

	@Override
	public void updateContext(ThreadDump dump) {
		// do nothing
	}

	@Override
	public void addPrintableExtraParameters(List<String> params) {
		params.add("Up time reached at");
		params.add(getPrintableDate(this.upTimeAlertDate));
	}

	@Override
	protected void dumpExtraParameters(StringBuilder msg) {
		msg.append("Up time reached at :" + getPrintableDate(this.upTimeAlertDate) + "\n");
	}

}
