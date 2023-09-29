package org.jeyzer.monitor.engine.event.info;

/*-
 * ---------------------------LICENSE_START---------------------------
 * Jeyzer Analyzer
 * --
 * Copyright (C) 2020 - 2023 Jeyzer
 * --
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 * ----------------------------LICENSE_END----------------------------
 */

import java.util.Date;
import java.util.Map;

public class MonitorEventWithContextInfo extends MonitorEventInfo{

	private Map<String, Object> context;
	
	public MonitorEventWithContextInfo(String id, String ref, Scope scope, Level level, SubLevel subLevel, Date start,
			Date end, String message, String ticket, Map<String, Object> context) {
		super(id, ref, scope, level, subLevel, start, end, message, ticket);
		this.context = context;
	}
	
	public Map<String, Object> getContext(){
		return this.context;
	}

}
