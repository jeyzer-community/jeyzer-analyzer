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




import org.jeyzer.analyzer.config.ConfigUtil;
import org.w3c.dom.Element;

public abstract class ConfigSheetAbstractHeaders {
	
	public enum HEADER_FREEZE_MODE { FREEZE, UNFREEZE, NOT_SET }
	
	private static final String JZRR_FREEZE = "freeze";

	private HEADER_FREEZE_MODE freezeMode;
	
	public ConfigSheetAbstractHeaders(Element configNode) {
		freezeMode = loadFreezeMode(configNode);
	}
	
	private HEADER_FREEZE_MODE loadFreezeMode(Element headersSetNode) {
		String value = ConfigUtil.getAttributeValue(headersSetNode, JZRR_FREEZE);
		if (value != null && !value.isEmpty())
			return Boolean.parseBoolean(value) ? HEADER_FREEZE_MODE.FREEZE : HEADER_FREEZE_MODE.UNFREEZE;
		else
			return HEADER_FREEZE_MODE.NOT_SET;
	}
	
	public HEADER_FREEZE_MODE getFreezeMode() {
		return freezeMode;
	}
	
}
