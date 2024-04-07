package org.jeyzer.analyzer.error;

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

public class JzrTranslatorMultipleJFRFilesException  extends JzrTranslatorException{

	private static final long serialVersionUID = -76770812734625535L;

	public JzrTranslatorMultipleJFRFilesException() {
		super();
	}
	
	/**
	 * Translation limit violation exception, due to human error
	 * Error message is made public (for example through the web UI)
	 * 
	 * @param public message
	 */
	public JzrTranslatorMultipleJFRFilesException(String arg0) {
		super(arg0);
	}
}
