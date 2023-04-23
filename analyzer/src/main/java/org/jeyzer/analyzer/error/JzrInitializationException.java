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







public class JzrInitializationException extends JzrException {

	private static final long serialVersionUID = -7995518258632536990L;

	public JzrInitializationException() {
	}

	public JzrInitializationException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public JzrInitializationException(String arg0) {
		super(arg0);
	}

	public JzrInitializationException(Throwable arg0) {
		super(arg0);
	}

}
