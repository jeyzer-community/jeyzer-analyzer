package org.jeyzer.monitor.config.engine;

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




import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.monitor.engine.event.info.Level;

public class ConfigMonitorAnalyzerThreshold extends ConfigMonitorThreshold{

	private static final String THRESHOLD_NAME = "analyzer exception";
	
	public ConfigMonitorAnalyzerThreshold(String message, String ref) throws JzrInitializationException {
		super(THRESHOLD_NAME, ref, Level.ERROR, message);
	}
	
	public ConfigMonitorAnalyzerThreshold(String message, Level level, String ref) throws JzrInitializationException {
		super(THRESHOLD_NAME, ref, level, message);
	}
}
