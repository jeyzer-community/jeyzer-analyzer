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
import java.util.List;

import org.jeyzer.analyzer.config.ConfigUtil;
import org.jeyzer.analyzer.config.translator.ConfigTranslator;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ConfigDeobfuscation extends ConfigTranslator{

	public static final String TYPE_NAME = "obfuscation";	
	
	// deobfuscation nodes
	public static final String JZRA_DEOBFUSCATION_NODE = "deobfuscation";
	
	private static final String JZRA_DEOBFUSCATION_ENABLED_NODE = "enabled";
	private static final String JZRA_DEOBFUSCATION_DIRECTORY_NODE = "directory";
	private static final String JZRA_DEOBFUSCATION_KEEP_FILES = "keep_files";
	private static final String JZRA_DEOBFUSCATION_PLUGINS = "plugins";
	
	private static final String JZRA_DEOBFUSCATION_PLUGIN_SET = "plugin_set";
	private static final String JZRA_DEOBFUSCATION_PLUGIN = "plugin";	

	private static final String JZRA_CONFIG_SET_FILE = "config_file";
	
	private static final String JZRA_DEOBFUSCATION_ENABLED = JZRA_DEOBFUSCATION_NODE + "_" + JZRA_DEOBFUSCATION_ENABLED_NODE;
	private static final String JZRA_DEOBFUSCATION_DIRECTORY = JZRA_DEOBFUSCATION_NODE + "_" + JZRA_DEOBFUSCATION_DIRECTORY_NODE;
	
	// logger
	private static final Logger logger = LoggerFactory.getLogger(ConfigDeobfuscation.class);
	
	private List<ConfigDeobfuscatorPlugin> deobsPluginConfigurations = new ArrayList<>(2);
	private List<ConfigPropertyCardMapper> propertyMapperConfigurations = new ArrayList<>(2);
	
	public ConfigDeobfuscation(Element translatorNode, String configFilePath, String threadDumpDirectory) throws JzrInitializationException{
		super(TYPE_NAME, configFilePath, false); // abort on failure set at plugin config level
		if (translatorNode == null)
			return;
		
		Element deobfuscationNode = ConfigUtil.getFirstChildNode(translatorNode, JZRA_DEOBFUSCATION_NODE);
		if (deobfuscationNode == null)
			throw new JzrInitializationException("Invalid obfuscation file " + this.configFilePath + ". Obfuscation node not found.");
		
		this.enabled = loadDeobfuscationRequired(deobfuscationNode);
		this.keepTranslatedFiles = loadDeobfuscatedFilesKept(deobfuscationNode);
		this.outputDir = loadDeobfuscationDirectoryPath(deobfuscationNode, threadDumpDirectory);

		// deobfuscation plugins
		loadDeobfuscationPlugins(deobfuscationNode);
		
		// property mapper plugins
		loadPropertyMappers(deobfuscationNode);
	}

	public List<ConfigDeobfuscatorPlugin> getDeobfuscatorPluginConfigurations(){
		return this.deobsPluginConfigurations;
	}	
	
	public List<ConfigPropertyCardMapper> getPropertyMapperConfigurations() {
		return propertyMapperConfigurations;
	}
	
	private boolean loadDeobfuscationRequired(Element deobfuscationNode) {
		String value;
		boolean enabled;

		value = ConfigUtil.getAttributeValue(deobfuscationNode,JZRA_DEOBFUSCATION_ENABLED_NODE);
		
		if (value == null || value.isEmpty()){
			logger.error("Deobfuscation parameter {} not found. Deobfuscation is disabled.", JZRA_DEOBFUSCATION_ENABLED);
			return false;
		}
		
		try{
			enabled = Boolean.valueOf(value);			
		}catch(Exception ex){
			logger.error("Deobfuscation parameter {} value {} is invalid. Deobfuscation is disabled.", JZRA_DEOBFUSCATION_ENABLED, value);
			return false;
		}
		
		return enabled;
	}		

	public boolean loadDeobfuscatedFilesKept(Element deobfuscationNode) {
		String value;
		boolean filesKept;

		value = ConfigUtil.getAttributeValue(deobfuscationNode, JZRA_DEOBFUSCATION_KEEP_FILES);
		
		if (value == null || value.isEmpty()){
			logger.error("Deobfuscation parameter {} not found. Deobfuscation file keeping is disabled.", JZRA_DEOBFUSCATION_KEEP_FILES);
			return false;			
		}
		
		try{
			filesKept = Boolean.valueOf(value);			
		}catch(Exception ex){
			logger.error("Deobfuscation parameter {} value {} is invalid. Deobfuscation file keeping is disabled.", JZRA_DEOBFUSCATION_KEEP_FILES, value);
			return false;
		}
		
		return filesKept;
	}
	
	private String loadDeobfuscationDirectoryPath(Element deobfuscationNode, String threadDumpDirectory) throws JzrInitializationException{
		String dirPath = ConfigUtil.getAttributeValue(deobfuscationNode, JZRA_DEOBFUSCATION_DIRECTORY_NODE);

		if (dirPath == null || dirPath.isEmpty()){
			logger.error("Failed to deobfuscate : deobfuscation parameter {} not found.", JZRA_DEOBFUSCATION_DIRECTORY);
			throw new JzrInitializationException("Failed to deobfuscate : deobfuscation parameter " + JZRA_DEOBFUSCATION_DIRECTORY + " not found.");
		}
		
		dirPath = dirPath.replace('\\', '/');
		if (dirPath.equals(threadDumpDirectory.replace('\\', '/'))){
			logger.error("Failed to deobfuscate. Deobfuscation directory cannot be equal to thread dump directory : {}", dirPath);
			throw new JzrInitializationException("Failed to deobfuscate. Deobfuscation directory cannot be equal to thread dump directory : " + dirPath);
		}
		
		return dirPath;
	}
	

	private void loadDeobfuscationPlugins(Element deobfuscationNode) throws JzrInitializationException{
		Element pluginsNode = ConfigUtil.getFirstChildNode(deobfuscationNode, JZRA_DEOBFUSCATION_PLUGINS);
		if (pluginsNode == null)
			throw new JzrInitializationException("Invalid deobfuscation configuration : plugins section is missing.");			
		
		NodeList pluginSetNodes = pluginsNode.getElementsByTagName(JZRA_DEOBFUSCATION_PLUGIN_SET);
		for (int i=0; i<pluginSetNodes.getLength(); i++){
			Element pluginSetNode = (Element)pluginSetNodes.item(i);
			
			String path = ConfigUtil.getAttributeValue(pluginSetNode, JZRA_CONFIG_SET_FILE);
			if (path != null && !path.isEmpty()){
				Element innerPluginSetNode = loadSetConfigurationFile(
						path, 
						JZRA_DEOBFUSCATION_PLUGIN_SET, 
						"deobfuscation plugin set"
						);
				loadPluginSet(innerPluginSetNode);
			}
			else{
				loadPluginSet(pluginSetNode);
			}
		}
	}
	
	private void loadPluginSet(Element pluginSetNode) throws JzrInitializationException {
		NodeList pluginNodes = pluginSetNode.getElementsByTagName(JZRA_DEOBFUSCATION_PLUGIN);
		for (int i=0; i<pluginNodes.getLength(); i++){
			Element pluginNode = (Element)pluginNodes.item(i);
			ConfigDeobfuscatorPlugin deobsPluginConfig = new ConfigDeobfuscatorPlugin(pluginNode);
			this.deobsPluginConfigurations.add(deobsPluginConfig);
		}
	}

	private void loadPropertyMappers(Element deobfuscationNode) throws JzrInitializationException {
		Element propertMappersNode = ConfigUtil.getFirstChildNode(deobfuscationNode, ConfigPropertyCardMapper.JZRA_PROPERTY_CARD_MAPPERS);
		if (propertMappersNode == null)
			return; // property mappers are optional

		NodeList propertyMapperSetNodes = propertMappersNode.getElementsByTagName(ConfigPropertyCardMapper.JZRA_PROPERTY_CARD_MAPPER_SET);
		for (int i=0; i<propertyMapperSetNodes.getLength(); i++){
			Element propertyMapperSetNode = (Element)propertyMapperSetNodes.item(i);
			
			String path = ConfigUtil.getAttributeValue(propertyMapperSetNode, JZRA_CONFIG_SET_FILE);
			if (path != null && !path.isEmpty()){
				Element innerPropertyMapperSetNode = loadSetConfigurationFile(
						path,
						ConfigPropertyCardMapper.JZRA_PROPERTY_CARD_MAPPER_SET,
						"deobfuscation property mapper set"
						);
				loadPropertyMapperSet(innerPropertyMapperSetNode);
			}
			else{
				loadPropertyMapperSet(propertyMapperSetNode);
			}
		}
	}

	private void loadPropertyMapperSet(Element propertyMapperSetNode) throws JzrInitializationException {
		NodeList propertMapperNodes = propertyMapperSetNode.getElementsByTagName(ConfigPropertyCardMapper.JZRA_PROPERTY_CARD_MAPPER);
		for (int i=0; i<propertMapperNodes.getLength(); i++){
			Element propertyMapperNode = (Element)propertMapperNodes.item(i);
			ConfigPropertyCardMapper deobsPluginConfig = new ConfigPropertyCardMapper(propertyMapperNode);
			this.propertyMapperConfigurations.add(deobsPluginConfig);
		}
	}

	private Element loadSetConfigurationFile(String path, String id, String name) throws JzrInitializationException {
		Document doc;
		
		try {
			doc = ConfigUtil.loadXMLFile(path);
			if (doc == null){
				logger.error("Failed to open the " + name + " resource using path : " + path);
				throw new JzrInitializationException("Failed to open the " + name + " using path : " + path);
			}
		} catch (JzrInitializationException e) {
			throw e;
		} catch (Exception e) {
			logger.error("Failed to open the " + name + " resource using path : " + path, e);
			throw new JzrInitializationException("Failed to open the " + name + " resource using path : " + path, e);
		}
		
		NodeList nodes = doc.getElementsByTagName(id);
		Element setNode = (Element)nodes.item(0);
		if (setNode == null){
			logger.error("The " + name + " configuration " + path + " is invalid.");
			throw new JzrInitializationException("The " + name + " configuration " + path + " is invalid.");
		}
		
		return setNode;
	}
}
