package org.jeyzer.analyzer.data;

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




import java.util.HashMap;
import java.util.Map;

public class DiskSpaces {

	private Map<String, DiskSpaceInfo> diskSpaces = new HashMap<>();
	
	public DiskSpaceInfo getDiskSpace(String name){
		return this.diskSpaces.get(name);
	}
	
	public void addDiskSpaceInfo(String name, DiskSpaceInfo info){
		this.diskSpaces.put(name, info);
	}
}
