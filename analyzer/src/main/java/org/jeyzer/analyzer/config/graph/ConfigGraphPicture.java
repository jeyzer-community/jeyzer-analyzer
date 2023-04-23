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




import org.jeyzer.analyzer.config.ConfigTemplate;
import org.jeyzer.analyzer.config.ConfigUtil;
import org.w3c.dom.Element;

public class ConfigGraphPicture {

	public static final String JZRA_GRAPH_PICTURE = "picture";
	
	private static final String JZRA_GRAPH_PICTURE_FILE = "file";
	private static final String JZRA_HTML_FILE = "html_file";
	
	private String outputDirectory;
	private String pictureFileName;
	private String htmlFileName;
	
	private ConfigTemplate templateCfg;
	
	public ConfigGraphPicture(Element graphNode, String outputDirectory){
		this.pictureFileName = ConfigUtil.getAttributeValue(graphNode,JZRA_GRAPH_PICTURE_FILE);
		this.htmlFileName = ConfigUtil.getAttributeValue(graphNode,JZRA_HTML_FILE);
		this.outputDirectory = outputDirectory;
		
		// HTML one
		this.templateCfg = new ConfigTemplate(ConfigUtil.getFirstChildNode(graphNode, ConfigTemplate.JZRA_CONTENT_TEMPLATE));
	}

	public String getOutputDirectory() {
		return outputDirectory;
	}
	
	public String getPicturePath() {
		return outputDirectory + "/" + pictureFileName;
	}
	
	public String getPictureFileName() {
		return pictureFileName;
	}

	public ConfigTemplate getTemplateConfiguration() {
		return templateCfg;
	}
	
	public String getHtmlPath() {
		return outputDirectory + "/" + htmlFileName;
	}

	public void setOutputDirectory(String dir) {
		if (dir != null)
			this.outputDirectory = dir;
	}
}
