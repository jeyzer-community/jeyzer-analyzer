package org.jeyzer.web.util;

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


public enum SecMode {
	
	/*
	 * Duplicate of JzrProfileSecurity
	 * Extracted from Analyzer project to not expose the whole security layer after obfuscation.
	 */
	
	NONE("none"),
	EXTERNAL_OPTIONAL("external optional"),
	EXTERNAL_MANDATORY("external mandatory"),
	INTERNAL_MANDATORY("internal mandatory"),
	;
	
	private String label;
	
	private SecMode(String label){
		this.label = label;
	}
	
	public String getLabel(){
		return this.label;
	}
}
