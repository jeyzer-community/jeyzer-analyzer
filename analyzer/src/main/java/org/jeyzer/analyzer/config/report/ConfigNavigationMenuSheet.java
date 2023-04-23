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







import java.util.List;

import org.w3c.dom.Element;

public class ConfigNavigationMenuSheet  extends ConfigSheet {

	public static final String DEFAULT_NAME = "Menu";
	public static final String DEFAULT_DESCRIPTION = "Displays all available sheets and direct links to it.";	
	
	public static final String TYPE = "navigation";	
	
	private List<ConfigSheet> orderedSheets;
	
	public ConfigNavigationMenuSheet(Element configNode, int index, List<ConfigSheet> orderedSheets) {
		super(configNode, index);
		this.orderedSheets = orderedSheets;
	}
	
	
	public ConfigNavigationMenuSheet(List<ConfigSheet> orderedSheets) {
		super(DEFAULT_NAME, DEFAULT_DESCRIPTION, 0);
		this.orderedSheets = orderedSheets;
	}
	
	public List<ConfigSheet> getConfigNavigationMenuSheet(){
		return this.orderedSheets;
	}

	@Override
	public String getDefaultDescription() {
		return DEFAULT_DESCRIPTION; 
	}

}
