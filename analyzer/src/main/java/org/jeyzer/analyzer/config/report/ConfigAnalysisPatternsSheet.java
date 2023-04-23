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
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ConfigAnalysisPatternsSheet extends ConfigSheet {
	
	public static final String TYPE = "analysis_patterns";
	
	public static final String DEFAULT_DESCRIPTION = "Lists all the master and shared analysis patterns used to generate this report.";
	
	public static final String JZRR_EXCLUDES = "excludes";
	public static final String JZRR_OPERATIONS = "operations";
	public static final String JZRR_DISCOVERY_OPERATIONS = "discovery_operations";
	public static final String JZRR_FUNCTIONS = "functions";
	public static final String JZRR_DISCOVERY_FUNCTIONS = "discovery_functions";
	public static final String JZRR_LOCKERS = "lockers";
	public static final String JZRR_EXECUTORS = "executors";
	
	private static final String JZRR_DISPLAY = "display";
	
	private final List<ConfigDisplay> displayConfigs = new ArrayList<>();
	
	public ConfigAnalysisPatternsSheet(Element configNode, int index){
		super(configNode, index);
		
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

	@Override
	public String getDefaultDescription() {
		return DEFAULT_DESCRIPTION;
	}
	
	public List<ConfigDisplay> getDisplayConfigs(){
		return this.displayConfigs;
	}
}
