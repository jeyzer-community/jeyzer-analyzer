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

public class ConfigContentionTypes {

	private ConfigHighlights highlightsCfg;
	private ConfigGraph graphCfg;
	
	public ConfigContentionTypes(Element node){
		Element graphNode = ConfigUtil.getFirstChildNode(node, ConfigGraph.JZRA_GRAPH);
		if (graphNode != null)
			graphCfg = new ConfigGraph(graphNode);
		
		highlightsCfg = new ConfigHighlights(node);
	}
	
	public ConfigGraph getConfigGraph(){
		return graphCfg;
	}
	
	public ConfigHighlights getHighlightsConfig(){
		return highlightsCfg;
	}
	
}
