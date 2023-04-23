package org.jeyzer.analyzer.data.flags;

/*-
 * ---------------------------LICENSE_START---------------------------
 * Jeyzer Analyzer
 * --
 * Copyright (C) 2020 - 2021 Jeyzer
 * --
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 * ----------------------------LICENSE_END----------------------------
 */

public class JVMFlag {

	public static final String SEPARATOR = "\t";
	public static final String FLAG_TYPE_NOT_AVAILABLE = "-1";
	
	private String name;
	private String type;
	private String value;
	private String origin;
	private long time;
	private String oldValue;
	
	public JVMFlag(String entry) {
		String[] elements = entry.split(SEPARATOR);
		this.name = elements[0];
		this.type = elements[1];
		this.value = elements[2];
		this.oldValue = elements[3];
		this.origin = elements[4];
		this.time = Long.valueOf(elements[5]);
	}
	
	public String getName() {
		return name;
	}
	
	public String getType() {
		return type; // can be -1 (JZR Recording)
	}
	
	public String getValue() {
		return value;
	}
	
	public String getOldValue() {
		return oldValue;
	}
	 
	public String getOrigin() {
		return origin;
	}
	
	public long getTime() {
		return time;
	}
	
	public boolean isChangedValue() {
		return this.time != -1;
	}
}
