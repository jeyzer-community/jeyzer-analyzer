package org.jeyzer.analyzer.error;

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

public class JzrTranslatorZipPasswordProtectedException extends JzrTranslatorException {

	/**
	 * Translation recording zip file is password encrypted (and we do not have the password)
	 * Error message is made public (for example through the web UI)
	 * 
	 * @param public message
	 */
	private static final long serialVersionUID = 1964783867035220534L;

	public JzrTranslatorZipPasswordProtectedException(String message) {
		super(message);
	}	
	
}
