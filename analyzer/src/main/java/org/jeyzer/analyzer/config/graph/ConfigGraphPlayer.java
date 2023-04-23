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







import java.io.File;

import org.jeyzer.analyzer.config.ConfigUtil;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public abstract class ConfigGraphPlayer {
	
	public static final String JZRA_FUNCTION_GRAPH_PLAYER = "function_graph_player";
	public static final String JZRA_CONTENTION_GRAPH_PLAYER = "contention_graph_player";
	private static final String JZRA_GRAPH_CONFIG_FILE = "graph_config_file";
	
	private ConfigGraphPicture picture;
	private ConfigGraphViewer viewer;
	protected ConfigGraphRendering rendering;
	
	public ConfigGraphPlayer(Element playerRootNode, String playerRootNodeType, String outputDirectory) throws JzrInitializationException{
		Element playerNode = loadPlayerNode(playerRootNode, playerRootNodeType);
		
		viewer = new ConfigGraphViewer(
				ConfigUtil.getFirstChildNode(playerNode, ConfigGraphViewer.JZRA_GRAPH_VIEWER));
		picture = new ConfigGraphPicture(
				ConfigUtil.getFirstChildNode(playerNode, ConfigGraphPicture.JZRA_GRAPH_PICTURE), 
				outputDirectory);
		
		init(playerNode);
	}
	
	protected abstract void init(Element playerNode)  throws JzrInitializationException;

	public ConfigGraphRendering getConfigRendering() {
		return rendering;
	}
	
	public ConfigGraphPicture getConfigPicture() {
		return picture;
	}

	public ConfigGraphViewer getConfigViewer() {
		return viewer;
	}
	
	protected Element loadPlayerNode(Element playerRootNode, String playerRootNodeType) throws JzrInitializationException{
		String configFilePath = ConfigUtil.getAttributeValue(playerRootNode, JZRA_GRAPH_CONFIG_FILE);
		File graphConfigFile = new File(configFilePath);
		if (!graphConfigFile.exists())
			throw new JzrInitializationException("File " + graphConfigFile.getPath() + " not found.");
		
		Document doc = ConfigUtil.loadDOM(graphConfigFile);
		
		// report
		NodeList nodes = doc.getElementsByTagName(playerRootNodeType);
		return (Element)nodes.item(0);
	}

}
