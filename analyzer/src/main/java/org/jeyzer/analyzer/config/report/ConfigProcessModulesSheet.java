package org.jeyzer.analyzer.config.report;

import org.jeyzer.analyzer.config.ConfigUtil;

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

public class ConfigProcessModulesSheet extends ConfigSheet {

	public static final String DEFAULT_DESCRIPTION = "Displays the list of Java modules loaded by the JVM.\n"
			+ "Includes their version and attributes.\n"
			+ "Detect the snapshot libraries.";
	
	public static final String TYPE = "process_modules";

	private ConfigGraph moduleGraphCfg; // optional	
	
	public ConfigProcessModulesSheet(Element configNode, int index) {
		super(configNode, index);
		
		Element moduleGraphNode = ConfigUtil.getFirstChildNode(configNode, ConfigGraph.JZRA_MODULE_GRAPH);
		
		if (moduleGraphNode != null){
			Element graphNode = ConfigUtil.getFirstChildNode(moduleGraphNode, ConfigGraph.JZRA_GRAPH);
			if (graphNode != null)
				moduleGraphCfg = new ConfigGraph(graphNode);
		}
	}

	@Override
	public String getDefaultDescription() {
		return DEFAULT_DESCRIPTION;
	}

	public ConfigGraph getModuleGraphCfg() {
		return moduleGraphCfg;
	}
	
	public boolean isModuleGraphDisplayed() {
		return moduleGraphCfg != null;
	}
}
