package org.jeyzer.analyzer.error;

/*-
 * ---------------------------LICENSE_START---------------------------
 * Jeyzer Analyzer
 * --
 * Copyright (C) 2023 Jeyzer
 * --
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 * ----------------------------LICENSE_END----------------------------
 */

public class JzrTranslatorZipInvalidFileException  extends JzrTranslatorException{

	private static final long serialVersionUID = -8194712299219590605L;

	public JzrTranslatorZipInvalidFileException() {
		super();
	}
	
	/**
	 * Translation limit violation exception, due to human error
	 * Error message is made public (for example through the web UI)
	 * 
	 * @param public message
	 */
	public JzrTranslatorZipInvalidFileException(String arg0) {
		super(arg0);
	}
}
