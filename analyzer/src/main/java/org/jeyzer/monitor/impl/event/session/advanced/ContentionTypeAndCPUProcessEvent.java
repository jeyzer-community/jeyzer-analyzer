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


import java.util.ArrayList;
import java.util.List;

import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.monitor.config.engine.ConfigMonitorThreshold;
import org.jeyzer.monitor.engine.event.MonitorSessionCustomEvent;
import org.jeyzer.monitor.engine.event.MonitorSessionEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;

public class ContentionTypeAndCPUProcessEvent extends MonitorSessionEvent implements MonitorSessionCustomEvent{
	
	public static final String EVENT_NAME = "CPU peak and contention";
	
	protected List<Double> usedCPUProcessPercents = new ArrayList<>();
	
	public ContentionTypeAndCPUProcessEvent(MonitorEventInfo info, ThreadDump dump) {
		super(EVENT_NAME, info);
	}
	
	private String getMaxProcessCPU() {
		double max = 0;
		for (Double value : this.usedCPUProcessPercents){
			if (value > max)
				max = value;
		}
		return Math.round(max) + "%";
	}

	@Override
	public void updateContext(ThreadDump dump) {
		usedCPUProcessPercents.add(dump.getProcessCpu());
	}

	@Override
	public void updateContext(ThreadDump dump, ConfigMonitorThreshold thresholdCfg) {
		updateContext(dump);
	}

	@Override
	public void addPrintableExtraParameters(List<String> params) {
		params.add("Max process CPU used");
		params.add(getMaxProcessCPU());
	}

	@Override
	protected void dumpExtraParameters(StringBuilder msg) {
		msg.append("Max process CPU used :" + getMaxProcessCPU() + "\n");
	}	
}
