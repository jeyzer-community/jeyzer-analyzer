package org.jeyzer.analyzer.data.module;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProcessModule {

	private static final Pattern MODULE_VERSION_PATTERN = Pattern.compile("([\\d\\.]+)(-?[\\w\\.-^0-9]+)?$");
	
	private String name;
	private String version; // can be null
	private boolean open; 
	private boolean automatic; 
	private List<String> requires;
	private List<String> exports;
	private List<String> uses;
	private List<String> provides;
	private String classLoader; // can be null
	
	private boolean snapshot = false;
	
	public ProcessModule(String moduleEntry) {
		String[] elements = moduleEntry.split(";", 10); // include the empty values
		extractName(elements[0]);
		extractVersion(elements[1]);
		extractOpen(elements[2]);
		extractAutomatic(elements[3]);
		
		this.requires = extractList(elements[4]);
		this.exports = extractList(elements[5]);
		this.uses = extractList(elements[6]);
		this.provides = extractList(elements[7]);
		
		extractClassLoader(elements[8]);
	}

	public String getName() {
		return name;
	}
	
	public String getVersion() {
		return version;
	}
	
	public boolean hasNoVersion() {
		return version == null;
	}
	
	public boolean isSnapshot() {
		return snapshot;
	}

	public boolean isOpen() {
		return open;
	}

	public boolean isAutomatic() {
		return automatic;
	}

	public List<String> getRequires() {
		return requires;
	}

	public List<String> getExports() {
		return exports;
	}

	public List<String> getUses() {
		return uses;
	}

	public List<String> getProvides() {
		return provides;
	}

	public String getClassLoader() {
		return classLoader;
	}
	
	private void extractName(String value) {
		if (value != null && !value.isEmpty())
			this.name = value;
	}
	
	private void extractVersion(String value) {
		if (value == null || value.isEmpty())
			return;
		
		Matcher matcher = MODULE_VERSION_PATTERN.matcher(value);
		if (matcher.matches()) {
			if (matcher.group(2) == null) {
				// just the version
				this.version = matcher.group(1);
			}
			else {
				// version + snapshot
				this.snapshot = true;
				this.version = matcher.group(1) + matcher.group(2);
			}
		}
	}
	
	private void extractOpen(String value) {
		this.open = Boolean.valueOf(value);
	}
	
	private void extractAutomatic(String value) {
		this.automatic = Boolean.valueOf(value);
	}

	private List<String> extractList(String values) {
		if (values.isEmpty())
			return new ArrayList<>();
		
		String[] elements = values.split(",");
		return Arrays.asList(elements);
	}
	
	private void extractClassLoader(String value) {
		if (value != null && !value.isEmpty())
			this.classLoader = value;
	}
}
