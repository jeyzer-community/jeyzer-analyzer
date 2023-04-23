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
import java.util.regex.Pattern;

import org.jeyzer.analyzer.data.ProcessCommandLine;
import org.jeyzer.analyzer.data.ProcessCommandLine.CommandLineProperty;
import org.jeyzer.analyzer.session.JzrSession;
import org.jeyzer.monitor.engine.event.MonitorSystemEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;

public class ProcessCommandLinePropertyPatternEvent extends MonitorSystemEvent {

	private Pattern paramNamePattern;
	private String value;
	
	public ProcessCommandLinePropertyPatternEvent(String eventName, MonitorEventInfo info, Pattern paramNamePattern) {
		super(eventName, info);
		this.paramNamePattern = paramNamePattern;
	}
	
	@Override
	public void updateContext(JzrSession session) {
		if (this.value != null)
			return; // already set, should not happen
		
		ProcessCommandLine commandLine = session.getProcessCommandLine();
		if  (commandLine == null)
			return; // should not happen
		
		CommandLineProperty property = commandLine.getValue(paramNamePattern);
		if (property == null)
			return; // should not happen
		
		value = property.getValue();
	}

	@Override
	protected void dumpExtraParameters(StringBuilder msg) {
		msg.append("Pattern " + paramNamePattern.pattern() + " matches value : " + this.value + "\n");
	}
	
	@Override
	public void addPrintableExtraParameters(List<String> params) {
		params.add(paramNamePattern.pattern());
		params.add(value);
	}

}
