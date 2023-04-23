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
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.w3c.dom.Element;

public class ConfigFunctionGraphPlayer extends ConfigGraphPlayer {
	
	public ConfigFunctionGraphPlayer(Element playerRootNode, String outputDirectory) throws JzrInitializationException {
		super(playerRootNode, JZRA_FUNCTION_GRAPH_PLAYER, outputDirectory);
	}

	@Override
	public ConfigFunctionGraphRendering getConfigRendering() {
		return (ConfigFunctionGraphRendering)rendering;
	}
	
	@Override
	protected void init(Element playerNode) throws JzrInitializationException {
		rendering = new ConfigFunctionGraphRendering(
				ConfigUtil.getFirstChildNode(playerNode, ConfigGraphRendering.JZRA_RENDERING));
	}

}
