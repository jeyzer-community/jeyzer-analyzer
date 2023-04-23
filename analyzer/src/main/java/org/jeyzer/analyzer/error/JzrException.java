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








/**
 * Thread dump analysis exception
 * 
 * Any exception resulting from an invalid thread dump or user data input 
 * must inherit from this class.
 */

public class JzrException extends Exception {

	private static final long serialVersionUID = 7574061232674280048L;

	public JzrException() {
		super();
	}

	public JzrException(String arg0, Throwable arg1) {
		super(arg0, arg1); 
	}
	public JzrException(String arg0) {
		super(arg0);
	}

	public JzrException(Throwable arg0) {
		super(arg0);
	}

}
