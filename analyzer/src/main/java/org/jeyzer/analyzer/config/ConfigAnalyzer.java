package org.jeyzer.analyzer.config;

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


import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.jeyzer.analyzer.config.analysis.ConfigFilePattern;
import org.jeyzer.analyzer.config.analysis.ConfigPreFilter;
import org.jeyzer.analyzer.config.analysis.ConfigProfileDiscovery;
import org.jeyzer.analyzer.config.analysis.ConfigReplay;
import org.jeyzer.analyzer.config.analysis.ConfigStackSorting;
import org.jeyzer.analyzer.config.translator.ConfigTranslator;
import org.jeyzer.analyzer.config.translator.compression.ConfigDecompression;
import org.jeyzer.analyzer.config.translator.jfr.ConfigJFRDecompression;
import org.jeyzer.analyzer.config.translator.obfuscation.ConfigDeobfuscation;
import org.jeyzer.analyzer.config.translator.security.ConfigDecryption;
import org.jeyzer.analyzer.data.location.JzrResourceLocation;
import org.jeyzer.analyzer.data.location.MultipleJzrResourceLocation;
import org.jeyzer.analyzer.data.location.SingleJzrResourceLocation;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.analyzer.util.SystemHelper;
import org.jeyzer.analyzer.util.TimeZoneInfoHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

public class ConfigAnalyzer {

	private static final Logger logger = LoggerFactory
			.getLogger(ConfigAnalyzer.class);
	
	// properties
	public static final String ANALYSIS_CONFIG_FILE = "jeyzer.analysis.config";

	private static final String JZRA_DEV_CONFIG_FILE = "config.properties";

	// analysis file nodes
	public static final String JZRA_APPLICATION_TYPE = "application_type";
	public static final String JZRA_APPLICATION_ID = "application_id";
	public static final String JZRA_DESCRIPTION = "desc";
	public static final String JZRA_ISSUER = "issuer";
	public static final String JZRA_DISCOVERY_MODE_ENABLED = "discovery_mode_enabled";
	public static final String JZRA_UFO = "ufo";
	public static final String JZRA_UFO_THREAD_STACK_GENERATION_ENABLED = "generation_enabled";
	public static final String JZRA_UFO_DIRECTORY = "zip_output_dir";
	public static final String JZRA_UFO_ORDER_BY = "order_by";

	public static final String JZRA_ANALYSIS = "analysis";
	private static final String JZRA_STATE = "state";
	private static final String JZRA_CONTEXT = "context";
	
	private static final String JZRA_DISCOVERY_MODE = "discovery_mode";
	private static final String JZRA_ENABLED = "enabled";
	private static final String JZRA_DISCOVERY = "discovery";
	private static final String JZRA_DISCOVERY_RULES = "discovery_rules";

	private static final String JZRA_DEPENDENCIES = "dependencies";
	private static final String JZRA_SET = "set";
	private static final String JZRA_ID = "id";
	private static final String JZRA_PROFILES = "profiles";
	private static final String JZRA_PATTERNS = "patterns";
	private static final String JZRA_PATTERN_SET = "pattern_set";
	private static final String JZRA_PATTERN_SETS = "pattern_sets";
	private static final String JZRA_FILE = "file";
	private static final String JZRA_FILES = "files";
	private static final String JZRA_DYNAMIC_PATTERN_SETS = "dynamic_pattern_sets";

	public static final String JZRA_REPORT_CONFIG_FILE = "report_config_file";
	private static final String JZRA_REPORT = "report";
	private static final String JZRA_NOTIFICATION = "notification";
	private static final String JZRA_NOTIFICATION_THRESHOLD = "threshold";
	public static final String JZRA_NOTIFICATION_MONITORING_EVENT_THRESHOLD = "monitoring_event_threshold";

	private static final String JZRA_RECORDING = "recording";
	public static final String JZRA_JEYZER_RECORD_DIRECTORY = "directory";
	public static final String JZRA_JEYZER_RECORD_FILE = "file";

	private static final String JZRA_TRANSLATORS = "translators";
	private static final String JZRA_TRANSLATOR = "translator";
	private static final String JZRA_TYPE = "type";
	private static final String JZRA_TRANSLATOR_CONFIG_FILE = "translator_config_file";

	private static final String JZRA_THREAD_DUMP_FILE_PATTERNS = "file_patterns";
	private static final String JZRA_THREAD_STACK = "thread_stack";
	private static final String JZRA_THREAD_STACK_IGNORED = "ignored_stacks";
	public static final String JZRA_THREAD_STACK_GENERATION_ENABLED = "generation_enabled";
	public static final String JZRA_THREAD_STACK_OUTPUT_DIR = "zip_output_dir";

	private static final String JZRA_SETUP = "setup";
	private static final String JZRA_REPLAY = "replay";

	private static final String JZRA_TIME_ZONES = "time_zones";
	public static final String JZRA_RECORDING_TIME_ZONE = "recording_time_zone";
	public static final String JZRA_DISPLAY_TIME_ZONE = "display_time_zone";
	private static final String JZRA_TIME_ZONE_ID = "id";
	
	private static final String JZRA_RECORDING_TIME_ZONE_VAR = "${JEYZER_RECORDING_TIME_ZONE_ID}";
	private static final String JZRA_DISPLAY_TIME_ZONE_VAR = "${JEYZER_DISPLAY_TIME_ZONE_ID}";

	private static final String JZRA_RECORDING_TIME_ZONE_USER_SELECTED_VAR = "${JEYZER_RECORDING_TIME_ZONE_ID_USER_SELECTED}";
	public static final String JZRA_RECORDING_TIME_ZONE_USER_SPECIFIED = "recording_time_zone_user_specified";
	private static final String JZRA_DISPLAY_TIME_ZONE_USER_SELECTED_VAR = "${JEYZER_DISPLAY_TIME_ZONE_ID_USER_SELECTED}";
	public static final String JZRA_DISPLAY_TIME_ZONE_USER_SPECIFIED = "display_time_zone_user_specified";
	
	public static final String JZRA_SETUP_CONFIG_FILE = "setup_config_file";

	private static final String JZRA_THREAD_DUMP_PERIOD = "period";
	
	// Manifest version info
	public static final String JEYZER_ANALYZER_VERSION = "jeyzer.analyzer.version";
	public static final String JEYZER_ANALYZER_LABEL = "Jeyzer Analyzer version";

	// Internal properties
	private static final String PROPERTY_JEYZER_ANALYZER_DISCOVERY_FUNCTIONS_PREFIX = "JEYZER_ANALYZER_DISCOVERY_FUNCTIONS_";
	private static final String PROPERTY_BUILT_JEYZER_ANALYZER_DISCOVERY_FUNCTIONS_REGEX_PREFIX = "BUILT_JEYZER_ANALYZER_DISCOVERY_FUNCTIONS_REGEX_";
	public static final String INVALID_DISCOVERY_REGEX_FIELD = ".*.*";
	public static final String DISABLED_DISCOVERY_REGEX_FIELD = "^ABCDEFGHIJKLMNOPZYX"; // No function like that exists for sure..

	private Properties config;

	private List<ConfigFilePattern> filePatterns = new ArrayList<>(6);
	private List<JzrResourceLocation> patternLocations = new ArrayList<>(3);
	private Multimap<String, String> dependencies = LinkedHashMultimap.create();
	
	private ConfigDynamicLoading dynamicPatternsLoadingCfg;

	private List<ConfigTranslator> translators = new ArrayList<>(4);
	private List<String> translatorInputFileExtensions = new ArrayList<>(3);

	private ConfigMail mailCfg;

	private ConfigReplay replayCfg = null;
	
	private ConfigPreFilter preFilterCfg;

	private Duration threadDumpPeriod;
	
	private ConfigState state  = ConfigState.DISABLED;
	
	private ConfigStackSorting sortingCfg;
	
	private ConfigProfileDiscovery profileDiscoveryCfg;

	public ConfigAnalyzer() throws JzrInitializationException {

		String jzrFilePath = ConfigThreadLocal.get(ANALYSIS_CONFIG_FILE);
		if (jzrFilePath == null)
			jzrFilePath = System.getProperty(ANALYSIS_CONFIG_FILE);

		config = ConfigUtil.loadDevVariables(jzrFilePath, JZRA_DEV_CONFIG_FILE);

		if (config == null) {
			// production environment
			config = new Properties();
			config.setProperty(ANALYSIS_CONFIG_FILE, jzrFilePath);
		}

		// set the jzrFile path
		jzrFilePath = config.getProperty(ANALYSIS_CONFIG_FILE);

		// resolve any variable or system property (ex: ${user.dir})
		jzrFilePath = ConfigUtil.resolveValue(jzrFilePath);
		logger.info("Loading Analysis config file : "
				+ SystemHelper.sanitizePathSeparators(jzrFilePath));

		// test that file exists
		File jzrFile = new File(jzrFilePath);
		if (!jzrFile.exists()) {
			throw new JzrInitializationException(
					"Analysis configuration file \""
							+ jzrFilePath.replace('\\', '/')
							+ "\" not found. Please fix it.");
		}

		loadConfiguration(jzrFile);

		loadVersion();

		setBuiltinProperties();
		
		dumpProperties();
	}

	private void setBuiltinProperties() {
		if (Boolean.parseBoolean((String) config
				.get(JZRA_DISCOVERY_MODE_ENABLED)))
			setBuiltinDisplayHighlightDiscoveryRegexProperties();
		else
			disableHighlightDiscoveryRegexProperties(); // in case
														// BUILT_JEYZER_ANALYZER_DISCOVERY_FUNCTIONS_REGEX_<x>
														// variable is defined
														// in any discovery
														// report..
	}

	private void disableHighlightDiscoveryRegexProperties() {
		Properties props = new Properties();

		for (int i = 1; i < 6; i++) {
			props.put(
					PROPERTY_BUILT_JEYZER_ANALYZER_DISCOVERY_FUNCTIONS_REGEX_PREFIX
							+ i, DISABLED_DISCOVERY_REGEX_FIELD);
		}

		// add it to thread local properties
		ConfigThreadLocal.put(props);
	}

	private void setBuiltinDisplayHighlightDiscoveryRegexProperties() {
		Properties props = new Properties();

		for (int i = 1; i < 6; i++) {
			String variableName = ConfigUtil.VARIABLE_PREFIX
					+ PROPERTY_JEYZER_ANALYZER_DISCOVERY_FUNCTIONS_PREFIX + i
					+ ConfigUtil.VARIABLE_SUFFIX;
			String value = ConfigUtil.resolveVariable(variableName);

			String regex;
			if (value.equals(variableName) || value.isEmpty())
				regex = INVALID_DISCOVERY_REGEX_FIELD; // variable not found or
														// empty
			else
				regex = buildRegex(value);

			props.put(
					PROPERTY_BUILT_JEYZER_ANALYZER_DISCOVERY_FUNCTIONS_REGEX_PREFIX
							+ i, regex);
		}

		// add it to thread local properties
		ConfigThreadLocal.put(props);
	}

	private String buildRegex(String discoveryKeywords) {
		StringBuilder regex = new StringBuilder();

		regex.append(".*(");

		StringTokenizer tokenizer = new StringTokenizer(discoveryKeywords, ",");

		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken().trim();
			// balloon
			regex.append(token);
			regex.append("|");
			// Balloon
			regex.append(token.substring(0, 1).toUpperCase()
					+ token.substring(1));
			if (tokenizer.hasMoreTokens())
				regex.append("|");
		}

		regex.append(").*");

		return regex.toString();
	}

	private void loadConfiguration(File file) throws JzrInitializationException {

		try {
			Document doc = ConfigUtil.loadDOM(file);

			// analysis
			NodeList nodes = doc.getElementsByTagName(JZRA_ANALYSIS);
			Element analysisNode = (Element) nodes.item(0);

			// Application type
			config.put(JZRA_APPLICATION_TYPE, ConfigUtil.getAttributeValue(
					analysisNode, JZRA_APPLICATION_TYPE));

			// Analyzer configuration state
			loadState(analysisNode);

			// Context node
			Element contextNode = ConfigUtil.getFirstChildNode(analysisNode,
					JZRA_CONTEXT);

			// Description - optional
			String desc = ConfigUtil.getAttributeValue(contextNode,
					JZRA_DESCRIPTION);
			desc = desc != null ? desc : "Not available";
			config.put(JZRA_DESCRIPTION, desc);

			// Issuer email - optional
			String issuer = ConfigUtil.getAttributeValue(contextNode,
					JZRA_ISSUER);
			issuer = issuer != null ? issuer : "Not available";
			config.put(JZRA_ISSUER, issuer);

			// node id
			config.put(JZRA_APPLICATION_ID, ConfigUtil.getAttributeValue(
					analysisNode, JZRA_APPLICATION_ID));

			// discovery_mode
			loadDiscovery(analysisNode);

			// dependencies
			Element dependenciesNode = ConfigUtil.getFirstChildNode(
					analysisNode, JZRA_DEPENDENCIES);
			loadDependencies(dependenciesNode);

			// static patterns
			Element patternsNode = ConfigUtil.getFirstChildNode(analysisNode, JZRA_PATTERNS);
			loadPatternPaths(patternsNode);
			this.dynamicPatternsLoadingCfg = new ConfigDynamicLoading(ConfigUtil.getFirstChildNode(patternsNode, JZRA_DYNAMIC_PATTERN_SETS));

			// report
			Element reportNode = ConfigUtil.getFirstChildNode(analysisNode, JZRA_REPORT);

			// report configuration file
			config.put(JZRA_REPORT_CONFIG_FILE, ConfigUtil.getAttributeValue(
					reportNode, JZRA_REPORT_CONFIG_FILE));

			// notification
			Element notificationNode = ConfigUtil.getFirstChildNode(
					analysisNode, JZRA_NOTIFICATION);

			// notification event threshold
			config.put(JZRA_NOTIFICATION_MONITORING_EVENT_THRESHOLD, ConfigUtil
					.getAttributeValue(notificationNode,
							JZRA_NOTIFICATION_THRESHOLD));

			// mail
			mailCfg = new ConfigMail(notificationNode);

			// recording
			Element recordingNode = ConfigUtil.getFirstChildNode(analysisNode,
					JZRA_RECORDING);

			// recording period : can be standard duration or -1s (set through
			// web usually)
			this.threadDumpPeriod = ConfigUtil.getAttributeDuration(
					recordingNode, JZRA_THREAD_DUMP_PERIOD);

			// recording directory
			String recordingDirectory = ConfigUtil.getAttributeValue(
					recordingNode, JZRA_JEYZER_RECORD_DIRECTORY);
			config.put(JZRA_JEYZER_RECORD_DIRECTORY, recordingDirectory);

			// recording zip or gz file name : optional 
			String recordingFileName = recordingNode.getAttribute(JZRA_JEYZER_RECORD_FILE); 
			recordingFileName = ConfigUtil.resolveVariable(recordingFileName, false); // no warning if not resolved
			if (ConfigUtil.isVariableUnresolved(recordingFileName))
				recordingFileName = ""; // variable not resolved, replace by empty string 
			config.put(JZRA_JEYZER_RECORD_FILE, recordingFileName);

			// translators
			Element translatorsNode = ConfigUtil.getFirstChildNode(
					analysisNode, JZRA_TRANSLATORS);
			loadTranslators(translatorsNode, recordingDirectory);

			// file patterns
			Element filePatternsNode = ConfigUtil.getFirstChildNode(
					recordingNode, JZRA_THREAD_DUMP_FILE_PATTERNS);
			loadFilePatterns(filePatternsNode,
					ConfigFilePattern.JZRA_RECORDING_FILE_JZR_PATTERN); // should be unique
			loadFilePatterns(filePatternsNode,
					ConfigFilePattern.JZRA_RECORDING_FILE_TIMESTAMP_PATTERN);
			loadFilePatterns(filePatternsNode,
					ConfigFilePattern.JZRA_RECORDING_FILE_REGEX_PATTERN);

			// stack node
			Element stackNode = ConfigUtil.getFirstChildNode(analysisNode,
					JZRA_THREAD_STACK);

			preFilterCfg = new ConfigPreFilter(stackNode);
			
			// UFO node
			Element ufoNode = ConfigUtil.getFirstChildNode(analysisNode, JZRA_UFO);

			// UFO attributes
			config.put(JZRA_UFO + JZRA_UFO_THREAD_STACK_GENERATION_ENABLED,
					ConfigUtil.getAttributeValue(ufoNode,
							JZRA_UFO_THREAD_STACK_GENERATION_ENABLED));
			config.put(JZRA_UFO + JZRA_UFO_DIRECTORY,
					ConfigUtil.getAttributeValue(ufoNode, JZRA_UFO_DIRECTORY));
			config.put(JZRA_UFO + JZRA_UFO_ORDER_BY,
					ConfigUtil.getAttributeValue(ufoNode, JZRA_UFO_ORDER_BY));

			// ignored stack node
			Element ignoredStackNode = ConfigUtil.getFirstChildNode(stackNode,
					JZRA_THREAD_STACK_IGNORED);

			// ignored stack generation enabled
			config.put(JZRA_THREAD_STACK_GENERATION_ENABLED, ConfigUtil
					.getAttributeValue(ignoredStackNode,
							JZRA_THREAD_STACK_GENERATION_ENABLED));

			// ignored stack output dir
			config.put(JZRA_THREAD_STACK_OUTPUT_DIR, ConfigUtil
					.getAttributeValue(ignoredStackNode,
							JZRA_THREAD_STACK_OUTPUT_DIR));
			
			// stack ordering
			sortingCfg = new ConfigStackSorting(stackNode);
			
			// time zones
			loadTimeZone(analysisNode, JZRA_RECORDING_TIME_ZONE, JZRA_RECORDING_TIME_ZONE_USER_SELECTED_VAR, JZRA_RECORDING_TIME_ZONE_USER_SPECIFIED, JZRA_RECORDING_TIME_ZONE_VAR);
			loadTimeZone(analysisNode, JZRA_DISPLAY_TIME_ZONE, JZRA_DISPLAY_TIME_ZONE_USER_SELECTED_VAR, JZRA_DISPLAY_TIME_ZONE_USER_SPECIFIED, JZRA_DISPLAY_TIME_ZONE_VAR);
			
			// setup node
			Element setupNode = ConfigUtil.getFirstChildNode(analysisNode,
					JZRA_SETUP);

			// report configuration file
			config.put(JZRA_SETUP_CONFIG_FILE, ConfigUtil.getAttributeValue(
					setupNode, JZRA_SETUP_CONFIG_FILE));

			// load replay configuration (optional)
			Element replayNode = ConfigUtil.getFirstChildNode(analysisNode,
					JZRA_REPLAY);
			this.replayCfg = new ConfigReplay(replayNode);
		} catch (Exception e) {
			throw new JzrInitializationException(
					"Failed to load the Jeyzer analysis configuration.", e);
		}

	}

	private void loadDiscovery(Element analysisNode) {
		Element discoveryNode = ConfigUtil.getFirstChildNode(analysisNode,
				JZRA_DISCOVERY);
		
		if (discoveryNode != null) {
			// Version 2.6+
			Element discoveryRulesNode = ConfigUtil.getFirstChildNode(discoveryNode, JZRA_DISCOVERY_RULES);
			if (discoveryRulesNode != null)
				config.put(JZRA_DISCOVERY_MODE_ENABLED, ConfigUtil.getAttributeValue(discoveryRulesNode, JZRA_ENABLED));
		}
		else {
			// Backward compatibility (2.0 to 2.5)
			Element discoveryModeNode = ConfigUtil.getFirstChildNode(analysisNode, JZRA_DISCOVERY_MODE);
			if (discoveryModeNode != null)
				config.put(JZRA_DISCOVERY_MODE_ENABLED, ConfigUtil.getAttributeValue(discoveryModeNode, JZRA_ENABLED));
			else
				config.put(JZRA_DISCOVERY_MODE_ENABLED, Boolean.FALSE.toString());
		}
			
		this.profileDiscoveryCfg = new ConfigProfileDiscovery(discoveryNode);
	}

	private void loadTimeZone(Element analysisNode, String typeField, String userSpecifiedVariableName, String userSpecifiedField, String timeZoneIdVariableName) throws JzrInitializationException {
		// User selection
		String value = ConfigUtil.resolveVariable(userSpecifiedVariableName, false);
		if (!ConfigUtil.isVariableUnresolved(value) && Boolean.parseBoolean(value)) {
			String timeZoneId = ConfigUtil.resolveVariable(timeZoneIdVariableName, false);
			if (!ConfigUtil.isVariableUnresolved(timeZoneId)) {
				this.config.put(typeField, timeZoneId);
				this.config.put(userSpecifiedField, Boolean.TRUE.toString());
				return;
			}
			else {
				throw new JzrInitializationException("Time zone is specified by the end user but no time zone id is provided.");
			}
		}
		else {
			this.config.put(userSpecifiedField, Boolean.FALSE.toString());
		}
		
		// Time zone id
		Element timeZonesNode = ConfigUtil.getFirstChildNode(analysisNode, JZRA_TIME_ZONES);
		if (timeZonesNode == null)
			return;
		Element timeZoneTypeNode = ConfigUtil.getFirstChildNode(timeZonesNode, typeField);
		if (timeZoneTypeNode == null)
			return;
		String id = ConfigUtil.getAttributeValue(timeZoneTypeNode, JZRA_TIME_ZONE_ID);
		if (ConfigUtil.isVariableUnresolved(id))
			return;
		
		// validate the time zone id
		if (!TimeZoneInfoHelper.isValidTimeZone(id))
			throw new JzrInitializationException("Invalid time zone id : " + id + " for the " + typeField + " field.");
	
		this.config.put(typeField, id);
		this.config.put(userSpecifiedField, Boolean.FALSE.toString());
	}

	private void loadState(Element analysisNode) throws JzrInitializationException {
		// If not specified, consider it as draft	
		String value = ConfigUtil.getAttributeValue(analysisNode, JZRA_STATE);
		if (value.isEmpty()) {
			this.state = ConfigState.DRAFT;
			return;
		}

		try {
			this.state = ConfigState.valueOf(value.trim().toUpperCase());
		}catch(IllegalArgumentException ex) {
			throw new JzrInitializationException("Analysis profile "
					+ config.get(JZRA_APPLICATION_TYPE) + " is invalid : state " + value + " is not recognized. "
					+ "Must be one of those values :" + ConfigState.values());
		}

		if (this.state.isDisabled())
			throw new JzrInitializationException("Analysis profile "
				+ config.get(JZRA_APPLICATION_TYPE) + " disabled.");
	}

	private void loadTranslators(Element translatorsNode,
			String threadDumpDirectory) throws JzrInitializationException {
		NodeList nodes = translatorsNode.getElementsByTagName(JZRA_TRANSLATOR);
		for (int i = 0; i < nodes.getLength(); i++) {
			Element translatorNode = (Element) nodes.item(i);

			String type = ConfigUtil.getAttributeValue(translatorNode,
					JZRA_TYPE);
			if (type == null || type.isEmpty())
				continue;

			String translatorConfigPath = ConfigUtil.getAttributeValue(
					translatorNode, JZRA_TRANSLATOR_CONFIG_FILE);
			if (translatorConfigPath == null || translatorConfigPath.isEmpty())
				continue;

			File translatorFile = new File(translatorConfigPath);
			if (!SystemHelper.doesFileExist(translatorConfigPath)) {
				logger.error("Translator file " + translatorConfigPath
						+ " not found.");
				throw new JzrInitializationException("Translator file "
						+ translatorConfigPath + " not found.");
			}

			Document doc = ConfigUtil.loadDOM(translatorFile);

			NodeList fileNodes = doc.getElementsByTagName(JZRA_TRANSLATOR);
			Element translNode = (Element) fileNodes.item(0);
			if (translNode == null)
				throw new JzrInitializationException("Invalid translator file "
						+ translatorConfigPath + ". Translator node not found.");

			if (ConfigDecompression.TYPE_NAME.equals(type)) {
				// decompression
				ConfigTranslator translatorCfg = new ConfigDecompression(
						translNode, translatorConfigPath, threadDumpDirectory);
				this.translators.add(translatorCfg);
			} else if (ConfigDecryption.TYPE_NAME.equals(type)) {
				// decryption
				ConfigTranslator translatorCfg = new ConfigDecryption(
						translNode, translatorConfigPath, threadDumpDirectory);
				this.translators.add(translatorCfg);
			} else if (ConfigDeobfuscation.TYPE_NAME.equals(type)) {
				// deobfuscation
				ConfigTranslator translatorCfg = new ConfigDeobfuscation(
						translNode, translatorConfigPath, threadDumpDirectory);
				this.translators.add(translatorCfg);
			} else if (ConfigJFRDecompression.TYPE_NAME.equals(type)) {
				// JFR
				ConfigTranslator translatorCfg = new ConfigJFRDecompression(
						translNode, translatorConfigPath, threadDumpDirectory);
				this.translators.add(translatorCfg);
			} else {
				logger.error("Translator configuration implementation not found for type : "
						+ type);
			}
		}
		
		for (ConfigTranslator translatorCfg : this.translators) {
			this.translatorInputFileExtensions.addAll(translatorCfg.getSupportedInputFileExtensions());
		}
	}

	private void loadDependencies(Element dependenciesNode) {
		NodeList nodes = dependenciesNode.getElementsByTagName(JZRA_SET);
		for (int i = 0; i < nodes.getLength(); i++) {
			Element setNode = (Element) nodes.item(i);

			String id = ConfigUtil.getAttributeValue(setNode, JZRA_ID);
			if (id == null || id.isEmpty())
				continue;

			String profiles = ConfigUtil.getAttributeValue(setNode,
					JZRA_PROFILES);
			StringTokenizer st = new StringTokenizer(profiles, ",");
			while (st.hasMoreTokens()) {
				String profile = st.nextToken().trim();
				if (!profile.isEmpty())
					dependencies.put(id, profile);
			}
		}
	}

	private void loadPatternPaths(Element patternsNode) {
		NodeList patternNodes = patternsNode.getChildNodes();
		for (int j = 0; j < patternNodes.getLength(); j++) {
			Node node = patternNodes.item(j);
			if (node.getNodeType() == Node.ELEMENT_NODE
					&& JZRA_PATTERN_SET.equals(((Element) node).getTagName())) {
				Element patternNode = (Element) node;
				String location = ConfigUtil.getAttributeValue(patternNode, JZRA_FILE);
				if (location != null && !location.isEmpty())
					this.patternLocations.add(new SingleJzrResourceLocation(location));
			} else if (node.getNodeType() == Node.ELEMENT_NODE
					&& JZRA_PATTERN_SETS.equals(((Element) node).getTagName())) {
				Element patternNode = (Element) node;
				String location = ConfigUtil.getAttributeValue(patternNode, JZRA_FILES);
				if (location != null && !location.isEmpty())
					this.patternLocations.add(new MultipleJzrResourceLocation(location));
			}
		}
	}

	private void loadFilePatterns(Element filePatternsNode, String type) {
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

	private void loadVersion() {
		try {
			Class<ConfigAnalyzer> clazz = ConfigAnalyzer.class;
			String className = clazz.getSimpleName() + ".class";
			String classPath = clazz.getResource(className).toString();
			if (!classPath.startsWith("jar")) {
				// Class not from JAR
				config.put(JEYZER_ANALYZER_VERSION, JEYZER_ANALYZER_LABEL);
				System.setProperty(JEYZER_ANALYZER_VERSION,
						JEYZER_ANALYZER_LABEL);
				return;
			}
			String manifestPath = classPath.substring(0,
					classPath.lastIndexOf('!') + 1)
					+ "/META-INF/MANIFEST.MF";
			Manifest manifest = new Manifest(new URL(manifestPath).openStream());
			Attributes attr = manifest.getMainAttributes();
			String value = attr.getValue("Specification-Version");
			if (value == null) {
				// Class from JAR within War dev file
				config.put(JEYZER_ANALYZER_VERSION, JEYZER_ANALYZER_LABEL);
				System.setProperty(JEYZER_ANALYZER_VERSION,
						JEYZER_ANALYZER_LABEL);
				return;
			}
			config.put(JEYZER_ANALYZER_VERSION, value);
			System.setProperty(JEYZER_ANALYZER_VERSION, value);
		} catch (IOException e) {
			logger.warn("Failed to read Jeyzer version from Manifest", e);
			config.put(JEYZER_ANALYZER_VERSION, "unknown version");
			System.setProperty(JEYZER_ANALYZER_VERSION, "unknown version");
		}
	}

	public boolean isProductionReady() {
		return this.state.isProduction();
	}
	
	public ConfigState getState() {
		return this.state;
	}
	
	public String getParamValue(String name) {
		return config.getProperty(name);
	}

	public List<ConfigFilePattern> getSnapshotFilePatterns() {
		return this.filePatterns;
	}

	public List<ConfigTranslator> getTranslators() {
		return this.translators;
	}
	
	public List<String> getTranslatorInputFileExtensions() {
		return this.translatorInputFileExtensions;
	}

	public List<JzrResourceLocation> getPatternsLocations() {
		return this.patternLocations;
	}

	public ConfigDynamicLoading getDynamicPatternsLoadingCfg() {
		return dynamicPatternsLoadingCfg;
	}

	public Multimap<String, String> getDependencies() {
		return dependencies;
	}

	public ConfigMail getConfigMail() {
		return this.mailCfg;
	}

	public ConfigReplay getReplayCfg() {
		return replayCfg;
	}
	
	public ConfigPreFilter getPreFilterCfg() {
		return this.preFilterCfg;
	}

	public int getThreadDumpPeriod() {
		return (int) threadDumpPeriod.getSeconds();
	}
	
	public ConfigStackSorting getStackSortingCfg() {
		return this.sortingCfg;
	}
	
	public ConfigProfileDiscovery getProfileDiscoveryCfg() {
		return this.profileDiscoveryCfg;
	}

	public void dumpProperties() {
		if (!logger.isInfoEnabled())
			return;
		
		Enumeration<?> e = config.propertyNames();
		Map<String,String> startupProps = new TreeMap<>();

		logger.info("Analysis startup properties :");
		while (e.hasMoreElements()) {
			String key = (String) e.nextElement();
			String value = config.getProperty(key);
			if (value != null)
				startupProps.put(key, value);
		}
		
		// display it ordered
		for (String key : startupProps.keySet()) {
			// format paths in correct way
			logger.info("- {} : {}", key, startupProps.get(key).replace('\\', '/'));			
		}
	}
}
