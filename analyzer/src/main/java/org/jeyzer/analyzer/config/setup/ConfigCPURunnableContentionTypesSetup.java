package org.jeyzer.analyzer.config.setup;

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
import org.w3c.dom.NodeList;

public class ConfigCPURunnableContentionTypesSetup {
	
	private static final String JZRA_CONTENTION_TYPES = "contention_types";
	private static final String JZRA_CONTENTION_MODE = "mode";
	private static final String JZRA_CONTENTION_TYPES_INCLUDES = "includes";
	private static final String JZRA_CONTENTION_TYPES_EXCLUDES = "excludes";
	private static final String JZRA_CONTENTION_TYPE = "contention_type";
	private static final String JZRA_CONTENTION_TYPE_NAME = "name";
	
	private boolean contentionTypesInclude;
	private List<String> contentionTypesIncludes = new ArrayList<>();
	private List<String> contentionTypesExcludes = new ArrayList<>();
	
	public ConfigCPURunnableContentionTypesSetup(Element cpuRunnableNode) {
		Element contentionTypesNode = ConfigUtil.getFirstChildNode(cpuRunnableNode, JZRA_CONTENTION_TYPES);
		String mode = ConfigUtil.getAttributeValue(contentionTypesNode, JZRA_CONTENTION_MODE);
		
		contentionTypesInclude = JZRA_CONTENTION_TYPES_INCLUDES.equals(mode) || mode.isEmpty(); 
		if (contentionTypesInclude)
			loadIncludesOrExcludes(contentionTypesNode, contentionTypesIncludes, JZRA_CONTENTION_TYPES_INCLUDES);
		else
			loadIncludesOrExcludes(contentionTypesNode, contentionTypesExcludes, JZRA_CONTENTION_TYPES_EXCLUDES);
	}

	public boolean isContentionTypesInclude(){
		return contentionTypesInclude;
	}
	
	public List<String> getContentionTypesIncludes(){
		return contentionTypesIncludes;
	}
	
	public List<String> getContentionTypesExcludes(){
		return contentionTypesExcludes;
	}

	private void loadIncludesOrExcludes(Element contentionTypesNode, List<String> contentionTypesElements, String includesOrExcludes) {
		
		Element includesOrExcludesNodes = ConfigUtil.getFirstChildNode(contentionTypesNode, includesOrExcludes);
		NodeList contentiontypesNodeList = includesOrExcludesNodes.getElementsByTagName(JZRA_CONTENTION_TYPE);
		
		for (int i=0; i<contentiontypesNodeList.getLength(); i++){
			Element includesOrExcludesNode = (Element)contentiontypesNodeList.item(i);
			
			String name = ConfigUtil.getAttributeValue(includesOrExcludesNode,JZRA_CONTENTION_TYPE_NAME);
			if (name != null && !name.isEmpty())
				contentionTypesElements.add(name);
		}		
	}
}
