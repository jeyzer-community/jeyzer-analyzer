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
import org.w3c.dom.Element;

public class ConfigGraphViewer {

	public static final String JZRA_GRAPH_VIEWER = "viewer";
	private static final String JZRA_GRAPH_VIEWER_ENABLED = "enabled";
	
	private boolean enabled;
	
	public ConfigGraphViewer(Element viewerNode){
		enabled = Boolean.parseBoolean(ConfigUtil.getAttributeValue(viewerNode,JZRA_GRAPH_VIEWER_ENABLED));
	}

	public boolean isEnabled() {
		return enabled;
	}
}
