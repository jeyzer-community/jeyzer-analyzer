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





import org.w3c.dom.Element;
import org.jeyzer.analyzer.config.ConfigUtil;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class ConfigDeobfuscatorPlugin {

	private static final String JZRA_DEOBFUSCATION_PLUGIN_TYPE = "type";
	private static final String JZRA_DEOBFUSCATION_PLUGIN_ID = "id";
	private static final String JZRA_DEOBFUSCATION_FAIL_IF_CONFIG_NOT_FOUND = "fail_if_config_not_found";
	private static final String JZRA_DEOBFUSCATION_ABORT_ON_ERROR = "abort_on_error";
	
	// logger
	private static final Logger logger = LoggerFactory.getLogger(ConfigDeobfuscatorPlugin.class);	
	
	private String id;
	private String type;
	private boolean failOnconfigNotFound = true;
	private boolean abortOnError = true;
	private ConfigDeobfuscatorConfiguration configuration;
	
	public ConfigDeobfuscatorPlugin(Element pluginNode) throws JzrInitializationException {
		// deobfuscation plugin id
		this.id  = ConfigUtil.getAttributeValue(pluginNode,JZRA_DEOBFUSCATION_PLUGIN_ID);
		if (this.id  == null || this.id .isEmpty()){
			logger.error("Invalid deobfuscator plugin configuration : deobfuscation parameter {} not found.", ConfigDeobfuscatorPlugin.JZRA_DEOBFUSCATION_PLUGIN_ID);
			throw new JzrInitializationException("Invalid deobfuscator plugin configuration : deobfuscation parameter " + ConfigDeobfuscatorPlugin.JZRA_DEOBFUSCATION_PLUGIN_ID + " not found.");
		}

		// deobfuscation plugin type
		this.type = ConfigUtil.getAttributeValue(pluginNode,JZRA_DEOBFUSCATION_PLUGIN_TYPE);
		if (this.type == null || this.type.isEmpty()){
			logger.error("Invalid deobfuscator plugin configuration : deobfuscation parameter {} not found.", ConfigDeobfuscatorPlugin.JZRA_DEOBFUSCATION_PLUGIN_TYPE);
			throw new JzrInitializationException("Invalid deobfuscator plugin configuration : deobfuscation parameter " + ConfigDeobfuscatorPlugin.JZRA_DEOBFUSCATION_PLUGIN_TYPE + " not found.");
		}

		String value = ConfigUtil.getAttributeValue(pluginNode,JZRA_DEOBFUSCATION_FAIL_IF_CONFIG_NOT_FOUND);
		if (value != null && !value.isEmpty())
			failOnconfigNotFound = Boolean.parseBoolean(value);

		value = ConfigUtil.getAttributeValue(pluginNode,JZRA_DEOBFUSCATION_ABORT_ON_ERROR);
		if (value != null && !value.isEmpty())
			abortOnError = Boolean.parseBoolean(value);
		
		configuration = new ConfigDeobfuscatorConfiguration(pluginNode);
	}

	public String getId() {
		return id;
	}

	public String getType() {
		return type;
	}
	
	public boolean isFailOnconfigNotFound(){
		return failOnconfigNotFound; 
	}
	
	public boolean isAbortOnError(){
		return abortOnError; 
	}

	public ConfigDeobfuscatorConfiguration getDeobfuscatorConfiguration() {
		return configuration;
	}
}
