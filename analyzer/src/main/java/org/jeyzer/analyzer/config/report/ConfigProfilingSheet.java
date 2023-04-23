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
import org.w3c.dom.Element;

public abstract class ConfigProfilingSheet extends ConfigGraphSheet{

	public static final String JZRR_DISPLAY = "display";
	public static final String JZRR_COLOR = "color";
	
	public static final String JZRR_JAVA_MODULES = "java_modules";
	public static final String JZRR_STRIP = "strip";
	
	private static final String JZRR_OPERATIONS = "operations";
	private static final String JZRR_FUNCTIONS = "functions";
	
	private String operationColor;
	private String functionColor;
	
	private boolean stripJavaModulePrefix = true;
	
	public ConfigProfilingSheet(Element configNode, int index){
		super(configNode, index);
		
		Element displayNode = ConfigUtil.getFirstChildNode(configNode, JZRR_DISPLAY);
		if(displayNode == null)
			return;
		
		loadJavaModules(displayNode);
		loadOperations(displayNode);
		loadFunctions(displayNode);
	}
	
	private void loadJavaModules(Element displayNode) {
		Element javaModulesNode = ConfigUtil.getFirstChildNode(displayNode, JZRR_JAVA_MODULES);
		if(javaModulesNode == null)
			return; 		// true by default
		
		String value = ConfigUtil.getAttributeValue(javaModulesNode, JZRR_STRIP);
		if (value.isEmpty())
			return;
		
		stripJavaModulePrefix = Boolean.valueOf(value);
	}

	private void loadFunctions(Element displayNode) {
		Element functionsNode = ConfigUtil.getFirstChildNode(displayNode, JZRR_FUNCTIONS);
		if(functionsNode == null)
			return;
		
		this.functionColor = ConfigUtil.getAttributeValue(functionsNode,JZRR_COLOR);
		if (this.functionColor.isEmpty())
			this.functionColor = null;
	}

	private void loadOperations(Element displayNode) {
		Element operationsNode = ConfigUtil.getFirstChildNode(displayNode, JZRR_OPERATIONS);
		if(operationsNode == null)
			return;
		
		this.operationColor = ConfigUtil.getAttributeValue(operationsNode,JZRR_COLOR);
		if (this.operationColor.isEmpty())
			this.operationColor = null;
	}
	
	public String getOperationColor() {
		return operationColor; // can be null
	}
	
	public String getFunctionColor() {
		return functionColor;  // can be null
	}
	
	public boolean isOperationDisplayed(){
		return this.operationColor != null;
	}
	
	public boolean isFunctionDisplayed(){
		return this.functionColor != null;
	}

	public boolean isJavaModuleStripped(){
		return this.stripJavaModulePrefix;
	}
}
