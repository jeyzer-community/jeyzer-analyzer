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







public class JzrLineParsingException extends JzrParsingException{

	private static final long serialVersionUID = -6990633601252332611L;

	public JzrLineParsingException(Throwable arg1, String fileName, String line, int linePos) {
		super("Parsing of file " 
				+ fileName 
				+ " failed on line : "
				+ linePos 
				+ (line != null ? " - " + line : "")
				+ "\n   Reason : "				
				+ arg1.getMessage(),
				arg1);
	}
	
	public JzrLineParsingException(String reason, String fileName, String line, int linePos) {
		super("Parsing of file " 
				+ fileName 
				+ " failed on line "
				+ linePos 
				+ (line != null ? " - " + line : "")
				+ "\n      Reason : "
				+ reason);
	}	
	
}
