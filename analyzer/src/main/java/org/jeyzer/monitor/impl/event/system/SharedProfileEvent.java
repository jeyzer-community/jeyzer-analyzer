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

import org.jeyzer.analyzer.session.JzrSession;
import org.jeyzer.monitor.engine.event.MonitorSystemEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;

public class SharedProfileEvent extends MonitorSystemEvent {

	private String profileName;
	
	public SharedProfileEvent(String eventName, MonitorEventInfo info, String profileName) {
		super(eventName, info);
		this.profileName = profileName;
	}
	
	@Override
	public void updateContext(JzrSession session) {
		// nothing to do
	}

	@Override
	protected void dumpExtraParameters(StringBuilder msg) {
		msg.append("Shared profile :" + this.profileName + "\n");
	}
	
	@Override
	public void addPrintableExtraParameters(List<String> params) {
	}

}
