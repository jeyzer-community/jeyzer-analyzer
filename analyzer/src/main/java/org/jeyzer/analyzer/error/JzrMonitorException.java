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







public class JzrMonitorException extends JzrExecutionException {

	private static final long serialVersionUID = -3169224485237580473L;

	public JzrMonitorException() {
	}

	public JzrMonitorException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public JzrMonitorException(String arg0) {
		super(arg0);
	}

	public JzrMonitorException(Throwable arg0) {
		super(arg0);
	}

}
