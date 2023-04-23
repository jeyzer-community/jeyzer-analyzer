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
import java.util.List;

import org.jeyzer.analyzer.config.ConfigUtil;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.primitives.Ints;

public class ConfigSheetHeaders extends ConfigSheetAbstractHeaders{

	private static final Logger logger = LoggerFactory.getLogger(ConfigSheetHeaders.class);
	
	private static final String JZRR_FILE = "header_config_file";
	private static final String JZRR_HEADER = "header";
	
	private static final String JZRR_MATH_FUNCTIONS = "math_functions";
	private static final String JZRR_TITLE_COLUMN_INDEX = "title_column_index";

	private static final int TITLE_COLUMN_INDEX_NOT_SET = -1;
	
	private int headerPos = TITLE_COLUMN_INDEX_NOT_SET;
	
	private List<ConfigSheetHeader> headerConfigs;
	private List<String> functions;

	public ConfigSheetHeaders(Element configNode) throws JzrInitializationException{
		super(configNode);
		headerConfigs = loadHeaders(configNode);
		headerPos = loadHeaderIndex(configNode);
		functions = loadFunctions(configNode);
	}

	private List<ConfigSheetHeader> loadHeaders(Element headersSetNode) throws JzrInitializationException {
		List<ConfigSheetHeader> headers = new ArrayList<>();
		NodeList headerNodes = headersSetNode.getChildNodes();
		for(int j=0; j<headerNodes.getLength(); j++){
			Node node = headerNodes.item(j);
			if (node.getNodeType() == Node.ELEMENT_NODE 
					&& !JZRR_MATH_FUNCTIONS.equals(((Element)node).getTagName())){
				
				Element headerNode = (Element)node;
				String path = ConfigUtil.getAttributeValue(headerNode, JZRR_FILE);
				if (path!= null && !path.isEmpty()){
					// load sheet configuration file
					headerNode = loadHeaderConfigurationFile(path);
				}
				
				ConfigSheetHeader header = new ConfigSheetHeader(headerNode);
				headers.add(header);
			}
		}
		return headers;
	}

	private List<String> loadFunctions(Element configNode) {
		functions = new ArrayList<>(); 
		
		Element functionsNodes = ConfigUtil.getFirstChildNode(configNode, JZRR_MATH_FUNCTIONS);
		if (functionsNodes == null)
			return functions;
		
		NodeList functionNodes = functionsNodes.getChildNodes();
		for(int j=0; j<functionNodes.getLength(); j++){
			Node node = functionNodes.item(j);
			if (node.getNodeType() == Node.ELEMENT_NODE){
				functions.add(node.getNodeName());
			}
		}
		
		return functions;
	}

	private int loadHeaderIndex(Element headersSetNode) {
		String indexValue = ConfigUtil.getAttributeValue(headersSetNode, JZRR_TITLE_COLUMN_INDEX);
		
		if (indexValue != null && !indexValue.isEmpty()){
			Integer index = Ints.tryParse(indexValue);
			return index != null ? index : TITLE_COLUMN_INDEX_NOT_SET; 
		}
		
		return TITLE_COLUMN_INDEX_NOT_SET;
	}
	
	public int getHeaderPos(int headerSize) {
		if (!functions.isEmpty())
			return 1; // if any function, set it on 2nd column
		if (headerPos > headerSize)
			return headerSize;
		return headerPos != TITLE_COLUMN_INDEX_NOT_SET ? headerPos : headerSize;
	}

	public List<String> getFunctions() {
		return functions;
	}
	
	public List<ConfigSheetHeader> getHeaderConfigs() {
		return headerConfigs;
	}
	
	private Element loadHeaderConfigurationFile(String path) throws JzrInitializationException {
		Document doc;
		
		try {
			doc = ConfigUtil.loadXMLFile(path);
			if (doc == null){
				logger.error("Failed to open the header configuration resource using path : " + path);
				throw new JzrInitializationException("Failed to open the header configuration resource using path : " + path);
			}
		} catch (JzrInitializationException e) {
			throw e;
		} catch (Exception e) {
			logger.error("Failed to open the header configuration resource using path : " + path, e);
			throw new JzrInitializationException("Failed to open the header configuration resource using path : " + path, e);
		}
		
		NodeList nodes = doc.getElementsByTagName(JZRR_HEADER);
		Element headerNode = (Element)nodes.item(0);
		if (headerNode == null){
			logger.error("Header configuration " + path + " is invalid. Header node is missing.");
			throw new JzrInitializationException("Header configuration " + path + " is invalid. Header node is missing.");
		}
		
		nodes = headerNode.getChildNodes();
		for(int j=0; j<nodes.getLength(); j++){
			Node node = nodes.item(j);
			if (node.getNodeType() == Node.ELEMENT_NODE){
				return (Element)node; 
			}
		}
		logger.error("Header configuration " + path + " is invalid. Header node is missing.");
		throw new JzrInitializationException("Header configuration " + path + " is invalid. Header node is missing.");
	}
}
