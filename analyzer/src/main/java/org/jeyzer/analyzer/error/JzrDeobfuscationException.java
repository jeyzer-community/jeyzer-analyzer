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







public class JzrDeobfuscationException  extends JzrTranslatorException{

	private static final long serialVersionUID = -225831066503841243L;

	public JzrDeobfuscationException() {
		super();
	}

	public JzrDeobfuscationException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public JzrDeobfuscationException(String arg0) {
		super(arg0);
	}

	public JzrDeobfuscationException(Throwable arg0) {
		super(arg0);
	}

}
