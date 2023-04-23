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




import org.jeyzer.analyzer.config.ConfigUtil;
import org.w3c.dom.Element;

public class ConfigActionProfilingSheet extends ConfigProfilingSheet {

	public static final String DEFAULT_DESCRIPTION = "Displays stack traces in a tree node style - grouped per action - with apperance/CPU/memory figures.\n"
												+ "Permits to detect bottlenecks in the applicative source code.";
	
	public static final String TYPE = "action_profiling";
	
	private static final String JZRR_INCLUDE_ATBI = "include_atbi";
	
	private boolean includeAtbi;
	
	public ConfigActionProfilingSheet(Element configNode, int index) {
		super(configNode, index);
		this.includeAtbi = Boolean.parseBoolean(ConfigUtil.getAttributeValue(configNode,JZRR_INCLUDE_ATBI)); 
	}

	public boolean isAtbiIncluded() {
		return includeAtbi;
	}

	@Override
	public String getDefaultDescription() {
		return DEFAULT_DESCRIPTION;
	}	
	
}
