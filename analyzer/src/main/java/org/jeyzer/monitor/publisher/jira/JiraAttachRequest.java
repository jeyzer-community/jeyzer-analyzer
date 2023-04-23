package org.jeyzer.monitor.publisher.jira;

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

import java.io.File;

public class JiraAttachRequest {

	private File file;
	private String ticketId;
	
	public JiraAttachRequest(String ticketId, String path) {
		this.file = new File(path);
		this.ticketId = ticketId;
	}
	
	public File getAttachment() {
		return this.file;
	}
	
	public String getTicketId() {
		return this.ticketId;
	}
}
