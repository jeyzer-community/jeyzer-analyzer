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

public class RecordingSnapshotCaptureTimeEvent extends MonitorSessionEvent {
	
	public static final String EVENT_NAME = "Recording snapshot capture time";
	
	protected double maxCaptureTime = 0;
	
	public RecordingSnapshotCaptureTimeEvent(MonitorEventInfo info, ThreadDump dump) {
		super(EVENT_NAME, info);
	}

	@Override
	public void updateContext(ThreadDump dump) {
		if (dump.getCaptureTime() > this.maxCaptureTime)
			this.maxCaptureTime = dump.getCaptureTime();
	}

	@Override
	public void addPrintableExtraParameters(List<String> params) {
		params.add("Max capture time");
		params.add(Math.round(this.maxCaptureTime) + " ms");
	}

	@Override
	protected void dumpExtraParameters(StringBuilder msg) {
		msg.append("Max capture time :" + this.maxCaptureTime + "\n");
	}
	
}
