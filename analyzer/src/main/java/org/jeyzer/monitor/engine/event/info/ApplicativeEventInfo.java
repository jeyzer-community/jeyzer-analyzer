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

public class ApplicativeEventInfo extends MonitorEventInfo {
	
	private final String appId;
	private final Date appStart;
	private final String threadId;
	private final boolean oneshot;
	private final Date appEnd;

	public ApplicativeEventInfo(String id, String ref, Scope scope, Level level, SubLevel subLevel, Date startSnapshot,
			Date endSnapshot, String message, String ticket, String appId, Date start, Date end, boolean oneshot) {
		super(id, ref, scope, level, subLevel, startSnapshot, endSnapshot, message, ticket);
		this.appId = appId;
		this.appStart = start;
		this.appEnd = end;
		this.threadId = null;
		this.oneshot = oneshot;
	}
	
	public ApplicativeEventInfo(String id, String ref, Scope scope, Level level, SubLevel subLevel, Date startSnapshot,
			Date endSnapshot, String message, String ticket, String appId, Date start, Date end, String threadId, boolean oneshot) {
		super(id, ref, scope, level, subLevel, startSnapshot, endSnapshot, message, ticket);
		this.appId = appId;
		this.appStart = start;
		this.appEnd = end;
		this.threadId = threadId;
		this.oneshot = oneshot;
	}

	public String getAppEventId() {
		return appId;
	}

	public Date getAppEventStart() {
		return appStart;
	}

	public String getAppThreadId() {
		return threadId;
	}

	public boolean isAppEventOneshot() {
		return oneshot;
	}

	public Date getAppEventEnd() {
		return appEnd; // can be null
	}
}
