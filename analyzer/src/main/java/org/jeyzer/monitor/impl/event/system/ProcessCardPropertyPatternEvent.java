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

import org.jeyzer.analyzer.data.ProcessCard;
import org.jeyzer.analyzer.data.ProcessCard.ProcessCardProperty;
import org.jeyzer.analyzer.session.JzrSession;
import org.jeyzer.monitor.engine.event.MonitorSystemEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;

public class ProcessCardPropertyPatternEvent extends MonitorSystemEvent {

	private Pattern paramNamePattern;
	private String value;
	
	public ProcessCardPropertyPatternEvent(String eventName, MonitorEventInfo info, Pattern paramNamePattern) {
		super(eventName, info);
		this.paramNamePattern = paramNamePattern;
	}
	
	@Override
	public void updateContext(JzrSession session) {
		if (this.value != null)
			return; // already set, should not happen
		
		ProcessCard card = session.getProcessCard();
		if  (card == null)
			return; // should not happen
		
		ProcessCardProperty property = card.getValue(paramNamePattern);
		if (property == null)
			return; // should not happen
		
		value = property.getValue();
	}

	@Override
	protected void dumpExtraParameters(StringBuilder msg) {
		msg.append(paramNamePattern.pattern() + " :" + this.value + "\n");
	}
	
	@Override
	public void addPrintableExtraParameters(List<String> params) {
		params.add(paramNamePattern.pattern());
		params.add(value);
	}

}
