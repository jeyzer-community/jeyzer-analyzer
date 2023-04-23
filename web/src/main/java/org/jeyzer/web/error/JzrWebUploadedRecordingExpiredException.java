package org.jeyzer.web.error;

/*-
 * ---------------------------LICENSE_START---------------------------
 * Jeyzer Web
 * --
 * Copyright (C) 2020 Jeyzer
 * --
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 * ----------------------------LICENSE_END----------------------------
 */


public class JzrWebUploadedRecordingExpiredException extends JzrWebException {

	private static final long serialVersionUID = 7420242048810286557L;

	public JzrWebUploadedRecordingExpiredException(String message) {
		super(message);
	}
	
}
