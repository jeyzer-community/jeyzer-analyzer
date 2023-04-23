package org.jeyzer.monitor.impl.event.session;

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

import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.monitor.engine.event.MonitorSessionEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;


public class HiatusTimeEvent extends MonitorSessionEvent {

	private static final String EVENT_NAME = "Hiatus time reached";	
	
	public HiatusTimeEvent(MonitorEventInfo info, ThreadDump dump) {
		super(EVENT_NAME, info);
		// no need to store hiatus time as it corresponds to event end time - start time
	}

	@Override
	public void updateContext(ThreadDump dump) {
		// nothing to do
	}

	@Override
	public void addPrintableExtraParameters(List<String> params) {
		// nothing to do
	}

	@Override
	protected void dumpExtraParameters(StringBuilder msg) {
		// nothing to do
	}

}
