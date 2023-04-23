package org.jeyzer.analyzer.data.tag;

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



public class ModuleTag extends Tag {
	
	public static final String DISPLAY_NAME = "Module";
	public static final String DISPLAY_JAVA_NAME = "ModuleJava";
	
	public static final String JAVA_MODULE_PREFIX = "java.";
	public static final String JDK_MODULE_PREFIX = "jdk.";
	
	public ModuleTag(String name) {
		super(name);
	}

	@Override
	public String getTypeName() {
		return this.getName().startsWith(JAVA_MODULE_PREFIX) || this.getName().startsWith(JDK_MODULE_PREFIX) ? DISPLAY_JAVA_NAME : DISPLAY_NAME;
	}
}
