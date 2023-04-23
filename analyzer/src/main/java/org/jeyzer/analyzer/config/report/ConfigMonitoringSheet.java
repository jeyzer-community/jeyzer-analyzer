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







import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.poi.xssf.usermodel.XSSFColor;
import org.jeyzer.analyzer.config.ConfigUtil;
import org.jeyzer.analyzer.config.graph.ConfigContentionGraphPlayer;
import org.jeyzer.analyzer.config.graph.ConfigFunctionGraphPlayer;
import org.jeyzer.analyzer.config.graph.ConfigGraphPlayer;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.monitor.config.engine.ConfigMonitorRules;
import org.jeyzer.monitor.config.sticker.ConfigStickers;
import org.jeyzer.service.location.JzrLocationResolver;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ConfigMonitoringSheet extends ConfigSheet {

	public static final String TYPE = "monitoring_events";
	
	public static final String DEFAULT_DESCRIPTION = "Displays all the raised monitoring events.\n"
			+ "Permits to identify problems at a glance.";
	
	private static final String JZRR_CRITICAL_TAB_COLOR = "critical_tab_color";
	private static final String JZRR_DISPLAY = "display";
	private static final String JZRR_DATE_FORMAT = "date_format";
	private static final String JZRR_GROUPING = "grouping";
	private static final String JZRR_GROUP_SORTING = "group_sorting";
	private static final String JZRR_CLEAN_DUPLICATE_EVENTS = "clean_duplicate_events";
	private static final String JZRR_RANKING = "ranking";
	private static final String JZRR_LINKS = "links";
	private static final String JZRR_SEQUENCE_SHEETS = "sequence_sheets";
	
	private boolean grouping;
	private boolean groupSorting;
	private boolean duplicateEventCleanup;
	private String dateFormat;
	private ConfigMonitorRules rules;
	private ConfigStickers stickers; // can be null
	
	// required to load dynamically the rules
	private JzrLocationResolver resolver;
	
	private List<String> links;
	
	// optional
	private ConfigHighlights rankingHighlights;
	
	// optional
	private ConfigFunctionGraphPlayer functionGraphPlayer;
	private ConfigGraph functionGraph;
	
	// optional
	private ConfigContentionGraphPlayer contentionGraphPlayer;
	private ConfigGraph contentionGraph;
	
	protected XSSFColor criticalColor = null; // none
	
	public ConfigMonitoringSheet(Element configNode, int index, JzrLocationResolver resolver) throws JzrInitializationException{
		super(configNode, index);
		
		Element rulesNode = ConfigUtil.getFirstChildNode(configNode, ConfigMonitorRules.JZRM_RULES);
		if (rulesNode == null)
			throw new JzrInitializationException("Rules node is missing in the monitoring sheet configuration.");
		this.rules = new ConfigMonitorRules(rulesNode, resolver);
		this.resolver = resolver;

		Element stickersNode = ConfigUtil.getFirstChildNode(configNode, ConfigStickers.JZRM_STICKERS);
		if (stickersNode != null)
			this.stickers = new ConfigStickers(stickersNode, resolver);
		
		criticalColor = loadColor(configNode, JZRR_CRITICAL_TAB_COLOR);
		
		Element displayNode = ConfigUtil.getFirstChildNode(configNode, JZRR_DISPLAY);
		dateFormat = ConfigUtil.getAttributeValue(displayNode,JZRR_DATE_FORMAT);
		
		grouping = Boolean.valueOf(ConfigUtil.getAttributeValue(displayNode,JZRR_GROUPING));
		groupSorting = Boolean.valueOf(ConfigUtil.getAttributeValue(displayNode,JZRR_GROUP_SORTING));
		
		// If enabled, it means that several rules of same nature have been defined and are generating therefore duplicate events.
		// It would be preferable to review the rules, which is not always possible when handling various rule sets.
		// Note that, within one rule, the threshold management is already and always preventing the event duplication. 
		duplicateEventCleanup = Boolean.valueOf(ConfigUtil.getAttributeValue(displayNode,JZRR_CLEAN_DUPLICATE_EVENTS));
		
		Element rankingNode = ConfigUtil.getFirstChildNode(displayNode, JZRR_RANKING);
		this.rankingHighlights = new ConfigHighlights(rankingNode);
		
		loadLinks(displayNode);
		
		loadGlobalFunctionGraphConfig(configNode);  // optional
		loadGlobalContentionGraphConfig(configNode);  // optional
	}

	private void loadLinks(Element configNode) {
		this.links = new ArrayList<>();
		
		Element linksNode = ConfigUtil.getFirstChildNode(configNode, JZRR_LINKS);
		if (linksNode == null)
			return;
		
		String value = ConfigUtil.getAttributeValue(linksNode, JZRR_SEQUENCE_SHEETS);
		if (value == null || value.isEmpty())
			return;
		
		StringTokenizer tokenizer = new StringTokenizer(value, ",");
		while (tokenizer.hasMoreTokens()){
			String token = tokenizer.nextToken().trim();
			if (!token.isEmpty())
				this.links.add(token);
		}
	}

	public ConfigMonitorRules getConfigMonitorRules(){
		return rules;
	}
	
	public JzrLocationResolver getJzrLocationResolver() {
		return this.resolver;
	}
	
	public ConfigStickers getConfigStickers(){
		return stickers;
	}
	
	public ConfigHighlights getRankingHighlights(){
		return rankingHighlights;
	}
	
	public List<String> getSequenceSheetLinks(){
		return links;
	}
	
	public ConfigFunctionGraphPlayer getConfigTDFunctionGraphPlayer(){
		return functionGraphPlayer;  //  may be null
	}
	
	public ConfigGraph getConfigFunctionGraph(){
		return functionGraph;  //  may be null
	}
	
	public boolean isFunctionGraphDisplayed(){
		return functionGraphPlayer!= null && functionGraph != null; 
	}
	
	public ConfigContentionGraphPlayer getConfigTDContentionGraphPlayer(){
		return contentionGraphPlayer;  //  may be null
	}
	
	public ConfigGraph getConfigContentionGraph(){
		return contentionGraph;  //  may be null
	}
	
	public boolean isContentionGraphDisplayed(){
		return contentionGraphPlayer!= null && contentionGraph != null; 
	}
	
	public XSSFColor getCriticalColor() {
		return criticalColor; // can be null
	}

	public String getDateFormat() {
		return dateFormat;
	}
	
	public boolean isGrouping() {
		return grouping;
	}

	public boolean isGroupSorting() {
		return groupSorting;
	}

	@Override
	public String getDefaultDescription() {
		return DEFAULT_DESCRIPTION;
	}
	
	private void loadGlobalContentionGraphConfig(Element configNode) throws JzrInitializationException {
		Element contentionGraphNode = ConfigUtil.getFirstChildNode(configNode, ConfigGraph.JZRA_CONTENTION_GRAPH);
		if (contentionGraphNode == null)
			return;
		
		String file = ConfigUtil.getAttributeValue(contentionGraphNode, ConfigGraph.JZRA_FILE); // resolve any variable
		if (!file.isEmpty()){
			contentionGraphNode = loadGlobalGraphConfigurationFile(file, ConfigGraph.JZRA_CONTENTION_GRAPH);
		}

		Element contentionGraphPlayerNode = ConfigUtil.getFirstChildNode(contentionGraphNode, ConfigGraphPlayer.JZRA_CONTENTION_GRAPH_PLAYER);
		if (contentionGraphPlayerNode != null){
			contentionGraphPlayer = new ConfigContentionGraphPlayer(contentionGraphPlayerNode, null); 
		}
		
		Element graphNode = ConfigUtil.getFirstChildNode(contentionGraphNode, ConfigGraph.JZRA_GRAPH);
		if (contentionGraphNode != null)
			contentionGraph = new ConfigGraph(graphNode);  // No contention types here. Managed inside the player
	}

	private void loadGlobalFunctionGraphConfig(Element configNode) throws JzrInitializationException {
		Element functionGraphNode = ConfigUtil.getFirstChildNode(configNode, ConfigGraph.JZRA_FUNCTION_GRAPH);
		if (functionGraphNode == null)
			return;
		
		String file = ConfigUtil.getAttributeValue(functionGraphNode, ConfigGraph.JZRA_FILE); // resolve any variable
		if (!file.isEmpty()){
			functionGraphNode = loadGlobalGraphConfigurationFile(file, ConfigGraph.JZRA_FUNCTION_GRAPH);
		}

		Element functionGraphPlayerNode = ConfigUtil.getFirstChildNode(functionGraphNode, ConfigGraphPlayer.JZRA_FUNCTION_GRAPH_PLAYER);
		if (functionGraphPlayerNode != null){
			functionGraphPlayer = new ConfigFunctionGraphPlayer(functionGraphPlayerNode, null); 
		}
		
		Element graphNode = ConfigUtil.getFirstChildNode(functionGraphNode, ConfigGraph.JZRA_GRAPH);
		if (graphNode != null)
			functionGraph = new ConfigGraph(graphNode);
	}
	
	private Element loadGlobalGraphConfigurationFile(String path, String graphType) {
		Document doc;
		
		try {
			doc = ConfigUtil.loadXMLFile(path);
			if (doc == null){
				logger.error("Failed to open the global graph configuration resource using path : " + path);
				return null;
			}
		} catch (Exception e) {
			logger.error("Failed to open the global graph configuration resource using path : " + path, e);
			return null;
		}
		
		NodeList nodes = doc.getElementsByTagName(graphType);
		Element node = (Element)nodes.item(0);
		if (node == null){
			logger.error("Global graph configuration " + path + " is invalid.");
			return null;
		}
		
		return node;
	}

	public boolean hasDuplicateEventCleanup() {
		return duplicateEventCleanup;
	}
}
