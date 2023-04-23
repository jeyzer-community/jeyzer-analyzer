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







import java.util.List;

import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.monitor.engine.event.MonitorSessionEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;

public class GarbageOldCollectionCountEvent extends MonitorSessionEvent {
	
	public static final String EVENT_NAME = "Excessive old GC execution";
	
	protected long gcOldCount = 0;
	
	public GarbageOldCollectionCountEvent(MonitorEventInfo info, ThreadDump dump) {
		super(EVENT_NAME, info);
	}

	@Override
	public void updateContext(ThreadDump dump) {
		if (dump.getGarbageCollection().getGcTime() > this.gcOldCount)
			this.gcOldCount = dump.getGarbageCollection().getOldGarbageCollectorInfo().getCollectionCount();
	}

	@Override
	public void addPrintableExtraParameters(List<String> params) {
		params.add("Max old GC collection count");
		params.add(Long.toString(this.gcOldCount));	
	}

	@Override
	protected void dumpExtraParameters(StringBuilder msg) {
		msg.append("Max old GC collection count :" + this.gcOldCount + "\n");
	}
	
}
