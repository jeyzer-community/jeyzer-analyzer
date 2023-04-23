package org.jeyzer.monitor.impl.event.task.advanced;

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

import org.jeyzer.analyzer.data.ThreadAction;
import org.jeyzer.analyzer.data.stack.ThreadStack;
import org.jeyzer.analyzer.data.stack.ThreadStackJeyzerMXInfo;
import org.jeyzer.monitor.engine.event.MonitorTaskEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;


public class JeyzerMXContextParamPatternTaskEvent extends MonitorTaskEvent {

	private String paramName;
	private String user;
	
	protected String value;
	protected String contextId;
	
	public JeyzerMXContextParamPatternTaskEvent(String eventName, MonitorEventInfo info, ThreadAction action, String paramName, String user, String contextId) {
		super(eventName, info, action);
		info.updateMessage(buildMessage(info.getMessage(), user));
		this.paramName = paramName;
		this.user = user;
		this.contextId = contextId;
	}

	private static String buildMessage(String message, String user) {
		if (user != null && !user.isEmpty())
			return "User " + user + " / " + message;
		else
			return message;
	}
	
	@Override
	public void updateContext(ThreadStack stack) {
		if (this.value != null)
			return; // already set
		
		ThreadStackJeyzerMXInfo info = stack.getThreadStackJeyzerMXInfo();
		if (info == null)
			return;
		
		String paramValue = info.getContextParam(paramName);
		if (paramValue != null && !paramValue.isEmpty())
			this.value = paramValue; 
	}

	@Override
	public void addPrintableExtraParameters(List<String> params) {
		params.add(paramName);
		params.add(this.value);
		
		params.add("User");
		params.add(this.user != null && !this.user.isEmpty() ? this.user : "NA");

		if (this.contextId != null && !this.contextId.isEmpty()){
			params.add("Context id");
			params.add(this.contextId);
		}
	}

	@Override
	protected void dumpExtraParameters(StringBuilder msg) {
		msg.append("User : " + this.user != null && !this.user.isEmpty() ? this.user : "NA" + "\n");
		if (this.contextId != null && !this.contextId.isEmpty())
			msg.append("Context id : " + this.contextId + "\n");	
		msg.append(paramName + " :" + this.value + "\n");
	}

}

