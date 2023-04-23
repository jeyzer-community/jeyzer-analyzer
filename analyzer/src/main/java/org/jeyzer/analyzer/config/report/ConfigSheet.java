package org.jeyzer.analyzer.config.report;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

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







import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.jeyzer.analyzer.config.ConfigUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

public abstract class ConfigSheet {
	
	protected static final Logger logger = LoggerFactory.getLogger(ConfigSheet.class);

	public static final String JZRR_NAME = "name";
	
	private static final String JZRR_DESCRIPTION = "description";
	private static final String JZRR_TAB_COLOR = "tab_color";
	private static final String JZRR_FORMATS = "formats";

	private int index;	
	
	protected String name;
	protected String description;
	private XSSFColor color;
	protected boolean navigationEnabled = false;
	protected boolean enabled = true;
	private List<String> supportedFormats; 
	
	public ConfigSheet(Element configNode, int index){
		String nameCandidate = ConfigUtil.getAttributeValue(configNode,JZRR_NAME);
		this.name = WorkbookUtil.createSafeSheetName(nameCandidate);
		if (nameCandidate.length() > 31) {
			logger.warn("Sheet name exceeds the Excel 31 chars limit. Name will be truncated.");
			logger.warn("  Configured sheet name is : {}", nameCandidate);
			logger.warn("  Final sheet name is      : {}", this.name);
		}
		
		Element descNode = ConfigUtil.getFirstChildNode(configNode, JZRR_DESCRIPTION);
		if (descNode != null && descNode.getFirstChild() != null)
			this.description = descNode.getFirstChild().getNodeValue();
		
		color = loadColor(configNode, JZRR_TAB_COLOR);
		this.index = index;
		this.supportedFormats = loadSupportedFormats(configNode);
	}

	public ConfigSheet(String name, String description, int index){
		this.name = name;
		this.description = description;
		this.index = index;
		this.supportedFormats = new ArrayList<>();
	}
	
	public String getName() {
		return name;
	}
	
	public int getIndex() {
		return index;
	}
	
	public XSSFColor getColor() {
		return color; // can be null
	}

	public String getDescription() {
		return description ==null || description.isEmpty() ? getDefaultDescription():description;
	}
	
	public abstract String getDefaultDescription();

	public void setNavigationEnabled(boolean enabled) {
		this.navigationEnabled = enabled;
	}
	
	public boolean isNavigationEnabled(){
		return this.navigationEnabled;
	}

	public void disable(){
		this.enabled = false;
	}
	
	public boolean isEnabled(){
		return this.enabled;
	}
	
	public List<String> getSupportedFormats() {
		return supportedFormats;
	}
	
	protected XSSFColor loadColor(Element configNode, String attributeName) {
		String colorValue = ConfigUtil.getAttributeValue(configNode, attributeName);
		
		if (colorValue != null && !colorValue.isEmpty()){
			try{
				return new XSSFColor(IndexedColors.valueOf(colorValue), null);
			}catch(java.lang.IllegalArgumentException e){
				logger.warn("Failed to load the color attribute value " + colorValue + " in the sheet " + this.name + ". Value is invalid."); 
				return null;  // if invalid value, disable it
			}
		}
		
		return null; // none
	}
	
	private List<String> loadSupportedFormats(Element configNode) {
		List<String> formats = new ArrayList<>();
		
		String concatenatedformats = ConfigUtil.getAttributeValue(configNode,JZRR_FORMATS);
		if (concatenatedformats != null){
			StringTokenizer tokenizer = new StringTokenizer(concatenatedformats, ",");
			while (tokenizer.hasMoreTokens()){
				formats.add(tokenizer.nextToken());
			}
		}
		return formats;
	}
}
