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
import java.util.Set;
import java.util.regex.Pattern;

import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.monitor.engine.event.MonitorSessionEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;

public class MXBeanParamPatternEvent extends MonitorSessionEvent {
	
	private Pattern paramNamePattern;
	
	protected String value;
	
	public MXBeanParamPatternEvent(String eventName, MonitorEventInfo info, ThreadDump dump, Pattern paramNamePattern) {
		super(eventName, info);
		this.paramNamePattern = paramNamePattern;
	}

	@Override
	public void updateContext(ThreadDump dump) {
		if (this.value != null)
			return; // already set
		
		// return the first that matches
		String paramKey = getParamKey(dump.getJMXBeanParams().keySet());
		
		String paramValue = dump.getJMXBeanParams().get(paramKey);
		
		if (paramValue != null && !paramValue.isEmpty())
			this.value = paramValue; 
	}

	@Override
	public void addPrintableExtraParameters(List<String> params) {
		params.add(paramNamePattern.pattern());
		params.add(value);
	}

	@Override
	protected void dumpExtraParameters(StringBuilder msg) {
		msg.append(paramNamePattern.pattern() + " :" + this.value + "\n");
	}

	private String getParamKey(Set<String> paramKeys) {
		for (String key : paramKeys){
			if (this.paramNamePattern.matcher(key).find())
				return key;
		}
		return null;
	}
	
}
