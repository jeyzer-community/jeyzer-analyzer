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

import org.jeyzer.analyzer.config.ConfigUtil;
import org.jeyzer.analyzer.config.report.headers.ConfigSheetHeadersSets;
import org.jeyzer.analyzer.config.report.headers.ConfigSheetRowHeaders;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class ConfigSequenceSheet extends ConfigSheet {
	
	public static final String TYPE = "sequence";
	
	public static final String DEFAULT_DESCRIPTION = "Displays actions in a time line manner along with process info (thread count, CPU..).\n"
			+ "Each configured task sequence sheet permits to focus on specific topics : operations, locks, thread state, memory, gc..";
	
	private static final String JZRR_DISPLAY = "display";
	private static final String JZRR_HEADERS = "headers";
	private static final String JZRR_ROW_HEADERS = "row_headers";
	private static final String JZRR_CHARTS = "charts";
	private static final String JZRR_LINK_TYPE = "link_type";

	private String linkType; // can be null
	private final List<ConfigDisplay> displayConfigs = new ArrayList<>();

	private ConfigSheetRowHeaders rowHeaderConfigSets;
	private ConfigSheetHeadersSets headersConfigSets;

	private final List<ConfigChart> chartConfigs = new ArrayList<>();

	public ConfigSequenceSheet(Element configNode, int index) throws JzrInitializationException{
		super(configNode, index);
		this.linkType = ConfigUtil.getAttributeValue(configNode, JZRR_LINK_TYPE);
		
		loadHeadersPerFormat(configNode);
		loadRowHeaders(configNode);
		loadCharts(configNode);
		loadDisplay(configNode);
	}

	private void loadRowHeaders(Element configNode) {
		Element rowHeadersNode = ConfigUtil.getFirstChildNode(configNode, JZRR_ROW_HEADERS);
		this.rowHeaderConfigSets = new ConfigSheetRowHeaders(rowHeadersNode);
	}

	private void loadHeadersPerFormat(Element configNode) throws JzrInitializationException {
		NodeList headersSetNodes = configNode.getElementsByTagName(JZRR_HEADERS);
		this.headersConfigSets = new ConfigSheetHeadersSets(headersSetNodes);
	}

	private void loadCharts(Element configNode) {
		Element chartsNode = ConfigUtil.getFirstChildNode(configNode, JZRR_CHARTS);
		if(chartsNode == null)
			return;
			
		// charts
		NodeList chartNodes = chartsNode.getChildNodes();
		for(int i=0; i<chartNodes.getLength(); i++){
			Node node = chartNodes.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE){
				ConfigChart chart = new ConfigChart((Element)node);
				this.chartConfigs.add(chart);
			}
		}
	}
	
	private void loadDisplay(Element configNode) {
		Element displayNode = ConfigUtil.getFirstChildNode(configNode, JZRR_DISPLAY);
		
		NodeList displayNodes = displayNode.getChildNodes();
		
		for(int i=0; i<displayNodes.getLength(); i++){
			Node node = displayNodes.item(i);
			
			if (node.getNodeType() == Node.ELEMENT_NODE){
				ConfigDisplay display = new ConfigDisplay((Element)node);
				this.displayConfigs.add(display);	
			}			
		}		
	}

	public List<ConfigDisplay> getDisplayConfigs() {
		return displayConfigs;
	}
	
	public ConfigSheetRowHeaders getRowHeadersConfig() {
		return rowHeaderConfigSets;
	}

	public ConfigSheetHeadersSets getHeaderConfigsSets() {
		return headersConfigSets;
	}
	
	public List<ConfigChart> getChartConfigs() {
		return chartConfigs;
	}	

	public boolean isLinkable() {
		return linkType != null && !linkType.isEmpty();
	}
	
	public String getLinkType() {
		return linkType;
	}

	@Override
	public String getDefaultDescription() {
		return DEFAULT_DESCRIPTION;
	}	
	
}
