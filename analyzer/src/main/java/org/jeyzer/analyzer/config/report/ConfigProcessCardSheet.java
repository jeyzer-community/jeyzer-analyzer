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







import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.jeyzer.analyzer.config.ConfigUtil;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ConfigProcessCardSheet extends ConfigSheet {

	public static final String TYPE = "process_card";
	
	public static final String DEFAULT_DESCRIPTION = "Displays and groups all the process properties per category : OS, architecture, startup parameters...\n"
			+ "Permits to collect technical details helping in the troubleshooting.";
	
	private static final String JZRR_CATEGORY = "category";
	private static final String JZRR_COLOR = "color";
	private static final String JZRR_FIELD = "field";
	private static final String JZRR_PROPERTY = "property";
	private static final String JZRR_PROPERTY_PATTERN = "property_pattern";
	
	private List<ConfigCategory> categories = new ArrayList<>(10); 
	
	public ConfigProcessCardSheet(Element configNode, int index) {
		super(configNode, index);
		loadCategories(configNode);
	}

	public List<ConfigCategory> getCategories() {
		return categories;
	}

	private void loadCategories(Element configNode) {
		NodeList categoryNodes = configNode.getElementsByTagName(JZRR_CATEGORY);
		if(categoryNodes != null){
			for (int i=0; i<categoryNodes.getLength(); i++){
				Element categoryNode = (Element)categoryNodes.item(i);
				ConfigCategory category = new ConfigCategory(categoryNode); 
				categories.add(category);
			}
		}
	}

	public static class ConfigCategory {
		
		private String name;
		private String color;
		private Map<Object, String> fields  = new LinkedHashMap<>(5);
		
		public ConfigCategory(Element categoryNode){
			this.name = ConfigUtil.getAttributeValue(categoryNode,JZRR_NAME);
			this.color = ConfigUtil.getAttributeValue(categoryNode,JZRR_COLOR);
			loadFields(categoryNode);
		}
		
		public String getName() {
			return name;
		}

		public String getColor() {
			return color;
		}

		public Map<Object, String> getFields() {
			return fields;
		}
		
		private void loadFields(Element categoryNode) {
			NodeList fieldNodes = categoryNode.getElementsByTagName(JZRR_FIELD);
			if(fieldNodes != null){
				for (int i=0; i<fieldNodes.getLength(); i++){
					Element fieldNode = (Element)fieldNodes.item(i);
					String fieldName = ConfigUtil.getAttributeValue(fieldNode,JZRR_NAME);
					String property = ConfigUtil.getAttributeValue(fieldNode,JZRR_PROPERTY);
					if (property != null && !property.isEmpty()){
						this.fields.put(property, fieldName);
					}
					else{
						// let's make it pattern
						property = ConfigUtil.getAttributeValue(fieldNode,JZRR_PROPERTY_PATTERN);
						Pattern propertyPattern = Pattern.compile(property);
						this.fields.put(propertyPattern, fieldName);
					}
				}
			}
		}
	}

	@Override
	public String getDefaultDescription() {
		return DEFAULT_DESCRIPTION;
	}
	
}
