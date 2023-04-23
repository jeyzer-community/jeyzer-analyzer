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




import org.jeyzer.analyzer.config.ConfigUtil;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.monitor.engine.event.info.Level;
import org.jeyzer.monitor.engine.event.info.SubLevel;
import org.w3c.dom.Element;

import com.google.common.primitives.Ints;

public class ConfigMonitorTaskThreshold extends ConfigMonitorThreshold{
	
	private static final String JZRM_FUNCTION = "function";
	private static final String JZRM_PERCENTAGE_IN_ACTION = "percentage_in_action";
	
	static{
		supportedParameters.add(JZRM_FUNCTION);
		supportedParameters.add(JZRM_PERCENTAGE_IN_ACTION);
	}
	
	private String function;
	private Integer percentage;
	
	/**
	 * Analyzer threshold constructor
	 */
	public ConfigMonitorTaskThreshold(Element thresholdNode, String name, String ref) throws JzrInitializationException {
		super(thresholdNode, name, ref);
		this.function = ConfigUtil.getAttributeValue(thresholdNode,JZRM_FUNCTION);
		this.percentage = loadPercentage(thresholdNode);
	}
	
	/**
	 * Analyzer applicative threshold constructor
	 */
	public ConfigMonitorTaskThreshold(String name, String ref, Level level, SubLevel subLevel, String message, short trustFactor){
		super(name, ref, level, subLevel, message, trustFactor);
	}

	public String getFunction() {
		return function;
	}

	public Integer getPercentageInAction() {
		return (this.percentage != null)? percentage : -1;
	}	
	
	private Integer loadPercentage(Element thresholdNode) {
		String value = ConfigUtil.getAttributeValue(thresholdNode,JZRM_PERCENTAGE_IN_ACTION);
		if (value == null || value.isEmpty())
			return null;
		
		Integer parsedValue = Ints.tryParse(value);
		if (parsedValue == null ||  parsedValue>100 || parsedValue<0)
			return null;
		
		return parsedValue;
	}
	
	public boolean hasPercentage(){
		return this.percentage != null;
	}
	
}
