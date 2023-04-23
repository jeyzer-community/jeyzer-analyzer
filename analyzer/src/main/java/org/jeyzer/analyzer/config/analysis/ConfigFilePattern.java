package org.jeyzer.analyzer.config.analysis;

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

public class ConfigFilePattern {

	public static final String JZRA_RECORDING_FILE_TIMESTAMP_PATTERN = "file_timestamp_pattern";
	public static final String JZRA_RECORDING_FILE_REGEX_PATTERN = "file_regex_pattern";
	public static final String JZRA_RECORDING_FILE_JZR_PATTERN = "file_jzr_pattern";
	public static final String JZRA_RECORDING_PATTERN  = "pattern";
	public static final String JZRA_RECORDING_FILE_TIMESTAMP_PATTERN_IGNORE_SUFFIX  = "ignoreSuffix";
	
	private String type;
	private String pattern;
	private boolean ignoreSuffix;
	
	public ConfigFilePattern(String type, String pattern, boolean ignoreSuffix){
		this.type = type;
		this.pattern = pattern;
		this.ignoreSuffix = ignoreSuffix;
	}
	
	public String getType() {
		return type;
	}

	public String getPattern() {
		return pattern;
	}

	public boolean isSuffixIgnored() {
		return ignoreSuffix;
	}	
	
}
