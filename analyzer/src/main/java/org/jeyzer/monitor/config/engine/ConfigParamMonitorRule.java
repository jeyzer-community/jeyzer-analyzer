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
import org.jeyzer.monitor.util.Operator;
import org.w3c.dom.Element;

public class ConfigParamMonitorRule extends ConfigMonitorRule{
	
	private static final String JZRM_MX_PARAM = "param";
	private static final String JZRM_MX_PARAM_DISPLAY = "param_display";
	private static final String JZRM_MX_OPERATOR = "operator";
	
	protected String paramName;    // unique
	private String displayParam; // unique
	private Operator operator;
	
	public ConfigParamMonitorRule (Element ruleNode, String group, List<String> groupStickerRefs, boolean dynamic) throws JzrInitializationException {
		super(ruleNode, group, groupStickerRefs, dynamic);
		this.paramName = ConfigUtil.getAttributeValue(ruleNode,JZRM_MX_PARAM);
		this.displayParam = ConfigUtil.getAttributeValue(ruleNode,JZRM_MX_PARAM_DISPLAY);
		this.operator = Operator.buildOperator(ConfigUtil.getAttributeValue(ruleNode,JZRM_MX_OPERATOR)); // optional
	}
	
	public String getParamName(){
		return this.paramName;
	}

	public String getDisplayName(){
		return this.displayParam;
	}
	
	public Operator getOperator(){
		return this.operator;
	}
	
}
