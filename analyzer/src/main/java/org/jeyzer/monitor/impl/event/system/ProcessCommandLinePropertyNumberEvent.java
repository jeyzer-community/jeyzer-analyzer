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
import org.jeyzer.monitor.util.Operator;

import com.google.common.primitives.Longs;

public class ProcessCommandLinePropertyNumberEvent extends MonitorSystemEvent {

	private Pattern paramNamePattern;
	private long maxValue;
	private Operator operator;
	
	public ProcessCommandLinePropertyNumberEvent(String eventName, MonitorEventInfo info, Pattern paramNamePattern, Operator operator) {
		super(eventName, info);
		this.paramNamePattern = paramNamePattern;
		this.operator = operator;
		this.maxValue = Operator.LOWER_OR_EQUAL.equals(operator) ? Integer.MAX_VALUE : Integer.MIN_VALUE;
	}
	
	@Override
	public void updateContext(JzrSession session) {
		ProcessCommandLine commandLine = session.getProcessCommandLine();
		if  (commandLine == null)
			return; // should not happen
		
		CommandLineProperty property = commandLine.getValue(paramNamePattern);
		if (property == null)
			return; // should not happen
		
		String longValue = property.getValue();
		if (longValue == null || longValue.isEmpty())
			return;
		
		Long paramLongValue = Longs.tryParse(longValue);
		if (paramLongValue == null)
			return;
		
		if (Operator.LOWER_OR_EQUAL.equals(operator) && paramLongValue <= maxValue)
			maxValue = paramLongValue;
		else if (!Operator.LOWER_OR_EQUAL.equals(operator) && paramLongValue >= maxValue)
			maxValue = paramLongValue;
	}

	@Override
	protected void dumpExtraParameters(StringBuilder msg) {
		msg.append(paramNamePattern.pattern() + " : " + this.maxValue + "\n");
	}
	
	@Override
	public void addPrintableExtraParameters(List<String> params) {
		params.add(paramNamePattern.pattern());
		params.add(Long.toString(this.maxValue));
	}

	
}
