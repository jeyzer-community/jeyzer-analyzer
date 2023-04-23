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

public class ConfigAboutSheet extends ConfigSheet {

	public static final String DEFAULT_DESCRIPTION = "All about Jeyzer.";
	public static final String SHEET_NAME = "About";
	
	public ConfigAboutSheet(Element configNode, int index) {
		super(SHEET_NAME, DEFAULT_DESCRIPTION, index);
	}

	@Override
	public String getDefaultDescription() {
		return DEFAULT_DESCRIPTION;
	}
	
}
