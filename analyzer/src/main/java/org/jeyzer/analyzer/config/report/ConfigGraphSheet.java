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

public abstract class ConfigGraphSheet extends ConfigSheet {
	
	private ConfigGraph functionGraphCfg; // optional
	private ConfigContentionTypes contentionGraphCfg; // optional
	
	public ConfigGraphSheet(Element configNode, int index) {
		super(configNode, index);
		
		Element functionGraphNode = ConfigUtil.getFirstChildNode(configNode, ConfigGraph.JZRA_FUNCTION_GRAPH);
		
		if (functionGraphNode != null){
			Element graphNode = ConfigUtil.getFirstChildNode(functionGraphNode, ConfigGraph.JZRA_GRAPH);
			if (graphNode != null)
				functionGraphCfg = new ConfigGraph(graphNode);
		}
		
		Element contentionGraphNode = ConfigUtil.getFirstChildNode(configNode, ConfigGraph.JZRA_CONTENTION_GRAPH);
		if (contentionGraphNode != null)
			contentionGraphCfg = new ConfigContentionTypes(contentionGraphNode);
	}

	public ConfigGraph getFunctionGraphCfg() {
		return functionGraphCfg;
	}
	
	public boolean isFunctionGraphDisplayed() {
		return functionGraphCfg != null;
	}
	
	public ConfigContentionTypes getContentionGraphCfg() {
		return contentionGraphCfg;
	}
	
	public boolean isContentionGraphDisplayed() {
		return contentionGraphCfg != null;
	}

}
