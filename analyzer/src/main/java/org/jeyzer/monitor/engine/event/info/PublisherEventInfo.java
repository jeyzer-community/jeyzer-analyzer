package org.jeyzer.monitor.engine.event.info;

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




import java.util.Date;

public class PublisherEventInfo extends MonitorEventInfo {
	
	private final Date time;

	public PublisherEventInfo(String id, String ref, Scope scope, Level level, SubLevel subLevel, Date startSnapshot,
			Date endSnapshot, String message, Date start) {
		super(id, ref, scope, level, subLevel, startSnapshot, endSnapshot, message, null);
		this.time = start;
	}

	public Date getPublisherEventTime() {
		return time;
	}
}
