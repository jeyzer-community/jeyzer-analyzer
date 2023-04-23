package org.jeyzer.profile.master;

/*-
 * ---------------------------LICENSE_START---------------------------
 * Jeyzer Analyzer
 * --
 * Copyright (C) 2020 - 2021 Jeyzer
 * --
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 * ----------------------------LICENSE_END----------------------------
 */

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.jeyzer.analyzer.config.ConfigThreadLocal;
import org.jeyzer.analyzer.config.ConfigUtil;
import org.jeyzer.analyzer.config.analysis.ConfigFilePattern;
import org.jeyzer.analyzer.config.analysis.ConfigProfileDiscovery;
import org.jeyzer.analyzer.data.TimeZoneInfo;
import org.jeyzer.analyzer.data.TimeZoneInfo.TimeZoneOrigin;
import org.jeyzer.analyzer.util.TimeZoneInfoHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class MasterProfileBuilder {
	
	private static final Logger logger = LoggerFactory.getLogger(MasterProfileBuilder.class);
	
	private static final String PROPERTY_JEYZER_TARGET_PROFILE = "JEYZER_TARGET_PROFILE";
	
	private static final String JZRA_ANALYSIS = "analysis";
	private static final String JZRA_APPLICATION_TYPE = "application_type";
	private static final String JZRA_STATE = "state";
	private static final String JZRA_RECORDING = "recording";
	private static final String JZRA_THREAD_DUMP_FILE_PATTERNS = "file_patterns";
	
	private static final String JZRA_DISCOVERY_MODE = "discovery_mode";
	private static final String JZRA_ENABLED = "enabled";
	private static final String JZRA_DISCOVERY = "discovery";
	private static final String JZRA_DISCOVERY_RULES = "discovery_rules";
	
	private static final String JZRR_REPORT = "report";
	private static final String JZRR_REPORT_CONFIG_FILE = "report_config_file";

	private static final String JZRA_SETUP = "setup";
	private static final String JZRA_SETUP_CONFIG_FILE = "setup_config_file";
	private static final String JZRA_DEFAULT_SETUP = "default_setup";
	private static final String JZRA_DISPLAY = "display";
	private static final String JZRA_REPORTS = "reports";
	
	private static final String JZRR_XLSX_REPORT = "xlsx_report";
	private static final String JZRR_SECURITY_FILE = "security_file";
	private static final String JZR_SECURITY = "security";
	private static final String JZR_PASSWORD = "password";
	private static final String JZR_MODE = "mode";
	
	private static final String JZR_TIME_ZONES = "time_zones";
	private static final String JZR_RECORDING_TIME_ZONE = "recording_time_zone";
	private static final String JZR_DISPLAY_TIME_ZONE = "display_time_zone";
	private static final String JZR_TIME_ZONE_ID = "id";

	private static final String JZRA_STATE_DISABLED_VALUE = "disabled";

	public MasterProfile loadProfile(String profileName, File analysisFile){
		// get profile name and security settings
		String profileType;
		JzrProfileSecurity security;
		boolean discoveryModeEnabled = false;
		List<ConfigFilePattern> filePatterns = new ArrayList<>(6);
		TimeZoneInfo defaultRecordingTimeZone;
		TimeZoneInfo defaultReportTimeZone;
		ConfigProfileDiscovery profileDiscovery;
		try {
			if (!analysisFile.exists()) {
				logger.info("Profile {} ignored : analysis configuration file not found.", profileName);
				return null;
			}
			
			Document doc = ConfigUtil.loadDOM(analysisFile);
			NodeList nodes = doc.getElementsByTagName(JZRA_ANALYSIS);
			Element analysisNode = (Element)nodes.item(0);
			profileType = ConfigUtil.getAttributeValue(analysisNode, JZRA_APPLICATION_TYPE);
			if (!isAnalysisProfileEnabled(ConfigUtil.getAttributeValue(analysisNode, JZRA_STATE))){
				logger.info("Profile {} is disabled by configuration : profile is ignored.", profileName);
				return null;
			}
			
			discoveryModeEnabled = loadRulesDiscovery(analysisNode);
			
			Element discoveryNode = ConfigUtil.getFirstChildNode(analysisNode, JZRA_DISCOVERY);
			profileDiscovery = new ConfigProfileDiscovery(discoveryNode);
			
			// file patterns
			Element recordingNode = ConfigUtil.getFirstChildNode(analysisNode, JZRA_RECORDING);
			Element filePatternsNode = ConfigUtil.getFirstChildNode(recordingNode, JZRA_THREAD_DUMP_FILE_PATTERNS);
			loadFilePatterns(filePatternsNode,
					ConfigFilePattern.JZRA_RECORDING_FILE_JZR_PATTERN, filePatterns); // should be unique
			loadFilePatterns(filePatternsNode,
					ConfigFilePattern.JZRA_RECORDING_FILE_TIMESTAMP_PATTERN, filePatterns);
			loadFilePatterns(filePatternsNode,
					ConfigFilePattern.JZRA_RECORDING_FILE_REGEX_PATTERN, filePatterns);
			
			defaultRecordingTimeZone = loadDefaultTimeZone(analysisNode, JZR_RECORDING_TIME_ZONE);
			defaultReportTimeZone = loadDefaultTimeZone(analysisNode, JZR_DISPLAY_TIME_ZONE);
			
			// not nice but required for path resolution. 
			// Every other environment variable (usually shared) must be shared in the environment (cf. Tomcat setenv.bat/sh)
			ConfigThreadLocal.getProperties().put(PROPERTY_JEYZER_TARGET_PROFILE, profileName);
			security = loadSecurity(analysisNode);
			if (security == null){
				logger.info("Profile {} ignored : invalid security configuration.", profileName);
				return null;
			}
			ConfigThreadLocal.getProperties().remove(PROPERTY_JEYZER_TARGET_PROFILE);
			
		} catch (Exception e) {
			logger.error("Failed to load {} profile configuration from file : {}", profileName, analysisFile.getAbsolutePath());
			return null;
		}
		
		logger.info("Profile {} loaded.", profileType);

		return new MasterProfile(
				profileType, 
				analysisFile.getAbsolutePath(), 
				discoveryModeEnabled, 
				profileDiscovery.getRedirectionPatterns(),
				filePatterns,
				security,
				defaultRecordingTimeZone, 
				defaultReportTimeZone);

	}
		
	private boolean loadRulesDiscovery(Element analysisNode) {
		Element discoveryNode = ConfigUtil.getFirstChildNode(analysisNode, JZRA_DISCOVERY);
		
		if (discoveryNode != null) {
			// Version 2.6+
			Element discoveryRulesNode = ConfigUtil.getFirstChildNode(discoveryNode, JZRA_DISCOVERY_RULES);
			if (discoveryRulesNode != null)
				return Boolean.parseBoolean(ConfigUtil.getAttributeValue(discoveryRulesNode, JZRA_ENABLED));
		}
		else {
			// Backward compatibility (2.0 to 2.5)
			Element discoveryModeNode = ConfigUtil.getFirstChildNode(analysisNode, JZRA_DISCOVERY_MODE);
			if (discoveryModeNode != null)
				return Boolean.parseBoolean(ConfigUtil.getAttributeValue(discoveryModeNode, JZRA_ENABLED));
			else
				return Boolean.FALSE;
		}
		
		return false;
	}

	private JzrProfileSecurity loadSecurity(Element analysisNode) {
		
		Element reportNode = ConfigUtil.getFirstChildNode(analysisNode, JZRR_REPORT);
		if (reportNode == null)
			return null;
		
		String reportPath = ConfigUtil.getAttributeValue(reportNode, JZRR_REPORT_CONFIG_FILE);
		if (reportPath == null || reportPath.isEmpty())
			return null;
		
		Document doc = ConfigUtil.loadXMLFile(reportPath);
		if (doc == null)
			return null;
		
		NodeList reportNodes = doc.getElementsByTagName(JZRR_REPORT);
		if (reportNodes == null || reportNodes.getLength() == 0)
			return null;
		
		reportNode = (Element) reportNodes.item(0);
		Element reportXlsxNode = ConfigUtil.getFirstChildNode(reportNode, JZRR_XLSX_REPORT);
		if (reportXlsxNode == null)
			return null;
		
		String securityPath = ConfigUtil.getAttributeValue(reportXlsxNode, JZRR_SECURITY_FILE);
		if (securityPath == null || securityPath.isEmpty()){
			// not specified, let's load the default one
			return loadDefaultSetupSecurity(analysisNode);
		}

		return loadProfileSecurity(securityPath);
	}
	
	private JzrProfileSecurity loadDefaultSetupSecurity(Element analysisNode) {
		Element setupNode = ConfigUtil.getFirstChildNode(analysisNode, JZRA_SETUP);
		if (setupNode == null)
			return null;
		
		String setupPath = ConfigUtil.getAttributeValue(setupNode, JZRA_SETUP_CONFIG_FILE);
		if (setupPath == null || setupPath.isEmpty())
			return null;
		
		Document doc = ConfigUtil.loadXMLFile(setupPath);
		if (doc == null)
			return null;
	
		NodeList defaultSetupNodes = doc.getElementsByTagName(JZRA_DEFAULT_SETUP);
		if (defaultSetupNodes == null || defaultSetupNodes.getLength() == 0)
			return null;
		
		Element defaultSetupNode = (Element) defaultSetupNodes.item(0);
		Element displayNode = ConfigUtil.getFirstChildNode(defaultSetupNode, JZRA_DISPLAY);
		if (displayNode == null)
			return null;
	
		Element reportsNode = ConfigUtil.getFirstChildNode(displayNode, JZRA_REPORTS);
		if (reportsNode == null)
			return null;
		
		Element reportXlsxNode = ConfigUtil.getFirstChildNode(reportsNode, JZRR_XLSX_REPORT);
		if (reportXlsxNode == null)
			return null;
		
		String securityPath = ConfigUtil.getAttributeValue(reportXlsxNode, JZRR_SECURITY_FILE);
		if (securityPath == null || securityPath.isEmpty())
			return null;
		
		return loadProfileSecurity(securityPath);
	}
	
	private void loadFilePatterns(Element filePatternsNode, String type, List<ConfigFilePattern> filePatterns) {
		NodeList filePatternNodes = filePatternsNode.getElementsByTagName(type);
		for (int i = 0; i < filePatternNodes.getLength(); i++) {
			Element filePatternNode = (Element) filePatternNodes.item(i);

			// file pattern
			String pattern = ConfigUtil.getAttributeValue(filePatternNode,
					ConfigFilePattern.JZRA_RECORDING_PATTERN);

			// can be null
			String value = ConfigUtil
					.getAttributeValue(
							filePatternNode,
							ConfigFilePattern.JZRA_RECORDING_FILE_TIMESTAMP_PATTERN_IGNORE_SUFFIX);
			boolean ignoreSuffix = (value != null && Boolean
					.parseBoolean(value)) ? Boolean.TRUE : Boolean.FALSE;

			ConfigFilePattern filePattern = new ConfigFilePattern(type,
					pattern, ignoreSuffix);

			filePatterns.add(filePattern);
		}
	}
	
	private TimeZoneInfo loadDefaultTimeZone(Element analysisNode, String typeField) {
		TimeZoneInfo timeZoneInfo = new TimeZoneInfo();
		
		// Time zone id
		Element timeZonesNode = ConfigUtil.getFirstChildNode(analysisNode, JZR_TIME_ZONES);
		if (timeZonesNode == null)
			return timeZoneInfo;
		Element timeZoneTypeNode = ConfigUtil.getFirstChildNode(timeZonesNode, typeField);
		if (timeZoneTypeNode == null)
			return timeZoneInfo;
		String id = timeZoneTypeNode.getAttribute(JZR_TIME_ZONE_ID);
		if (ConfigUtil.isVariableUnresolved(id))
			return timeZoneInfo;
		
		// validate the time zone id
		if (!TimeZoneInfoHelper.isValidTimeZone(id))
			return timeZoneInfo;
		
		return new TimeZoneInfo(TimeZoneOrigin.PROFILE, id);
	}
	
	private JzrProfileSecurity loadProfileSecurity(String securityPath) {
		Document doc = ConfigUtil.loadXMLFile(securityPath);
		if (doc == null)
			return null;
		
		NodeList securityNodes = doc.getElementsByTagName(JZR_SECURITY);
		if (securityNodes == null || securityNodes.getLength() == 0)
			return null;
		
		Element securityNode = (Element) securityNodes.item(0);
		Element passwordNode = ConfigUtil.getFirstChildNode(securityNode, JZR_PASSWORD);
		if (passwordNode == null)
			return null;
		
		String modeValue = ConfigUtil.getAttributeValue(passwordNode, JZR_MODE);
		try{
			SecMode mode = SecMode.valueOf(modeValue.toUpperCase());
			return new JzrProfileSecurity(mode);
		}catch(IllegalArgumentException ex){
			return null;
		}
	}
	
	private boolean isAnalysisProfileEnabled(String state) {
		if (state.isEmpty())
			// missing attribute, consider as valid (draft)
			return true;
		
		return !JZRA_STATE_DISABLED_VALUE.equalsIgnoreCase(state.trim());
	}
}
