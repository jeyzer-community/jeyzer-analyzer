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




public class JzrReportException extends JzrExecutionException {

	private static final long serialVersionUID = 2159153457752928284L;

	public JzrReportException() {
	}

	public JzrReportException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public JzrReportException(String arg0) {
		super(arg0);
	}

	public JzrReportException(Throwable arg0) {
		super(arg0);
	}

}
