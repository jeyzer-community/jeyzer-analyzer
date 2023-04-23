package org.jeyzer.analyzer.config.report;

import org.jeyzer.analyzer.config.ConfigUtil;

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

public class ConfigMonitoringRulesSheet extends ConfigSheet {

	public static final String DEFAULT_DESCRIPTION = "Displays monitoring rules applied on the current session.\n"
			+ "Includes rule hit count.";
	
	public static final String TYPE = "monitoring_rules";
	
	private static final String JZRR_DISPLAY = "display";
	private static final String JZRR_LIST_KEY = "list_key";
	
	private static final String KEY_RULE_REF = "rule_ref";
	private static final String KEY_SHEET = "sheet";

	private String key = KEY_RULE_REF;
	
	public ConfigMonitoringRulesSheet(Element configNode, int index) {
		super(configNode, index);
		loadListKey(configNode);
	}

	@Override
	public String getDefaultDescription() {
		return DEFAULT_DESCRIPTION;
	}
	
	private void loadListKey(Element configNode) {
		Element displayNode = ConfigUtil.getFirstChildNode(configNode, JZRR_DISPLAY);
		if (displayNode == null)
			return;
			
		String value = ConfigUtil.getAttributeValue(displayNode,JZRR_LIST_KEY);
		if (value != null && !value.isEmpty()){
			if (KEY_RULE_REF.equalsIgnoreCase(value) || KEY_SHEET.equalsIgnoreCase(value))
				this.key = value;
			else
				logger.warn("Failed to load the list key attribute value " + value + " in the sheet " + this.name + ". Value is invalid. It must be one of : "  + KEY_RULE_REF + " or " + KEY_SHEET + ". Defaulting to list key : " + key);
		}
	}
	
	public boolean isRuleRefListingKey() {
		return KEY_RULE_REF.equalsIgnoreCase(key);
	}
	
	public boolean isSheetListingKey() {
		return KEY_SHEET.equalsIgnoreCase(key);
	}
}
