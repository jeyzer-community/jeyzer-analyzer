package org.jeyzer.analyzer.config.analysis;

/*-
 * ---------------------------LICENSE_START---------------------------
 * Jeyzer Analyzer
 * --
 * Copyright (C) 2020 - 2021 Jeyzer
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
import org.w3c.dom.NodeList;

public class ConfigProfileDiscovery {
	
	private static final String JZRA_DISCOVERY_PROFILE_REDIRECTION = "profile_redirection";
	private static final String JZRA_DISCOVERY_ID_PATTERNS = "id_patterns";
	private static final String JZRA_DISCOVERY_ID_PATTERN = "id_pattern";
	private static final String JZRA_ENABLED = "enabled";
	private static final String JZRA_VALUE = "value";

	private boolean redirectionEnabled = false;
	private List<String> patterns = new ArrayList<>();
	
	public ConfigProfileDiscovery(Element discoveryNode) {
		if (discoveryNode == null)
			return;
		
		Element redirectNode = ConfigUtil.getFirstChildNode(discoveryNode, JZRA_DISCOVERY_PROFILE_REDIRECTION);
		if (redirectNode == null)
			return;
		
		String value = ConfigUtil.getAttributeValue(redirectNode, JZRA_ENABLED);
		this.redirectionEnabled = Boolean.parseBoolean(value);
		
		loadIdPatterns(redirectNode);
	}
	
	private void loadIdPatterns(Element redirectNode) {
		NodeList patternIdNodes = redirectNode.getElementsByTagName(JZRA_DISCOVERY_ID_PATTERNS);
		if (patternIdNodes == null)
			return;
		
		Element patternIdNode = (Element)patternIdNodes.item(0);
		if (patternIdNode == null)
			return;
		
		NodeList patternIdNodeList = patternIdNode.getElementsByTagName(JZRA_DISCOVERY_ID_PATTERN);
		if (patternIdNodeList == null)
			return;

		for (int i=0; i<patternIdNodeList.getLength(); i++){
			Element patternNode = (Element)patternIdNodeList.item(i);			
			String pattern = ConfigUtil.getAttributeValue(patternNode,JZRA_VALUE);
			patterns.add(pattern);
		}
	}

	public boolean isRedirectEnabled(){
		return redirectionEnabled;
	}
	
	public List<String> getRedirectionPatterns(){
		return patterns;
	}
}
