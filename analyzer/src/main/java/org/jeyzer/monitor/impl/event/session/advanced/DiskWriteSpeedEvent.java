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
import org.jeyzer.analyzer.math.FormulaHelper;
import org.jeyzer.monitor.engine.event.MonitorSessionEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;

public class DiskWriteSpeedEvent extends MonitorSessionEvent {
	
	public static final String EVENT_NAME = "Disk write speed";
	
	protected long diskWriteMinSpeed = Long.MAX_VALUE;
	
	public DiskWriteSpeedEvent(MonitorEventInfo info, ThreadDump dump) {
		super(EVENT_NAME, info);
	}

	@Override
	public void updateContext(ThreadDump dump) {
		long size = dump.getWriteSize();
		long time = dump.getWriteTime();
		
		if (size == -1 || time == -1 || time == 0)
			return;
		
		// do calculate in Kb / sec
		long speed = FormulaHelper.convertToKb((size / time) * 1000L);
		
		if (speed < this.diskWriteMinSpeed)
			this.diskWriteMinSpeed = speed;
	}

	@Override
	public void addPrintableExtraParameters(List<String> params) {
		params.add("Lowest recording write speed");
		params.add(Long.toString(this.diskWriteMinSpeed) + " Kb/sec");	
	}

	@Override
	protected void dumpExtraParameters(StringBuilder msg) {
		msg.append("Lowest recording write speed :" + this.diskWriteMinSpeed + " Kb/sec\n");
	}
	
}
