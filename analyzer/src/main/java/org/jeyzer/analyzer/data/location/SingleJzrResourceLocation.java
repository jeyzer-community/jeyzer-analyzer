package org.jeyzer.analyzer.data.location;

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




public class SingleJzrResourceLocation implements JzrResourceLocation {

	private String location;
	private String version; // can be null, Jeyzer repository specific
	
	public SingleJzrResourceLocation(String location){
		this.location = location;
	}
	
	public SingleJzrResourceLocation(String location, String version){
		this.location = location;
		this.version = version;
	}

	public String getPath() {
		return location;
	}

	public String getVersion() {
		return version; // can be null
	}
	
	public boolean hasVersion() {
		return version != null;
	}
}
