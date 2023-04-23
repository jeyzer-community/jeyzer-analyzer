package org.jeyzer.analyzer.parser;

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

import java.util.Date;
import java.util.List;
import java.util.Map;

public class StackContext {
	
	public StackContext(List<String> lines, int pos, String name, List<String> locks, List<String> biased, Date date) {
		this.threadLines = lines;
		this.filePos = pos;
		this.fileName = name;
		this.ownedLocks = locks;
		this.biasedLocks = biased;
		this.timestamp = date;
	}

	public StackContext(List<String> lines, int pos, String name, List<String> locks, List<String> biasedLocks, Date date, String lockedOn, String lockedOnClassName) {
		this(lines, pos, name, locks, biasedLocks, date);
		this.lockedOn = lockedOn;
		this.lockedOnClassName = lockedOnClassName;
	}

	public StackContext(List<String> lines, int pos, String name, List<String> locks, List<String> biasedLocks, Date date, String lockedOn, String lockedOnClassName, Map<String, Object> any) {
		this(lines, pos, name, locks, biasedLocks, date, lockedOn, lockedOnClassName);
		this.any = any; 
	}

	public List<String> threadLines; 
	public int filePos;
	public String fileName;
	public List<String> ownedLocks;
	public List<String> biasedLocks;
	public Date timestamp; 
	public String lockedOn; 
	public String lockedOnClassName;
	public Map<String, Object> any; // can be null, required for JFR
}
