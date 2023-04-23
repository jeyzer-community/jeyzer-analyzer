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
import org.jeyzer.monitor.config.engine.ConfigMonitorThreshold;
import org.jeyzer.monitor.engine.event.MonitorSessionCustomEvent;
import org.jeyzer.monitor.engine.event.MonitorSessionEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;

public class FunctionAndOperationParallelContentionEvent extends MonitorSessionEvent implements MonitorSessionCustomEvent{

	public static final String EVENT_NAME = "Function and operation parallel contention found";		

	private String functionName; // can be null
	
	public FunctionAndOperationParallelContentionEvent(MonitorEventInfo info, ThreadDump dump, String functionName) {
		super(EVENT_NAME + " : " + functionName, info);
		this.functionName = functionName;
	}

	@Override
	public void updateContext(ThreadDump dump) {
		// do nothing
	}

	@Override
	public void updateContext(ThreadDump dump, ConfigMonitorThreshold thresholdConfig) {
	}
	
	@Override
	public String getNameExtraInfo(){
		return functionName;
	}

	@Override
	public void addPrintableExtraParameters(List<String> params) {
	}

	@Override
	protected void dumpExtraParameters(StringBuilder msg) {
	}

}
