package org.jeyzer.analyzer.output.poi.rule.header;

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







import java.util.Map;

import org.jeyzer.analyzer.config.report.headers.ConfigSheetHeader;
import org.jeyzer.analyzer.output.poi.context.SheetDisplayContext;
import org.jeyzer.analyzer.output.poi.rule.AbstractNumericDisplayRule;

public abstract class AbstractNumericDisplayHeaderRule extends AbstractNumericDisplayRule{

	private Map<String, Integer> functionThresholds = null;
	private Map<String, String> functionColors = null;	
	
	public AbstractNumericDisplayHeaderRule(ConfigSheetHeader headerCfg, SheetDisplayContext context) {
		super(headerCfg, context);
		this.functionThresholds = headerCfg.getFunctionThresholds();
		this.functionColors = headerCfg.getFunctionColors();
	}
	
	public Integer getFunctionThreshold(String function){
		if (functionThresholds == null)
			return null;
		return this.functionThresholds.get(function);
	}
	
	public String getFunctionColor(String function){
		if (functionColors == null)
			return null;
		return this.functionColors.get(function);
	}
	
}
