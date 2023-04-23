package org.jeyzer.analyzer.config.graph;

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
import org.jeyzer.analyzer.config.report.ConfigHighlights;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.w3c.dom.Element;

public class ConfigContentionGraphPlayer extends ConfigGraphPlayer {
	
	private ConfigHighlights highlightsCfg;
	
	public ConfigContentionGraphPlayer(Element playerRootNode, String outputDirectory) throws JzrInitializationException {
		super(playerRootNode, JZRA_CONTENTION_GRAPH_PLAYER, outputDirectory);
	}
	
	public ConfigHighlights getHighlightsConfig(){
		return highlightsCfg;
	}

	@Override
	protected void init(Element playerNode) throws JzrInitializationException {
		rendering = new ConfigGraphRendering(
				ConfigUtil.getFirstChildNode(playerNode, ConfigGraphRendering.JZRA_RENDERING));
		
		highlightsCfg = new ConfigHighlights(playerNode); 
	}

}
