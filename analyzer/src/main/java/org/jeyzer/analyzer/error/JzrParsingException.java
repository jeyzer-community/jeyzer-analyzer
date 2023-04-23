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







public class JzrParsingException  extends JzrExecutionException{

	private static final long serialVersionUID = -828536286204267036L;

	public JzrParsingException() {
		super();
	}
	
	public JzrParsingException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public JzrParsingException(String arg0) {
		super(arg0);
	}

	public JzrParsingException(Throwable arg0) {
		super(arg0);
	}
}
