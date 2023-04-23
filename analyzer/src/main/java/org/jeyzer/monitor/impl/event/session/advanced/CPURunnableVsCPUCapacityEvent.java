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

import org.jeyzer.analyzer.config.ConfigThreadLocal;
import org.jeyzer.analyzer.data.ProcessCard;
import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.monitor.engine.event.MonitorSessionEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;

import com.google.common.primitives.Ints;

public class CPURunnableVsCPUCapacityEvent extends MonitorSessionEvent {
	
	public static final String EVENT_NAME = "System running out of CPUs";
	
	protected int maxCPURunnables = 0;
	protected int systemCPUs = -1;
	
	public CPURunnableVsCPUCapacityEvent(MonitorEventInfo info) {
		super(EVENT_NAME, info);
		this.systemCPUs = getAvailableProcessors();
	}

	@Override
	public void updateContext(ThreadDump dump) {
		if (dump.getCPURunnableThreadsCount() > maxCPURunnables)
			this.maxCPURunnables = dump.getCPURunnableThreadsCount();
	}

	@Override
	public void addPrintableExtraParameters(List<String> params) {
		params.add("Max runnable CPUs vs available CPUs");
		params.add(this.maxCPURunnables + " / " + this.systemCPUs);
	}

	@Override
	protected void dumpExtraParameters(StringBuilder msg) {
		msg.append("Max runnable CPUs vs available CPUs :" + this.maxCPURunnables + " / " + this.systemCPUs + "\n");		
	}

	private int getAvailableProcessors() {
		String availableProcessors = ConfigThreadLocal.get(ProcessCard.AVAILABLE_PROCESSORS);
		if (availableProcessors != null && !availableProcessors.isEmpty()) {
			Integer parsedValue = Ints.tryParse(availableProcessors);
			if (parsedValue != null)
				return parsedValue;
		}
		
		// Try JFR data
		availableProcessors = ConfigThreadLocal.get(ProcessCard.JFR_AVAILABLE_PROCESSORS);
		if (availableProcessors != null && !availableProcessors.isEmpty()) {
			Integer parsedValue = Ints.tryParse(availableProcessors);
			if (parsedValue != null)
				return parsedValue;	
		}
		
		return -1;
	}
}
