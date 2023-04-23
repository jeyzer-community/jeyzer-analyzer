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

public class ConfigPrincipalHistogramSheet extends ConfigGraphSheet {
	
	public static final String DEFAULT_DESCRIPTION = "Lists all the functions and operations at the action level.\n"
			+ "Permits to spot concentration points within actions.";
	
	public static final String TYPE = "principal_histogram";
	
	public ConfigPrincipalHistogramSheet(Element configNode, int index){
		super(configNode, index);
	}

	@Override
	public String getDefaultDescription() {
		return DEFAULT_DESCRIPTION;
	}
	
}
