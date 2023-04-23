package org.jeyzer.analyzer.config.report;

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







import org.w3c.dom.Element;

public class ConfigExecutorHistogramSheet extends ConfigSheet {
	
	public static final String DEFAULT_DESCRIPTION = "Lists all the executors with their number of actions and stacks.\n"
			+ "Permits to get a view on the activity type like user actions, scheduled activities, internals, etc.";
	
	public static final String TYPE = "executor_histogram";
	
	public ConfigExecutorHistogramSheet(Element configNode, int index){
		super(configNode, index);
	}

	@Override
	public String getDefaultDescription() {
		return DEFAULT_DESCRIPTION;
	}
	
}
