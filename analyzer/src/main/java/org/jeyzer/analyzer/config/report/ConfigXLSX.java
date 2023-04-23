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
import java.util.List;

import org.jeyzer.analyzer.config.ConfigUtil;
import org.jeyzer.analyzer.config.report.security.ConfigSecurity;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.analyzer.setup.JzrSetupManager;
import org.jeyzer.service.location.JzrLocationResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ConfigXLSX {

	private static final Logger logger = LoggerFactory.getLogger(ConfigXLSX.class);
	
	private static final String JZRR_SHEET = "sheet";
	private static final String JZRR_TYPE = "type";
	private static final String JZRR_FILE = "sheet_config_file";
	private static final String JZRR_OUTPUT_DIR = "output_directory";
	private static final String JZRR_OUTPUT_FILE_PREFIX = "output_file_prefix";
	private static final String JZRR_THEME = "theme";
	
	private ConfigSecurity security;
	
	private List<ConfigSequenceSheet> seqSheets = new ArrayList<>();
	private List<ConfigGroupSequenceSheet> groupSeqSheets = new ArrayList<>();
	private List<ConfigMonitoringSheet> monitoringSheets = new ArrayList<>();
	private List<ConfigMonitoringSequenceSheet> monitoringSequenceSheets = new ArrayList<>();
	private List<ConfigAnalysisPatternsSheet> analysisPatternsSheets = new ArrayList<>();
	
	private ConfigNavigationMenuSheet navigationSheet = null;
	private ConfigSessionDetailsSheet sessionDetailsSheet = null;
	private ConfigActionSheet actionSheet = null;
	private ConfigTopStackSheet topStackSheet = null;
	private ConfigPrincipalHistogramSheet principalHistogramSheet = null;
	private ConfigExecutorFunctionHistogramSheet executorFunctionHistogramSheet = null;
	private ConfigExecutorHistogramSheet executorHistogramSheet = null;
	private ConfigFunctionOperationHistogramSheet functionOperationHistogramSheet = null;
	private ConfigActionDistinctHistogramSheet actionDistinctHistogramSheet = null;
	private ConfigMonitoringRulesSheet monitoringRulesSheet = null;
	private ConfigMonitoringStickersSheet monitoringStickersSheet = null;
	private ConfigActionProfilingSheet actionProfilingSheet = null;
	private ConfigATBIProfilingSheet atbiProfilingSheet = null;
	private ConfigActionDistinctProfilingSheet actionDistinctProfilingSheet = null;
	private ConfigActionDashboardSheet actionDashboardSheet = null;
	private ConfigProcessCardSheet processCardSheet = null;
	private ConfigProcessJarsSheet processJarsSheet = null;
	private ConfigProcessModulesSheet processModulesSheet = null;
	private ConfigJVMFlagsSheet jvmFlagsSheet = null;
	private ConfigAboutSheet aboutSheet = null;
	
	private List<ConfigSheet> orderedSheets = new ArrayList<>(10);
	
	private int sheetSize;
	
	private String outputDir;
	private String outputFilePrefix;
	private String theme;

	public ConfigXLSX(Element node, JzrSetupManager setupMgr, JzrLocationResolver resolver) throws JzrInitializationException{
		boolean navigationEnabled = false;
		
		outputDir = ConfigUtil.getAttributeValue(node, JZRR_OUTPUT_DIR);
		outputFilePrefix = ConfigUtil.getAttributeValue(node, JZRR_OUTPUT_FILE_PREFIX);
		theme = ConfigUtil.getAttributeValue(node, JZRR_THEME);
		if (theme == null || theme.isEmpty())
			theme = setupMgr.getDefaultTheme();
		
		security = new ConfigSecurity(node, setupMgr.getReportSecurity());
		
		NodeList displayNodes = node.getElementsByTagName(JZRR_SHEET);
		if (displayNodes.getLength() == 0) {
			logger.error("Failed to load the sheet configuration : list of sheets is empty.");
			throw new JzrInitializationException("Failed to load the sheet configuration : list of sheets is empty.");
		}
		
		sheetSize = displayNodes.getLength() + 1; // add the about sheet
		for(int i=0; i<displayNodes.getLength(); i++){
			int index = i;
			Element displayNode = (Element)displayNodes.item(i);
			
			String path = ConfigUtil.getAttributeValue(displayNode, JZRR_FILE);
			if (path!= null && !path.isEmpty()){
				// load sheet configuration file
				displayNode = loadSheetConfigurationFile(path);
			}

			String type = loadAttribute(displayNode, JZRR_TYPE);
			String name = loadAttribute(displayNode, ConfigSheet.JZRR_NAME);
			
			if (ConfigSequenceSheet.TYPE.equals(type)){
				ConfigSequenceSheet sheet = new ConfigSequenceSheet(displayNode, index);
				this.seqSheets.add(sheet);
				this.orderedSheets.add(index, sheet);
			}
			else if (ConfigGroupSequenceSheet.TYPE.equals(type)){
				ConfigGroupSequenceSheet sheet = new ConfigGroupSequenceSheet(displayNode, index);
				this.groupSeqSheets.add(sheet);
				this.orderedSheets.add(index, sheet);
			}
			else if (ConfigSessionDetailsSheet.TYPE.equals(type)){
				this.sessionDetailsSheet = new ConfigSessionDetailsSheet(displayNode, index);
				this.orderedSheets.add(index, sessionDetailsSheet);
			}
			else if (ConfigActionSheet.TYPE.equals(type)){
				this.actionSheet = new ConfigActionSheet(displayNode, index);
				this.orderedSheets.add(index, actionSheet);
			}
			else if (ConfigTopStackSheet.TYPE.equals(type)){
				this.topStackSheet = new ConfigTopStackSheet(displayNode, index);
				this.orderedSheets.add(index, topStackSheet);
			}
			else if (ConfigActionProfilingSheet.TYPE.equals(type)){
				this.actionProfilingSheet = new ConfigActionProfilingSheet(displayNode, index);
				this.orderedSheets.add(index, actionProfilingSheet);
			}
			else if (ConfigATBIProfilingSheet.TYPE.equals(type)){
				this.atbiProfilingSheet = new ConfigATBIProfilingSheet(displayNode, index);
				this.orderedSheets.add(index, atbiProfilingSheet);
			}
			else if (ConfigActionDistinctProfilingSheet.TYPE.equals(type)){
				this.actionDistinctProfilingSheet = new ConfigActionDistinctProfilingSheet(displayNode, index);
				this.orderedSheets.add(index, actionDistinctProfilingSheet);
			}
			else if (ConfigActionDashboardSheet.TYPE.equals(type)){
				this.actionDashboardSheet = new ConfigActionDashboardSheet(displayNode, index, resolver);
				this.orderedSheets.add(index, actionDashboardSheet);
			}
			else if (ConfigProcessCardSheet.TYPE.equals(type)){
				this.processCardSheet = new ConfigProcessCardSheet(displayNode, index);
				this.orderedSheets.add(index, processCardSheet);
			}
			else if (ConfigProcessJarsSheet.TYPE.equals(type)){
				this.processJarsSheet = new ConfigProcessJarsSheet(displayNode, index);
				this.orderedSheets.add(index, processJarsSheet);
			}
			else if (ConfigProcessModulesSheet.TYPE.equals(type)){
				this.processModulesSheet = new ConfigProcessModulesSheet(displayNode, index);
				this.orderedSheets.add(index, processModulesSheet);
			}
			else if (ConfigJVMFlagsSheet.TYPE.equals(type)){
				this.jvmFlagsSheet = new ConfigJVMFlagsSheet(displayNode, index);
				this.orderedSheets.add(index, jvmFlagsSheet);
			}
			else if (ConfigPrincipalHistogramSheet.TYPE.equals(type)){
				this.principalHistogramSheet = new ConfigPrincipalHistogramSheet(displayNode, index);
				this.orderedSheets.add(index, principalHistogramSheet);
			}
			else if (ConfigExecutorFunctionHistogramSheet.TYPE.equals(type)){
				this.executorFunctionHistogramSheet = new ConfigExecutorFunctionHistogramSheet(displayNode, index);
				this.orderedSheets.add(index, executorFunctionHistogramSheet);
			}
			else if (ConfigExecutorHistogramSheet.TYPE.equals(type)){
				this.executorHistogramSheet = new ConfigExecutorHistogramSheet(displayNode, index);
				this.orderedSheets.add(index, executorHistogramSheet);
			}
			else if (ConfigFunctionOperationHistogramSheet.TYPE.equals(type)){
				this.functionOperationHistogramSheet = new ConfigFunctionOperationHistogramSheet(displayNode, index);
				this.orderedSheets.add(index, functionOperationHistogramSheet);
			}
			else if (ConfigActionDistinctHistogramSheet.TYPE.equals(type)){
				this.actionDistinctHistogramSheet = new ConfigActionDistinctHistogramSheet(displayNode, index);
				this.orderedSheets.add(index, actionDistinctHistogramSheet);
			}
			else if (ConfigMonitoringSheet.TYPE.equals(type)){
				ConfigMonitoringSheet monitoringSheet = new ConfigMonitoringSheet(displayNode, index, resolver);
				this.monitoringSheets.add(monitoringSheet);
				this.orderedSheets.add(index, monitoringSheet);
			}
			else if (ConfigEventJournalSheet.TYPE.equals(type)){
				ConfigEventJournalSheet eventJournalSheet = new ConfigEventJournalSheet(displayNode, index, resolver);
				this.monitoringSheets.add(eventJournalSheet);
				this.orderedSheets.add(index, eventJournalSheet);
			}
			else if (ConfigMonitoringSequenceSheet.TYPE.equals(type)){
				ConfigMonitoringSequenceSheet monitoringSequenceSheet = new ConfigMonitoringSequenceSheet(displayNode, index, resolver);
				this.monitoringSequenceSheets.add(monitoringSequenceSheet);
				this.orderedSheets.add(index, monitoringSequenceSheet);
			}
			else if (ConfigMonitoringRulesSheet.TYPE.equals(type)){
				this.monitoringRulesSheet = new ConfigMonitoringRulesSheet(displayNode, index);
				this.orderedSheets.add(index, monitoringRulesSheet);
			}
			else if (ConfigMonitoringStickersSheet.TYPE.equals(type)){
				this.monitoringStickersSheet = new ConfigMonitoringStickersSheet(displayNode, index, resolver);
				this.orderedSheets.add(index, monitoringStickersSheet);
			}
			else if (ConfigNavigationMenuSheet.TYPE.equals(type)){
				this.navigationSheet = new ConfigNavigationMenuSheet(displayNode, index, this.orderedSheets);
				this.orderedSheets.add(index, navigationSheet);
				navigationEnabled = true;
			}
			else if (ConfigAnalysisPatternsSheet.TYPE.equals(type)){
				ConfigAnalysisPatternsSheet analysisPatternsSheet = new ConfigAnalysisPatternsSheet(displayNode, index);
				this.analysisPatternsSheets.add(analysisPatternsSheet);
				this.orderedSheets.add(index, analysisPatternsSheet);
			}
			else{
				logger.error("Failed to load the \"" + name + "\" sheet configuration : its type \"" + type + "\" is unknown.");
				throw new JzrInitializationException("Failed to load the \"" + name + "\" sheet configuration : its type \"" + type + "\" is unknown.");
			}
		}
		
		// add automatically the about sheet in last position
		this.aboutSheet = new ConfigAboutSheet(null, sheetSize-1);  // position is 0 based
		this.orderedSheets.add(sheetSize-1, aboutSheet);
		
		if (navigationEnabled){
			for (ConfigSheet sheet : orderedSheets){
				sheet.setNavigationEnabled(true);
			}
		}
	}
	
	public ConfigSecurity getSecurityConfig(){
		return security;
	}

	public List<ConfigSheet> getOrderedSheets() {
		return orderedSheets;
	}	
	
	public List<ConfigSequenceSheet> getSequenceSheets() {
		return seqSheets;
	}
	
	public List<ConfigGroupSequenceSheet> getGroupSequenceSheets() {
		return groupSeqSheets;
	}
	
	public ConfigNavigationMenuSheet getNavigationMenuSheet(){
		return this.navigationSheet;
	}
	
	public ConfigSessionDetailsSheet getSessionDetailsSheet(){
		return this.sessionDetailsSheet;
	}	
	
	public ConfigActionSheet getActionSheet(){
		return this.actionSheet;
	}
	
	public ConfigTopStackSheet getTopStackSheet(){
		return this.topStackSheet;
	}

	public ConfigActionProfilingSheet getActionProfilingSheet(){
		return this.actionProfilingSheet;
	}
	
	public ConfigATBIProfilingSheet getATBIProfilingSheet(){
		return this.atbiProfilingSheet;
	}	
	
	public ConfigActionDistinctProfilingSheet getActionDistinctProfilingSheet(){
		return this.actionDistinctProfilingSheet;
	}

	public ConfigActionDashboardSheet getActionDashboardSheet(){
		return this.actionDashboardSheet;
	}
	
	public ConfigProcessCardSheet getProcessCardSheet() {
		return processCardSheet;
	}
	
	public ConfigProcessJarsSheet getProcessJarsSheet() {
		return processJarsSheet;
	}
	
	public ConfigProcessModulesSheet getProcessModulesSheet() {
		return processModulesSheet;
	}
	
	public ConfigJVMFlagsSheet getJVMFlagsSheet() {
		return this.jvmFlagsSheet;
	}

	public ConfigPrincipalHistogramSheet getPrincipalHistogramSheet() {
		return principalHistogramSheet;
	}
	
	public ConfigExecutorFunctionHistogramSheet getExecutorFunctionHistogramSheet() {
		return executorFunctionHistogramSheet;
	}
	
	public ConfigExecutorHistogramSheet getExecutorHistogramSheet() {
		return executorHistogramSheet;
	}

	public ConfigFunctionOperationHistogramSheet getFunctionOperationHistogramSheet() {
		return functionOperationHistogramSheet;
	}
	
	public ConfigActionDistinctHistogramSheet getActionDistinctHistogramSheet() {
		return this.actionDistinctHistogramSheet;
	}
	
	public List<ConfigMonitoringSheet> getMonitoringSheets(){
		return monitoringSheets;
	}
	
	public List<ConfigMonitoringSequenceSheet> getMonitoringSequenceSheets() {
		return monitoringSequenceSheets;
	}

	public ConfigMonitoringRulesSheet getMonitoringRulesSheet() {
		return this.monitoringRulesSheet;
	}
	
	public ConfigMonitoringStickersSheet getMonitoringStickersSheet() {
		return this.monitoringStickersSheet;
	}
	
	public List<ConfigAnalysisPatternsSheet> getAnalysisPatternsSheets() {
		return analysisPatternsSheets;
	}

	public ConfigAboutSheet getAboutSheet() {
		return aboutSheet;
	}
	
	public String getOutputDirectory(){
		return this.outputDir;
	}
	
	public String getOutputFilePrefix(){
		return this.outputFilePrefix;
	}

	public String getTheme(){
		return this.theme; // can be null
	}	
	
	public int getSheetSize(){
		return this.sheetSize;
	}
	
	private Element loadSheetConfigurationFile(String path) throws JzrInitializationException {
		Document doc;
		
		try {
			doc = ConfigUtil.loadXMLFile(path);
			if (doc == null){
				logger.error("Failed to open the sheet configuration resource using path : " + path);
				throw new JzrInitializationException("Failed to open the sheet configuration resource using path : " + path);
			}
		} catch (JzrInitializationException e) {
			throw e;
		} catch (Exception e) {
			logger.error("Failed to open the sheet configuration resource using path : " + path, e);
			throw new JzrInitializationException("Failed to open the sheet configuration resource using path : " + path, e);
		}
		
		NodeList nodes = doc.getElementsByTagName(JZRR_SHEET);
		Element sheetNode = (Element)nodes.item(0);
		if (sheetNode == null){
			logger.error("Sheet configuration " + path + " is invalid.");
			throw new JzrInitializationException("Sheet configuration " + path + " is invalid.");
		}
		
		return sheetNode;
	}
	
	private String loadAttribute(Element displayNode, String field) throws JzrInitializationException {
		String value = ConfigUtil.getAttributeValue(displayNode, field);
		if (value == null || value.isEmpty()){
			logger.error("Failed to load the sheet configuration : mandatory attribute \"" + field + "\" is missing.");
			throw new JzrInitializationException("Failed to load the sheet configuration : mandatory attribute \"" + field + "\" is missing.");
		}
			
		return value;
	}
}
