package org.jeyzer.analyzer.config.report;

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

public class ConfigActionDistinctHistogramSheet extends ConfigProfilingSheet {

	public static final String DEFAULT_DESCRIPTION = "Lists all the functions and operations  - grouped per distinct action - for the long running actions.\n"
			+ "Permits to spot concentration points on particular actions.";
	
	public static final String TYPE = "action_distinct_histogram";
	
	private static final String JZRR_THRESHOLD = "threshold";
	
	private int threshold;	
	
	public ConfigActionDistinctHistogramSheet(Element configNode, int index){
		super(configNode, index);
		this.threshold = Integer.valueOf(ConfigUtil.getAttributeValue(configNode,JZRR_THRESHOLD));		
	}
	
	public int getThreshold() {
		return threshold;
	}	

	@Override
	public String getDefaultDescription() {
		return DEFAULT_DESCRIPTION;
	}

}
