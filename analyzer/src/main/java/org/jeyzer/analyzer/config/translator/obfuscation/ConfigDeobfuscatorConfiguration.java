package org.jeyzer.analyzer.config.translator.obfuscation;

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

import org.jeyzer.analyzer.config.ConfigUtil;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ConfigDeobfuscatorConfiguration {

	// logger
	private static final Logger logger = LoggerFactory.getLogger(ConfigDeobfuscatorConfiguration.class);	
	
	private static final String JZRA_DEOBFUSCATION_PLUGIN_CONFIGURATIONS = "configurations";
	private static final String JZRA_DEOBFUSCATION_PLUGIN_CONFIG = "config";
	
	private static final String JZRA_DEOBFUSCATION_PLUGIN_CONFIG_FILE = "file";
	private static final String JZRA_DEOBFUSCATION_PLUGIN_CONFIG_MAPPERS = "property_card_mappers";
	
	private List<String> configurationPaths = new ArrayList<>();
	private Map<String, List<String>> propertyMappers = new HashMap<>();
	
	public ConfigDeobfuscatorConfiguration(Element configNode) throws JzrInitializationException {
		loadPluginConfigurations(configNode);
	}

	private void loadPluginConfigurations(Element pluginNode) throws JzrInitializationException {
		Element configurationsNode = ConfigUtil.getFirstChildNode(pluginNode, JZRA_DEOBFUSCATION_PLUGIN_CONFIGURATIONS);
		if (configurationsNode == null)
			throw new JzrInitializationException("Invalid deobfuscator plugin configuration : configurations node is missing");

		NodeList configurationNodes = configurationsNode.getElementsByTagName(JZRA_DEOBFUSCATION_PLUGIN_CONFIG);
		for (int i=0; i<configurationNodes.getLength(); i++){
			Element configNode = (Element)configurationNodes.item(i);
			
			// config path, not resolved here
			String configurationPath = configNode.getAttribute(JZRA_DEOBFUSCATION_PLUGIN_CONFIG_FILE);
			if (configurationPath  == null || configurationPath .isEmpty()){
				logger.error("Invalid deobfuscator plugin configuration : deobfuscation parameter {} not found.", JZRA_DEOBFUSCATION_PLUGIN_CONFIG_FILE);
				throw new JzrInitializationException("Invalid deobfuscator plugin configuration : deobfuscation parameter " + JZRA_DEOBFUSCATION_PLUGIN_CONFIG_FILE + " not found.");
			}
			
			loadPluginMapperNames(configurationPath, configNode);
			configurationPaths.add(configurationPath);
		}

	}

	private void loadPluginMapperNames(String configurationPath, Element configNode) {
		// property mapper ids
		String value = ConfigUtil.getAttributeValue(configNode,JZRA_DEOBFUSCATION_PLUGIN_CONFIG_MAPPERS);
		if (!value.isEmpty()){
			String[] values = value.split(",");
			List<String> mapperIds = new ArrayList<>();
			for (String mapper : values)
				if (!mapper.isEmpty())
					mapperIds.add(mapper.trim());
			propertyMappers.put(configurationPath, mapperIds);
		}
	}

	public List<String> getConfigurationPaths() {
		return configurationPaths;
	}

	public List<String> getPropertyMappers(String configurationPath) {
		return propertyMappers.get(configurationPath);
	}
	
}
