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

public class ConfigSessionDetailsSheet extends ConfigSheet {

	public static final String DEFAULT_DESCRIPTION = "Lists analysis and session recording information.";
	
	public static final String TYPE = "session_details";
	
	private static final String JZRR_EXPOSE_PATHS = "expose_paths";
	
	private boolean exposePaths; // false if not present
	
	public ConfigSessionDetailsSheet(Element configNode, int index) {
		super(configNode, index);
		this.exposePaths = Boolean.parseBoolean(ConfigUtil.getAttributeValue(configNode, JZRR_EXPOSE_PATHS));
	}

	@Override
	public String getDefaultDescription() {
		return DEFAULT_DESCRIPTION;
	}
	
	public boolean arePathsExposed() {
		return this.exposePaths;
	}
}
