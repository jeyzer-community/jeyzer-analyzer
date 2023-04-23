package org.jeyzer.monitor.impl.event.analyzer;

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

import org.jeyzer.monitor.engine.event.MonitorAnalyzerEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;

public class ToolErrorEvent extends MonitorAnalyzerEvent {

	public static final String EVENT_NAME = "Monitoring tool error";			
	
	public ToolErrorEvent(MonitorEventInfo info) {
		super(EVENT_NAME, info);
	}	
	
	@Override
	public void addPrintableExtraParameters(List<String> params) {
		// nothing to add
	}

	@Override
	protected void dumpExtraParameters(StringBuilder msg) {
		// nothing to add
	}

}
