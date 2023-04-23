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

public class ConfigJVMFlagsSheet extends ConfigSheet {

	public static final String TYPE = "jvm_flags";
	
	private static final String JZRR_DISPLAY = "display";
	private static final String JZRR_ORIGIN = "origin";
	private static final String JZRR_AUTO_FILTER_DEFAULT_FLAGS = "auto_filter_default_flags";
	
	public static final String DEFAULT_DESCRIPTION = "Displays all the JVM flags including the changed ones\n"
			+ "Permits to review the JVM technical details.";
	
	private ConfigHighlights originHighlightsCfg;
	private boolean autoFilterDefaultFlags;
	
	public ConfigJVMFlagsSheet(Element configNode, int index) {
		super(configNode, index);
		Element displayNode = ConfigUtil.getFirstChildNode(configNode, JZRR_DISPLAY);
		Element originNode = ConfigUtil.getFirstChildNode(displayNode, JZRR_ORIGIN);
		if (ConfigHighlights.hasHighlights(originNode))
			originHighlightsCfg = new ConfigHighlights(originNode);
		
		// Default to false if not found
		this.autoFilterDefaultFlags = Boolean.parseBoolean(ConfigUtil.getAttributeValue(originNode, JZRR_AUTO_FILTER_DEFAULT_FLAGS));
	}

	@Override
	public String getDefaultDescription() {
		return DEFAULT_DESCRIPTION;
	}

	public ConfigHighlights getOriginHighlights() {
		return originHighlightsCfg;
	}
	
	public boolean areDefaultFlagsAutoFiltered() {
		return autoFilterDefaultFlags;
	}	
}
