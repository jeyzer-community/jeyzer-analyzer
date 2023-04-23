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

public class JeyzerMXContextParamPatternEvent extends MonitorSessionEvent {
	
	private String paramName;
	private String value;
	
	public JeyzerMXContextParamPatternEvent(String eventName, MonitorEventInfo info, ThreadDump dump, String paramName) {
		super(eventName, info);
		this.paramName = paramName;
	}

	@Override
	public void updateContext(ThreadDump dump) {
		if (this.value != null)
			return; // already set
		
		String paramValue = dump.getJeyzerMXContextParams().get(paramName);
		
		if (paramValue != null && !paramValue.isEmpty())
			this.value = paramValue; 
	}

	@Override
	public void addPrintableExtraParameters(List<String> params) {
		params.add(paramName);
		params.add(value);
	}

	@Override
	protected void dumpExtraParameters(StringBuilder msg) {
		msg.append(paramName + " :" + this.value + "\n");
	}

}
