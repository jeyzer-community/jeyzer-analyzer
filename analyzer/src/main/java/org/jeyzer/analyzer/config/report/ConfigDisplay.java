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


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jeyzer.analyzer.config.ConfigUtil;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

public class ConfigDisplay {

	private static final String DELTA = "delta";
	
	private Map<String, Object> fields = new HashMap<>();
	private ConfigHighlights highlights; // can be null
	private ConfigDisplay cfgDelta; // can be null
	private String name;
	
	public ConfigDisplay(Element configNode){
		this.name = configNode.getTagName();
		
		NamedNodeMap attributes = configNode.getAttributes();
		
		for (int i=0; i<attributes.getLength(); i++){
			Attr attr = (Attr)attributes.item(i);
			this.fields.put(attr.getName(), ConfigUtil.resolveValue(attr.getNodeValue()));
		}
		
		if (ConfigHighlights.hasHighlights(configNode))
			highlights = new ConfigHighlights(configNode); 
		
		Element deltaNode = ConfigUtil.getFirstChildNode(configNode, DELTA);
		if (deltaNode != null)
			this.cfgDelta = new ConfigDisplay(deltaNode);
	}
	
	public Object getValue(String field){
		return this.fields.get(field);
	}
	
	public Object getValue(String field, Object defaultValue){
		Object value = this.fields.get(field);
		return value != null ? value : defaultValue;
	}
	
	public Map<String, Object> getFields(){
		return this.fields;
	}	

	public List<ConfigDisplay> getHighlights(){
		if (this.highlights == null)
			return null;
		return this.highlights.getHighlights();
	}
	
	public ConfigDisplay getDelta() {
		return cfgDelta; // can be null
	}	
	
	public String getName() {
		return name;
	}

}
