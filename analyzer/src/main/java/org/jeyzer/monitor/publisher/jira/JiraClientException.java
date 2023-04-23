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

import com.atlassian.jira.rest.client.ApiException;

public class JiraClientException extends Exception {

	private static final long serialVersionUID = 5423682050161883169L;

	public JiraClientException(JiraCreationRequest request, String message) {
		super("Failed to create the JIRA item for the event : " + request.getEvent().getName() + ". " + message);
	}
	
	public JiraClientException(JiraCreationRequest request, String message, ApiException ex) {
		super("Failed to create the JIRA item - due to JIRA API issue - for the event : " + request.getEvent().getName() + ". " + message, ex);
	}
}
