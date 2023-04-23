package org.jeyzer.monitor.config.engine;

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







import java.util.List;

import org.jeyzer.analyzer.config.ConfigUtil;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.w3c.dom.Element;

public class ConfigPrincipalMonitorRule extends ConfigParamMonitorRule{
	
	private static final String JZRM_PRINCIPAL = "principal";
	
	String principal;    // unique
	
	public ConfigPrincipalMonitorRule (Element ruleNode, String group, List<String> groupStickerRefs, boolean dynamic) throws JzrInitializationException {
		super(ruleNode, group, groupStickerRefs, dynamic);
		this.principal = ConfigUtil.getAttributeValue(ruleNode,JZRM_PRINCIPAL);
		if (principal == null || principal.isEmpty())
			throw new JzrInitializationException("Monitoring rule is missing the principal parameter.");
		if (principal.equals(this.paramName))
			throw new JzrInitializationException("Monitoring rule cannot have param name equal to principal.");
	}
	
	public String getPrincipal(){
		return this.principal;
	}
	
}
