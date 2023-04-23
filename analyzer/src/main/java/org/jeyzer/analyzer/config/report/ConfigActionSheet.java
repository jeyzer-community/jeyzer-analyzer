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

public class ConfigActionSheet extends ConfigSheet {
	
	public static final String DEFAULT_DESCRIPTION = "Displays all the detected actions in a flat mode.\n"
			+ "Permits to narrow actions occuring on a specific time range.";
	
	public static final String TYPE = "action_list";
	
	public ConfigActionSheet(Element configNode, int index){
		super(configNode, index);
	}

	@Override
	public String getDefaultDescription() {
		return DEFAULT_DESCRIPTION;
	}
	
}
