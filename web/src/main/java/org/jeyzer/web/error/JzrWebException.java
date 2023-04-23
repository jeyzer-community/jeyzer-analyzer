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


/**
 * Jeyzer web exception
 * 
 * Any exception which must end up as user warning must inherit from this class 
 */

public class JzrWebException extends Exception {

	private static final long serialVersionUID = 2280802505046387611L;

	public JzrWebException() {
		super();
	}

	public JzrWebException(String arg0, Throwable arg1) {
		super(arg0, arg1); 
	}
	public JzrWebException(String arg0) {
		super(arg0);
	}

	public JzrWebException(Throwable arg0) {
		super(arg0);
	}

}
