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







import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jeyzer.analyzer.config.ConfigUtil;
import org.jeyzer.analyzer.output.poi.CellColor;
import org.jeyzer.analyzer.output.poi.rule.highlight.HighLightBuilder;
import org.jeyzer.analyzer.output.poi.rule.highlight.Highlight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ConfigHighlights {

	private static final Logger logger = LoggerFactory.getLogger(ConfigHighlights.class);
	
	private static final String HIGHLIGHT = "highlight";
	private static final String HIGHLIGHTS = "highlights";
	private static final String HIGHLIGHTS_FILE = "file";
	
	private List<ConfigDisplay> cfgHighlights = new ArrayList<>();
	
	public ConfigHighlights(Element configNode) {
		if (configNode == null)
			return;
		
		loadHighlights(configNode);
		
		NodeList nodes = configNode.getElementsByTagName(HIGHLIGHTS);
		for (int i=0; i<nodes.getLength(); i++){
			String path = ConfigUtil.getAttributeValue((Element)nodes.item(0), HIGHLIGHTS_FILE);
			if (path!= null && !path.isEmpty()){
				loadHighlightsConfigurationFile(path);
			}
		}
	}
	
	public Map<String, String> getHexaHighlights() {
		Map<String, String> hexaHighlights = new HashMap<>();
		
		List<Highlight> highlights = HighLightBuilder.newInstance().buildHighLights(this.cfgHighlights);
		
		for (Highlight highlight : highlights){
			highlight.getName();
			Color color = CellColor.buildRGBColor(highlight.getColor()); // convert any indexed color in RGB
			if (color != null){
				String hex = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
				hexaHighlights.put(highlight.getName(), hex);
			}
		}
		
		return hexaHighlights;
	}

	private void loadHighlights(Element configNode) {
		NodeList nodes = configNode.getElementsByTagName(HIGHLIGHT);
		for (int i=0; i<nodes.getLength(); i++){
			this.cfgHighlights.add(new ConfigDisplay((Element)nodes.item(i)));
		}
	}

	public List<ConfigDisplay> getHighlights() {
		return cfgHighlights;
	}

	private void loadHighlightsConfigurationFile(String path) {
		Document doc;
		
		try {
			doc = ConfigUtil.loadXMLFile(path);
			if (doc == null){
				logger.error("Failed to open the highlights configuration resource using path : " + path);
				return;
			}
		} catch (Exception e) {
			logger.error("Failed to open the highlights configuration resource using path : " + path, e);
			return;
		}
		
		NodeList nodes = doc.getElementsByTagName(HIGHLIGHTS);
		Element highlightsNode = (Element)nodes.item(0);
		if (highlightsNode == null){
			logger.error("Highlights configuration " + path + " is invalid.");
			return;
		}
		
		loadHighlights(highlightsNode);
	}
	
	public static boolean hasHighlights(Element configNode){
		NodeList nodes = configNode.getElementsByTagName(HIGHLIGHT);
		if (nodes.getLength() > 0)
			return true;
		nodes = configNode.getElementsByTagName(HIGHLIGHTS);
		if (nodes.getLength() > 0)
			return true;
		return false;
	}
	
}
