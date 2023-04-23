package org.jeyzer.analyzer.error;

import java.util.Set;

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

public class JzrNoThreadDumpFileFound  extends JzrExecutionException {
	
	private static final long serialVersionUID = -4215509061665240452L;

	private Set<String> supportedFileFormats = null;
	
	public JzrNoThreadDumpFileFound(String message){
		super(message);
	}
	
	public JzrNoThreadDumpFileFound(String message, Set<String> supportedFileFormats){
		super(message);
		this.supportedFileFormats = supportedFileFormats;
	}
	
	public Set<String> getSupportedFileFormats() {
		return supportedFileFormats; // can be null
	}
}
