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
import java.net.MalformedURLException;

import org.jeyzer.analyzer.config.ConfigUtil;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.w3c.dom.Element;

public class ConfigGraphRendering {

	public static final String JZRA_RENDERING = "rendering";
	public static final String JZRA_MODE = "mode";
	public static final String JZRA_TYPE = "type";
	public static final String JZRA_NODE = "node";
	
	private static final String JZRA_NODE_VALUE_DISPLAY = "node_value_display";
	private static final String JZRA_SIZE_THRESHOLD = "size_threshold";
	private static final String JZRA_STYLE_SHEET = "style_sheet";
	private static final String JZRA_FILE = "file";
	private static final String JZRA_GRAPH_AREA_EXTEND = "graph_area_extend";
	
	private String styleSheetUrl = null;
	private int nodeValueDisplaySizeThresold = -1;

	private ConfigGraphResolution pictureResolution;
	private ConfigGraphExtend graphExtend;
	
	public ConfigGraphRendering(Element renderingNode) throws JzrInitializationException {
		Element styleSheetNode = ConfigUtil.getFirstChildNode(renderingNode, JZRA_STYLE_SHEET);
		if (styleSheetNode != null)
			styleSheetUrl = buildStyleSheetUrl(ConfigUtil.getAttributeValue(styleSheetNode, JZRA_FILE)); // resolve any variable
		
		Element nodeValueDisplayNode = ConfigUtil.getFirstChildNode(renderingNode, JZRA_NODE_VALUE_DISPLAY);
		if (nodeValueDisplayNode != null)
			this.nodeValueDisplaySizeThresold = Integer.valueOf(ConfigUtil.getAttributeValue(nodeValueDisplayNode,JZRA_SIZE_THRESHOLD));
		
		this.graphExtend = new ConfigGraphExtend(ConfigUtil.getFirstChildNode(renderingNode, JZRA_GRAPH_AREA_EXTEND));
		
		this.pictureResolution = new ConfigGraphResolution(ConfigUtil.getFirstChildNode(renderingNode, ConfigGraphResolution.JZRA_RESOLUTION));
	}

	public String getStyleSheetUrl(){
		return styleSheetUrl;
	}

	public ConfigGraphResolution getPictureResolution() {
		return pictureResolution;
	}

	public ConfigGraphExtend getGraphExtend() {
		return graphExtend;
	}
	
	public int getNodeValueDisplaySizeThresold() {
		return nodeValueDisplaySizeThresold;
	}

	private String buildStyleSheetUrl(String path) throws JzrInitializationException {
		String url =  null;
		try {
			url = "url(" + new File(path).toURI().toURL() + ")";
		} catch (MalformedURLException e) {
			throw new JzrInitializationException("Failed to create graph stream css file URI for path : " + path, e);
		}
		return url;
	}
	
}
