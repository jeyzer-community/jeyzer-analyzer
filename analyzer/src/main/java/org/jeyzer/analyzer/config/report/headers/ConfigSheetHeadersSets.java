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




import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.jeyzer.analyzer.config.ConfigUtil;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ConfigSheetHeadersSets {
	
	private static final Logger logger = LoggerFactory.getLogger(ConfigSheetHeadersSets.class);

	private static final String JZRR_FORMATS = "formats";	
	
	private static final String DEFAULT_SET = "default";
	
	private final Map<String,ConfigSheetHeaders> headerConfigSets = new HashMap<>(4);	
	
	public ConfigSheetHeadersSets(NodeList headersSetNodes) throws JzrInitializationException {
		if(headersSetNodes != null){
			for (int i=0; i<headersSetNodes.getLength(); i++){
				Element headersSetNode = (Element)headersSetNodes.item(i);
				
				// supported formats
				List<String> supportedHeaderFormats = loadSupportedFormats(headersSetNode); 
				
				// headers
				ConfigSheetHeaders headerConfigs = new ConfigSheetHeaders(headersSetNode);
				
				// format is key to get the headers...
				for (String format : supportedHeaderFormats){
					if (headerConfigSets.containsKey(format)){
						logger.warn("Sheet headers already present for format : "+ format +". Please check all the headers configurations in the current sheet.");
						continue;
					}
					headerConfigSets.put(format, headerConfigs);
				}
				
				// ...but format can be optional
				if (supportedHeaderFormats.isEmpty()){
					if (headerConfigSets.containsKey(DEFAULT_SET)){
						logger.warn("Default sheet headers already present. Please check all the headers configurations in the current sheet.");
						continue;
					}
					headerConfigSets.put(DEFAULT_SET, headerConfigs);
				}
			}
		}
	}

	public ConfigSheetHeaders getHeaderConfigs(String format) {
		ConfigSheetHeaders headerSet = headerConfigSets.get(format);
		
		if (headerSet == null)
			// return the default
			return headerConfigSets.get(DEFAULT_SET);
		
		return headerSet;
	}

	private List<String> loadSupportedFormats(Element configNode) {
		List<String> formats = new ArrayList<>();
		
		String concatenatedformats = ConfigUtil.getAttributeValue(configNode,JZRR_FORMATS);
		if (concatenatedformats != null){
			StringTokenizer tokenizer = new StringTokenizer(concatenatedformats, ",");
			while (tokenizer.hasMoreTokens()){
				String format = tokenizer.nextToken();
				if (DEFAULT_SET.equalsIgnoreCase(format))
					format = DEFAULT_SET;
				formats.add(format);
			}
		}
		return formats;
	}
	
}
