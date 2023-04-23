package org.jeyzer.analyzer.config.patterns;

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






public interface ConfigPatterns {

	public static final String JZRA_FUNCTION = "function";
	public static final String JZRA_DISCOVERY_FUNCTION = "discovery_function";
	public static final String JZRA_OPERATION = "operation";
	public static final String JZRA_DISCOVERY_OPERATION = "discovery_operation";
	public static final String JZRA_LOCKER = "locker";
	public static final String JZRA_EXCLUDE = "exclude";
	public static final String JZRA_EXCLUDE_THREAD_NAME = "exclude_thread_name";
	public static final String JZRA_EXECUTOR = "executor";
	public static final String JZRA_EXECUTOR_THREAD_NAME = "executor_thread_name";
	
	public Object getValue(String field);
	
}
