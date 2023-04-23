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







import java.util.HashMap;
import java.util.Map;

import org.jeyzer.analyzer.config.report.ConfigDisplay;
import org.w3c.dom.Element;

import com.google.common.primitives.Ints;

public class ConfigSheetHeader extends ConfigDisplay{

	private static final String JZRR_FUNCTION_PREFIX = "math_function_";
	private static final String JZRR_THRESHOLD_SUFFIX = "_threshold";
	private static final String JZRR_COLOR_SUFFIX = "_color";
	
	// can be null
	private Map<String, Integer> functionThresholds = null;
	private Map<String, String> functionColors = null;

	public ConfigSheetHeader(Element configNode) {
		super(configNode);
		loadHeaderFunctionThresholds();
	}

	private void loadHeaderFunctionThresholds() {
		Map<String, Object> attributes = this.getFields();
		
		for (String attrName : attributes.keySet()){
			if (attrName.startsWith(JZRR_FUNCTION_PREFIX) 
					&& attrName.endsWith(JZRR_THRESHOLD_SUFFIX)){
				
				// need color attribute otherwise ignore
				String colorAttrName = attrName.substring(0, attrName.length()-JZRR_THRESHOLD_SUFFIX.length())
						+ JZRR_COLOR_SUFFIX;
				String colorValue = (String)attributes.get(colorAttrName);
				if (colorValue == null)
					continue;
				
				// load threshold value
				Integer threshold = null;
				String thresholdValue = (String)attributes.get(attrName);
				if (thresholdValue != null && !thresholdValue.isEmpty()){
					threshold = Ints.tryParse(thresholdValue);
					if (threshold == null)
						continue;  // ignore
				}
				
				// fill the header function map
				String function = attrName.substring(
						JZRR_FUNCTION_PREFIX.length(), 
						attrName.length() - JZRR_THRESHOLD_SUFFIX.length()
						);
				if (functionThresholds == null){
					functionThresholds = new HashMap<String, Integer>();
					functionColors = new HashMap<String, String>();
				}
				functionThresholds.put(function, threshold);
				functionColors.put(function, colorValue);
			}
		}
	}

	public Map<String, Integer> getFunctionThresholds() {
		return functionThresholds; // can be null
	}

	public Map<String, String> getFunctionColors() {
		return functionColors; // can be null
	}

	
}
