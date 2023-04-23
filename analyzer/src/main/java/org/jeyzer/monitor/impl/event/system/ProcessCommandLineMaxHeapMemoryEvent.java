package org.jeyzer.monitor.impl.event.system;

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

import org.jeyzer.analyzer.data.ProcessCommandLine;
import org.jeyzer.analyzer.session.JzrSession;
import org.jeyzer.monitor.engine.event.MonitorSystemEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;
import org.jeyzer.monitor.util.CommanLineHelper;

public class ProcessCommandLineMaxHeapMemoryEvent extends MonitorSystemEvent {

	public static final String EVENT_NAME = "Process command line max heap size too low";
	
	private long minMemory = Long.MAX_VALUE;
	
	public ProcessCommandLineMaxHeapMemoryEvent(MonitorEventInfo info) {
		super(EVENT_NAME, info);
	}
	
	@Override
	public void updateContext(JzrSession session) {
		ProcessCommandLine commandLine = session.getProcessCommandLine();
		if  (commandLine == null)
			return; // should not happen
		
		String param = commandLine.getParameter(CommanLineHelper.MAX_HEAP_PARAM);
		if (param == null)
			return;
		
		long maxHeapSize = CommanLineHelper.parseHeapSize(param);
		
		if (maxHeapSize < minMemory)
			minMemory = maxHeapSize; 
	}

	@Override
	protected void dumpExtraParameters(StringBuilder msg) {
		msg.append("Command line max memory heap size (-Xmx) :" + this.minMemory + "\n");
	}
	
	@Override
	public void addPrintableExtraParameters(List<String> params) {
		params.add("Command line max memory heap size (-Xmx)");
		params.add(Math.round(this.minMemory) + " Mb");
	}

}
