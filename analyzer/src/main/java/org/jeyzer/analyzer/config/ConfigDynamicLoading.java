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

public class ConfigDynamicLoading {

	private static final String JZRA_DECLARED_REPOSITORY_ONLY = "declared_repository_only";
	
	private boolean dynamicLoadingActive = false;
	private boolean declaredRepositoryOnly = false;
	
	public ConfigDynamicLoading(Element dynamicNode) {
		if (dynamicNode == null || dynamicNode.getFirstChild() == null)
			return;
		
		this.dynamicLoadingActive = true;
		this.declaredRepositoryOnly = Boolean.parseBoolean(ConfigUtil.getAttributeValue(dynamicNode, JZRA_DECLARED_REPOSITORY_ONLY));
	}

	public boolean isDynamicLoadingActive() {
		return dynamicLoadingActive;
	}

	public boolean isDeclaredRepositoryOnly() {
		return declaredRepositoryOnly;
	}
	
}
