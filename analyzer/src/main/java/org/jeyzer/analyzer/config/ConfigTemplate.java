package org.jeyzer.analyzer.config;

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

import org.w3c.dom.Element;

public class ConfigTemplate {
	
	public static final String JZRA_CONTENT_TEMPLATE = "template";
	
	private static final String JZRA_CONTENT_TEMPLATE_DIR = "directory";
	private static final String JZRA_CONTENT_TEMPLATE_NAME = "name";
	
	private String templateDirectory;
	private String templateName;

	public ConfigTemplate(Element templateNode){
		this.templateDirectory = ConfigUtil.getAttributeValue(templateNode,JZRA_CONTENT_TEMPLATE_DIR);
		this.templateName = ConfigUtil.getAttributeValue(templateNode,JZRA_CONTENT_TEMPLATE_NAME);
	}
	
	public String getTemplateDirectory() {
		return templateDirectory;
	}

	public String getTemplateName() {
		return templateName;
	}
}
