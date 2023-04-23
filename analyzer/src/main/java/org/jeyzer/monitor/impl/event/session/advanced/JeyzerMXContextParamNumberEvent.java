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
import org.jeyzer.monitor.util.Operator;

import com.google.common.primitives.Longs;

public class JeyzerMXContextParamNumberEvent extends MonitorSessionEvent {
	
	private String paramName;
	private Operator operator;
	private long maxValue;
	
	public JeyzerMXContextParamNumberEvent(String eventName, MonitorEventInfo info, String paramName, Operator operator) {
		super(eventName, info);
		this.paramName = paramName;
		this.operator = operator;
		this.maxValue = Operator.LOWER_OR_EQUAL.equals(operator) ? Integer.MAX_VALUE : Integer.MIN_VALUE; 
	}

	@Override
	public void updateContext(ThreadDump dump) {
		String longValue = dump.getJeyzerMXContextParams().get(paramName);
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
	public void addPrintableExtraParameters(List<String> params) {
		params.add(operator.getBoundPrefix() + paramName);
		params.add(Long.toString(this.maxValue));
	}

	@Override
	protected void dumpExtraParameters(StringBuilder msg) {
		msg.append(operator.getBoundPrefix() + paramName + " : " + this.maxValue + "\n");
	}
}
