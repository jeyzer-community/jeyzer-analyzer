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
 * Jeyzer mastere profile redirection failure
 */

public class JzrMasterProfileRedirectFailureException extends Exception {
	
	private static final long serialVersionUID = -4696420330040719860L;

	public JzrMasterProfileRedirectFailureException() {
		super();
	}

	public JzrMasterProfileRedirectFailureException(String arg0, Throwable arg1) {
		super(arg0, arg1); 
	}
	public JzrMasterProfileRedirectFailureException(String arg0) {
		super(arg0);
	}

	public JzrMasterProfileRedirectFailureException(Throwable arg0) {
		super(arg0);
	}

}
