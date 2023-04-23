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




import org.jeyzer.analyzer.config.ConfigUtil;
import org.jeyzer.analyzer.config.report.ConfigGraph;
import org.w3c.dom.Element;

public class ConfigGraphSetup {

	private static final String JZRA_DISPLAY = "display";
	
	private static final String JZRA_OUTPUT = "output";
	private static final String JZRA_ROOT_DIR = "root_directory";
	private static final String JZRA_ARCHIVE = "archive";
	private static final String JZRA_ENABLED = "enabled";
	
	private ConfigGraph defaultConfigGraph;
	
	private String rootDirectory;
	private boolean archivingEnabled;
	
	public ConfigGraphSetup(Element graphNode) {
		loadGraph(graphNode);
	}

	private void loadGraph(Element graphNode) {
		Element displayNode = ConfigUtil.getFirstChildNode(graphNode, JZRA_DISPLAY);
		defaultConfigGraph = new ConfigGraph(displayNode);
		
		Element outputNode = ConfigUtil.getFirstChildNode(graphNode, JZRA_OUTPUT);
		rootDirectory = ConfigUtil.getAttributeValue(outputNode, JZRA_ROOT_DIR); // resolve any variable
		
		Element archiveNode = ConfigUtil.getFirstChildNode(outputNode, JZRA_ARCHIVE);
		archivingEnabled = Boolean.parseBoolean(ConfigUtil.getAttributeValue(archiveNode, JZRA_ENABLED));
	}

	public ConfigGraph getDefaultConfigGraph() {
		return defaultConfigGraph;
	}

	public String getRootDirectory() {
		return rootDirectory;
	}

	public boolean isArchivingEnabled() {
		return archivingEnabled;
	}

}
