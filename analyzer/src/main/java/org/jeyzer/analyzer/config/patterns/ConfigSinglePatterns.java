package org.jeyzer.analyzer.config.patterns;

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
import java.util.LinkedHashMap;
import java.util.StringTokenizer;

import org.jeyzer.analyzer.config.ConfigUtil;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.analyzer.rule.pattern.DiscoveryPattern;
import org.jeyzer.analyzer.rule.pattern.Pattern;
import org.jeyzer.analyzer.rule.pattern.PresetPattern;
import org.jeyzer.analyzer.rule.pattern.DiscoveryPattern.FOCUS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ConfigSinglePatterns implements ConfigPatterns {

	private static final Logger logger = LoggerFactory.getLogger(ConfigSinglePatterns.class);
	
	private static final String JZRA_PROFILE = "profile";
	private static final String JZRA_VERSION = "version";
	private static final String JZRA_PROFILE_NAME = "name";
	
	private static final String JZRA_PATTERNS = "patterns";
	
	private static final String JZRA_PATTERN = "pattern";
	private static final String JZRA_PATTERN_REGEX = "pattern_regex";
	private static final String JZRA_FOCUS = "focus";
	private static final String JZRA_NAME = "name";
	private static final String JZRA_TYPE = "type";
	private static final String JZRA_SIZE = "size";
	private static final String JZRA_PRIORITY = "priority";
	
	private static final String JZRA_FOCUS_CLASS_VALUE = "class";
	private static final String JZRA_FOCUS_METHOD_VALUE = "method";
	private static final String JZRA_FOCUS_BOTH_VALUE = "both";
	
	private Map<String, Object> config = new HashMap<>();
	private String name;
	private String version;
	private boolean dynamic;
	private Map<String, Integer> distinctPatternCounts = new HashMap<>();
	
	public ConfigSinglePatterns(String path, boolean dynamic) throws JzrInitializationException {
		loadConfiguration(path);
		this.dynamic = dynamic;
	}
	
	@Override
	public Object getValue(String field){
		return this.config.get(field);
	}
	
	public String getName(){
		return this.name;
	}
	
	public String getVersion(){
		return this.version;
	}
	
	public boolean isDynamic(){
		return this.dynamic;
	}
	
	public int getDistinctPatternCount(String field){
		Integer count = this.distinctPatternCounts.get(field);
		if (count==null)
			return 0;
		else 
			return count;
	}
	
	private void loadConfiguration(String path) throws JzrInitializationException {
		Document doc;
		
		try {
			doc = ConfigUtil.loadXMLFile(path);
			
			if (doc == null){
				logger.error("Failed to open the analysis patterns configuration resource path : " + path);
				throw new JzrInitializationException("Failed to load the analysis patterns configuration from ressource " + path);				
			}
			
			// profile
			NodeList nodes = doc.getElementsByTagName(JZRA_PROFILE);
			Element profileNode = (Element)nodes.item(0);
			
			// version
			version = ConfigUtil.getAttributeValue(profileNode,JZRA_VERSION);
			if (version == null || version.isEmpty())
				version = "NA";
			
			// name
			name = ConfigUtil.getAttributeValue(profileNode,JZRA_PROFILE_NAME);
			
			// patterns
			Element patternsNode = ConfigUtil.getFirstChildNode(profileNode, JZRA_PATTERNS);
			
			// pattern types
			setPatterns(patternsNode, JZRA_FUNCTION);
			setPatterns(patternsNode, JZRA_OPERATION);
			setPatterns(patternsNode, JZRA_LOCKER);
			setPatterns(patternsNode, JZRA_EXECUTOR);
			setPatterns(patternsNode, JZRA_EXECUTOR_THREAD_NAME);
			setPatterns(patternsNode, JZRA_EXCLUDE);
			setPatterns(patternsNode, JZRA_EXCLUDE_THREAD_NAME);
			
			setDiscoveryPatterns(patternsNode, JZRA_DISCOVERY_FUNCTION);
			setDiscoveryPatterns(patternsNode, JZRA_DISCOVERY_OPERATION);

		} catch (Exception ex) {
			if (ex instanceof JzrInitializationException)
				throw (JzrInitializationException)ex;
			logger.error("Failed to load the '" + name + "' analysis patterns configuration from ressource " + path, ex);
			throw new JzrInitializationException("Failed to load the '" + name + "' analysis patterns configuration from ressource " + path, ex);
		}
		
	}	
		
	private void setPatterns(Element patternsNode, String patternType) throws JzrInitializationException {
		Map<String, Pattern> patterns = new LinkedHashMap<String, Pattern>();
		config.put(patternType, patterns);
		
		NodeList patternTypesNodes = patternsNode.getElementsByTagName(patternType + "s");
		if (patternTypesNodes == null)
			return;
		
		Element patternTypeNode = (Element)patternTypesNodes.item(0);
		if (patternTypeNode == null)
			return;
		
		NodeList patternNodes = patternTypeNode.getElementsByTagName(patternType);
		if (patternNodes == null)
			return;

		distinctPatternCounts.put(patternType, patternNodes.getLength());
		for (int i=0; i<patternNodes.getLength(); i++){
			Element patternNode = (Element)patternNodes.item(i);
			
			String patternName = ConfigUtil.getAttributeValue(patternNode,JZRA_NAME);
			// Contention type optional, applies only for operations at this stage
			String type = ConfigUtil.getAttributeValue(patternNode,JZRA_TYPE);
			if (type.isEmpty())
				type = null;
			
			PresetPattern pattern = (PresetPattern)patterns.get(patternName);
			if (pattern == null){
				pattern = new PresetPattern(patternName, type, this.name);
				patterns.put(patternName, pattern);
			}		
			pattern.addPattern(ConfigUtil.getAttributeValue(patternNode,JZRA_PATTERN), JZRA_PATTERN);
			pattern.addPattern(ConfigUtil.getAttributeValue(patternNode,JZRA_PATTERN_REGEX), JZRA_PATTERN_REGEX);
			
			// size is optional on exclude and executor_thread_names patterns.
			String value = ConfigUtil.getAttributeValue(patternNode,JZRA_SIZE);
			if (value != null && !value.isEmpty()){
				long size = Long.parseLong(value);
				pattern.setSize(size);
			}
			
			// priority is optional on patterns
			if (! ConfigUtil.getAttributeValue(patternNode,JZRA_PRIORITY).isEmpty()){
				int priority = Integer.parseInt(ConfigUtil.getAttributeValue(patternNode,JZRA_PRIORITY));
				pattern.updatePriority(priority);
			}
		}
		if (logger.isDebugEnabled())
			logger.debug(" - " + patternNodes.getLength() + " " + patternType + " patterns loaded");
	}

	private void setDiscoveryPatterns(Element patternsNode, String patternType) throws JzrInitializationException {
		Map<String, Pattern> patterns = new LinkedHashMap<String, Pattern>();
		config.put(patternType, patterns);
		
		NodeList patternTypesNodes = patternsNode.getElementsByTagName(patternType + "s");
		if (patternTypesNodes == null)
			return;
		
		Element patternTypeNode = (Element)patternTypesNodes.item(0);
		if (patternTypeNode == null)
			return;
		
		NodeList patternNodes = patternTypeNode.getElementsByTagName(patternType);
		if (patternNodes == null)
			return;

		for (int i=0; i<patternNodes.getLength(); i++){
			Element patternNode = (Element)patternNodes.item(i);
			
			String discoveryNames = ConfigUtil.getAttributeValue(patternNode,JZRA_PATTERN);
			if (discoveryNames == null || discoveryNames.isEmpty()){
				// Will happen if JEYZER_ANALYZER_DISCOVERY_FUNCTIONS_<X> is not set or empty
				logger.debug("Discovery rule of type : " + patternType + " ignored. Discovery name is not set.");
				continue;
			}
			
			FOCUS focus = loadFocus(patternNode);
			if (focus == null){
				logger.warn("Invalid discovery rule of type : " + patternType + " and pattern : " + discoveryNames + ". Focus attribute value is invalid or missing.");
				return;
			}
			
			loadDiscoveryPattterns(patternNode, focus, discoveryNames, patternType, patterns);

			// priority is not considered
		}
		distinctPatternCounts.put(patternType, patterns.size());
		if (logger.isDebugEnabled())
			logger.debug(" - " + patterns.size() + " " + patternType + " patterns loaded");
	}

	private void loadDiscoveryPattterns(Element patternNode, FOCUS focus,
			String discoveryNames, String patternType, Map<String, Pattern> patterns) {
		
		StringTokenizer tokenizer = new StringTokenizer(discoveryNames, ",");
		while (tokenizer.hasMoreTokens()){
			String discoveryName = tokenizer.nextToken().trim();
			if (discoveryName == null || discoveryName.isEmpty()){
				logger.warn("Invalid discovery rule of type : " + patternType + ". Discovery name contains empty entries.");
				return;
			}
			
			DiscoveryPattern pattern = new DiscoveryPattern(
					discoveryName,
					focus,
					ConfigUtil.getAttributeValue(patternNode,JZRA_PATTERN_REGEX), // optional
					this.name); 
			patterns.put(discoveryName, pattern);
		}
	}

	private FOCUS loadFocus(Element patternNode) {
		String value = ConfigUtil.getAttributeValue(patternNode,JZRA_FOCUS);
		if (value.equals(JZRA_FOCUS_CLASS_VALUE))
			return FOCUS.CLASS;
		if (value.equals(JZRA_FOCUS_METHOD_VALUE))
			return FOCUS.METHOD;
		if (value.equals(JZRA_FOCUS_BOTH_VALUE))
			return FOCUS.BOTH;
		return null;
	}
	
}
