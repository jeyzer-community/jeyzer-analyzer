package org.jeyzer.analyzer.config.report.headers;

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

import org.jeyzer.analyzer.config.report.ConfigDisplay;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ConfigSheetRowHeaders extends ConfigSheetAbstractHeaders {

	private List<ConfigDisplay> headerConfigs;
	
	public ConfigSheetRowHeaders(Element configNode) {
		super(configNode);
		headerConfigs = loadHeaders(configNode);
	}
	
	private List<ConfigDisplay> loadHeaders(Element headersSetNode) {
		List<ConfigDisplay> headers = new ArrayList<>();
		NodeList headerNodes = headersSetNode.getChildNodes();
		for(int j=0; j<headerNodes.getLength(); j++){
			Node node = headerNodes.item(j);
			if (node.getNodeType() == Node.ELEMENT_NODE){
				ConfigDisplay header = new ConfigDisplay((Element)node);
				headers.add(header);
			}
		}
		return headers;
	}	

	public List<ConfigDisplay> getHeaderConfigs() {
		return headerConfigs;
	}
}
