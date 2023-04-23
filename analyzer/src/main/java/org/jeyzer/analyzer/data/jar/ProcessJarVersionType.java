package org.jeyzer.analyzer.data.jar;

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

public enum ProcessJarVersionType {
	
	JAR_FILE_VERSION("Jar File Version", 100),
	IMPLEMENTATION_VERSION("Implementation-Version", 10),
	SPECIFICATION_VERSION("Specification-Version", 5),
	BUNDLE_VERSION("Bundle-Version", 1);
	
	private int priority;
	private String name;
	
	private ProcessJarVersionType(String name, int priority){
		this.name = name;
		this.priority = priority;
	}
	
	public String getAttributeName() {
		return this.name;
	}
	
	public int getPriority() {
		return this.priority;
	}
	
	public static ProcessJarVersionType loadProcessJarManifestVersion(String name) {
		if (IMPLEMENTATION_VERSION.getAttributeName().equals(name))
			return IMPLEMENTATION_VERSION;
		else if (SPECIFICATION_VERSION.getAttributeName().equals(name))
			return SPECIFICATION_VERSION;
		else if (BUNDLE_VERSION.getAttributeName().equals(name))
			return BUNDLE_VERSION;
		else 
			return null;
	}
}
