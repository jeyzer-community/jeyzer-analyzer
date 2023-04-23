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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import com.google.common.primitives.Ints;

public class ConfigATBIProfilingSheet extends ConfigActionProfilingSheet {

	private static final Logger logger = LoggerFactory.getLogger(ConfigATBIProfilingSheet.class);
	
	public static final String DEFAULT_DESCRIPTION = "Displays ATBI sections in a tree node style - grouped per action - with apperance/CPU/memory figures.\n"
												+ "Permits to focus on ATBI of interest and enrich the profiles.";
	
	public static final String TYPE = "atbi_profiling";	
	
	private static final String JZRR_ATBI = "atbi";
	private static final String JZRR_STACK_COUNT_THRESHOLD = "stack_count_threshold";
	private static final String JZRR_SECTION_SIZE_THRESHOLD = "section_size_threshold";
	
	private int stackCountThreshold = 4;
	private int sectionSizeThreshold = 4;
	
	public ConfigATBIProfilingSheet(Element configNode, int index) {
		super(configNode, index);
		
		Element displayNode = ConfigUtil.getFirstChildNode(configNode, JZRR_DISPLAY);
		if(displayNode == null)
			return;
		Element atbiNode = ConfigUtil.getFirstChildNode(displayNode, JZRR_ATBI);
		if(atbiNode == null)
			return;		
		this.stackCountThreshold = loadValue(atbiNode, JZRR_STACK_COUNT_THRESHOLD, 4);
		this.sectionSizeThreshold = loadValue(atbiNode, JZRR_SECTION_SIZE_THRESHOLD, 4);
	}
	
	private int loadValue(Element atbiNode, String name, int defaultValue) {
		Integer value;
		String attribute = ConfigUtil.getAttributeValue(atbiNode, name);
		
		value = Ints.tryParse(attribute);
		if (value == null){
			logger.warn("Failed to load ATBI attribute " + name + " with value : " + attribute 
					+ ". Defaulting to value : " + defaultValue);
			return defaultValue;
		}
		
		if (value <= 0){
			logger.warn("Invalid ATBI attribute " + name + " value : " + attribute 
					+ ". Defaulting to value : " + defaultValue);
			return defaultValue;
		}
		
		return value;
	}

	public int getStackCountThreshold() {
		return stackCountThreshold;
	}

	public int getSectionSizeThreshold() {
		return sectionSizeThreshold;
	}

	@Override
	public String getDefaultDescription() {
		return DEFAULT_DESCRIPTION;
	}	
	
}
