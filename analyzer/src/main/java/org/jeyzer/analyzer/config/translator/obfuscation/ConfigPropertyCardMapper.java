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
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.jeyzer.analyzer.config.ConfigUtil;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ConfigPropertyCardMapper {

	// logger
	private static final Logger logger = LoggerFactory.getLogger(ConfigPropertyCardMapper.class);

	public static final String JZRA_PROPERTY_CARD_MAPPERS = "property_card_mappers";
	public static final String JZRA_PROPERTY_CARD_MAPPER_SET = "property_card_mapper_set";
	
	public static final String JZRA_PROPERTY_CARD_MAPPER = "property_card_mapper";

	private static final String JZRA_PROPERTY_CARD_MAPPER_NAME = "name";
	private static final String JZRA_PROPERTY_CARD_MAPPER_SOURCE_PROPERTY = "source_property";
	private static final String JZRA_PROPERTY_CARD_MAPPER_PROPERTIES = "properties";
	
	private String name;
	private Pattern propertySourceNamePattern;
	private List<PropertyMapping> propertyMappings = new ArrayList<>();
	
	public ConfigPropertyCardMapper(Element mapperNode) throws JzrInitializationException{
		// name
		this.name  = ConfigUtil.getAttributeValue(mapperNode,JZRA_PROPERTY_CARD_MAPPER_NAME);
		if (this.name  == null || this.name.isEmpty()){
			logger.error("Invalid property card mapper configuration : parameter {} not found.", JZRA_PROPERTY_CARD_MAPPER_NAME);
			throw new JzrInitializationException("Invalid property card mapper configuration : parameter " + JZRA_PROPERTY_CARD_MAPPER_NAME + " not found.");
		}
		
		// property source name
		String value  = ConfigUtil.getAttributeValue(mapperNode,JZRA_PROPERTY_CARD_MAPPER_SOURCE_PROPERTY);
		if (value  == null || value .isEmpty()){
			logger.error("Invalid property card mapper configuration : parameter {} not found.", JZRA_PROPERTY_CARD_MAPPER_SOURCE_PROPERTY);
			throw new JzrInitializationException("Invalid property card mapper configuration : parameter " + JZRA_PROPERTY_CARD_MAPPER_SOURCE_PROPERTY + " not found.");
		}
		try {
			this.propertySourceNamePattern = Pattern.compile(value);
		} catch (PatternSyntaxException ex) {
			logger.error("Invalid property card mapper configuration : property pattern " + value + " is invalid.");
			throw new JzrInitializationException("Invalid property card mapper configuration : parameter " + value + " is invalid regular expression.");
		}
		
		loadTargetProperties(mapperNode);
	}

	public Pattern getPropertySourceNamePattern() {
		return propertySourceNamePattern;
	}

	public List<PropertyMapping> getPropertyMappings() {
		return propertyMappings;
	}

	public String getName() {
		return name;
	}

	private void loadTargetProperties(Element mapperNode) throws JzrInitializationException {
		Element propertiesNode = ConfigUtil.getFirstChildNode(mapperNode, JZRA_PROPERTY_CARD_MAPPER_PROPERTIES);
		if (propertiesNode == null)
			throw new JzrInitializationException("Invalid property card mapper configuration : properties section is missing.");
		
		NodeList propertyNodes = propertiesNode.getElementsByTagName(PropertyMapping.JZRA_PROPERTY);
		if (propertyNodes == null || propertyNodes.getLength() == 0)
			throw new JzrInitializationException("Invalid property card mapper configuration : property mappings are missing.");
		
		for (int i=0; i<propertyNodes.getLength(); i++){
			Element propertyNode = (Element)propertyNodes.item(i);
			PropertyMapping propertyMapping = new PropertyMapping(propertyNode);
			this.propertyMappings.add(propertyMapping);
		}
	}
	
	public static final class PropertyMapping{

		public static final String JZRA_PROPERTY = "property";
		
		private static final String JZRA_PROPERTY_NAME = "name";
		private static final String JZRA_PROPERTY_PATTERN = "pattern";
		private static final String JZRA_PROPERTY_SCOPE = "scope";
		
		private static final String PROPERTY_SCOPE_NAME  = "property-name";
		private static final String PROPERTY_SCOPE_VALUE = "property-value";
		
		private String name;
		private Pattern pattern;
		private String scope;
		
		public PropertyMapping(Element propertyNode) throws JzrInitializationException {
			// name
			this.name  = ConfigUtil.getAttributeValue(propertyNode,JZRA_PROPERTY_NAME);
			if (this.name  == null || this.name.isEmpty()){
				logger.error("Invalid property card mapper configuration : property parameter {} not found.", JZRA_PROPERTY_NAME);
				throw new JzrInitializationException("Invalid property card mapper configuration : property parameter " + JZRA_PROPERTY_NAME + " not found.");
			}
			
			// pattern
			String value = ConfigUtil.getAttributeValue(propertyNode,JZRA_PROPERTY_PATTERN);
			if (value  == null || value.isEmpty()){
				logger.error("Invalid property card mapper configuration : property parameter {} not found.", JZRA_PROPERTY_PATTERN);
				throw new JzrInitializationException("Invalid property card mapper configuration : property parameter " + JZRA_PROPERTY_PATTERN + " not found.");
			}
			try {
				this.pattern = Pattern.compile(value);
			} catch (PatternSyntaxException ex) {
				logger.error("Invalid property card mapper configuration : property pattern " + value + " is invalid.");
				throw new JzrInitializationException("Invalid property card mapper configuration : property pattern " + value + " is invalid.");
			}
			
			// scope
			this.scope = ConfigUtil.getAttributeValue(propertyNode,JZRA_PROPERTY_SCOPE);
			if (this.scope  == null || this.scope.isEmpty()){
				logger.error("Invalid property card mapper configuration : property parameter {} not found.", JZRA_PROPERTY_SCOPE);
				throw new JzrInitializationException("Invalid property card mapper configuration : property parameter " + JZRA_PROPERTY_SCOPE + " not found.");
			}
			if (!(PROPERTY_SCOPE_NAME.equals(this.scope) || PROPERTY_SCOPE_VALUE.equals(this.scope))){
				logger.error("Invalid property card mapper configuration : property scope " + this.scope + " is not valid value.");
				throw new JzrInitializationException("Invalid property card mapper configuration : property scope " + this.scope + " is not valid value.");
			}
		}

		public String getName() {
			return name;
		}

		public Pattern getPattern() {
			return pattern;
		}

		public boolean isValueScope() {
			return PROPERTY_SCOPE_VALUE.equals(scope);
		}
		
	}
}
