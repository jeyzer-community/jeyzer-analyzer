package org.jeyzer.analyzer.config.translator.jfr;


import java.util.TimeZone;

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
import org.jeyzer.analyzer.config.translator.ConfigTranslator;
import org.jeyzer.analyzer.data.TimeZoneInfo;
import org.jeyzer.analyzer.data.TimeZoneInfo.TimeZoneOrigin;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.analyzer.math.FormulaHelper;
import org.jeyzer.analyzer.util.JFRHelper;
import org.jeyzer.analyzer.util.SystemHelper;
import org.jeyzer.analyzer.util.TimeZoneInfoHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

public class ConfigJFRDecompression extends ConfigTranslator {

	private static final Logger logger = LoggerFactory.getLogger(ConfigJFRDecompression.class);

	public static final String TYPE_NAME = "jfr";
	
	private static final String JZRA_JFR_COMPRESSION = "jfr_uncompress";
	private static final String JZRA_KEEP_FILES = "keep_files";
	private static final String JZRA_DIRECTORY = "directory";
	
	private static final String JZRA_TIME_ZONE = "time_zone";
	private static final String JZRA_TIME_ZONE_SOURCE = "source";
	private static final String JZRA_TIME_ZONE_CUSTOM = "custom";
		
	private static final String JZRA_DUMP_EVENTS = "dump_events";
	private static final String JZRA_DUMP_PER_TYPE = "per_type";
	private static final String JZRA_DUMP_ALL = "all";
	
	private static final String JZRA_LIMITS = "limits";
	private static final String JZRA_FILE_SIZE_LIMIT = "file_size_limit";

	private static final String JZRA_TIME_ZONE_SOURCE_JFR = "jfr";
	private static final String JZRA_TIME_ZONE_SOURCE_PROCESS = "process";
	private static final String JZRA_TIME_ZONE_SOURCE_CUSTOM = "custom";

	
	private static final int DEFAULT_FILE_SIZE_LIMIT = 100; // Mb
	
	private int fileSizeLimit = DEFAULT_FILE_SIZE_LIMIT;
	
	private boolean dumpJFRDataPerType;
	private boolean dumpAllJFRData;
	private String dumpDir;
	
	private TimeZoneInfo.TimeZoneOrigin origin = TimeZoneInfo.TimeZoneOrigin.JFR;
	private TimeZone customTimeZone;
	
	public ConfigJFRDecompression(Element translatorNode, String configFilePath, String threadDumpDirectory) throws JzrInitializationException {
		super(TYPE_NAME, configFilePath, true); // always abort on error
		if (translatorNode == null)
			return;
		
		supportedInputFileExtensions.add(JFRHelper.JFR_EXTENSION);
		
		loadJFRCompression(translatorNode, threadDumpDirectory);
	}
	
	public long getFileSizeLimitInBytes() {
		return (long)fileSizeLimit * FormulaHelper.BYTES_IN_1_MB;
	}
	
	public int getFileSizeLimit() {
		return fileSizeLimit;
	}
	
	public boolean isJFRDataPerTypeDump() {
		return dumpJFRDataPerType;
	}
	
	public boolean isAllJFRDataDump() {
		return dumpAllJFRData;
	}
	
	public String getDumpDirectory() {
		return dumpDir;
	}
	
	public TimeZoneInfo.TimeZoneOrigin getTimeZoneOrigin(){
		return this.origin;
	}
	
	public TimeZone getCustomTimeZone(){
		return this.customTimeZone; // can be null
	}

	private void loadJFRCompression(Element translatorNode, String threadDumpDirectory) throws JzrInitializationException {
		if (!SystemHelper.isAtLeastJdK9()) {
			logger.info("The JFR translater cannot be instantiated. The Jeyzer Analyzer requires Java 9+ to read JFR files.");
			this.enabled = false;
			return;
		}

		Element compressionNode = ConfigUtil.getFirstChildNode(translatorNode, JZRA_JFR_COMPRESSION);
		if (compressionNode == null)
			throw new JzrInitializationException("Invalid JFR translater configuration file " + this.configFilePath + ". Compression node not found.");
						
		loadJFRUncompressDirectoryPath(compressionNode, threadDumpDirectory);
		loadKeepFiles(compressionNode);
		loadLimits(compressionNode);
		
		loadTimeZoneSource(compressionNode);
		
		loadJFRDump(compressionNode, threadDumpDirectory);
		
		this.enabled = true;
	}
	
	private void loadTimeZoneSource(Element compressionNode) {
		Element timeZoneNode = ConfigUtil.getFirstChildNode(compressionNode, JZRA_TIME_ZONE);
		if (timeZoneNode == null) {
			logger.warn("JFR translator time zone not specified. Defaulting to source : {}", this.origin.name().toLowerCase());
			return;
		}
		
		String source = ConfigUtil.getAttributeValue(timeZoneNode, JZRA_TIME_ZONE_SOURCE);
		if (source == null || source.isEmpty()) {
			logger.warn("JFR translator time zone source not specified. Defaulting to source : {}", this.origin.name().toLowerCase());
			return;
		}
		this.origin = parseSource(source);
		
		if (TimeZoneOrigin.CUSTOM.equals(origin)) {
			String custom = ConfigUtil.getAttributeValue(timeZoneNode, JZRA_TIME_ZONE_CUSTOM);
			if (custom == null || custom.isEmpty()) {
				logger.warn("JFR translator custom time zone id not specified. Defaulting to source : {}", TimeZoneOrigin.JFR.name().toLowerCase());
				this.origin = TimeZoneOrigin.JFR;
				return;
			}
			
			if (!TimeZoneInfoHelper.isValidTimeZone(custom)) {
				logger.warn("JFR translator custom time zone id is invalid. Defaulting to source : {}", TimeZoneOrigin.JFR.name().toLowerCase());
				this.origin = TimeZoneOrigin.JFR;
				return;
			}
			
			this.customTimeZone = TimeZone.getTimeZone(custom);
		}
	}

	private void loadJFRDump(Element compressionNode, String threadDumpDirectory) throws JzrInitializationException {
		Element dumpNode = ConfigUtil.getFirstChildNode(compressionNode, JZRA_DUMP_EVENTS);
		if (dumpNode == null) {
			logger.warn("Invalid JFR translater configuration file {}. {} node not found. JFR dump will not occur.", this.configFilePath, JZRA_DUMP_EVENTS);
			this.dumpJFRDataPerType = false;
			this.dumpAllJFRData = false;
		}
		
		String value = ConfigUtil.getAttributeValue(dumpNode, JZRA_DUMP_PER_TYPE);
		if (value == null || value.isEmpty()){
			logger.warn("JFR translater configuration file is missing the {} attribute. JFR per type dump will not occur.", JZRA_DUMP_PER_TYPE);
		}
		this.dumpJFRDataPerType = Boolean.valueOf(value);

		value = ConfigUtil.getAttributeValue(dumpNode, JZRA_DUMP_ALL);
		if (value == null || value.isEmpty()){
			logger.warn("JFR translater configuration file is missing the {} attribute. JFR per type dump will not occur.", JZRA_DUMP_ALL);
		}
		this.dumpAllJFRData = Boolean.valueOf(value);
		
		if (this.dumpJFRDataPerType || this.dumpAllJFRData) {
			String dirPath = ConfigUtil.getAttributeValue(dumpNode, JZRA_DIRECTORY);

			if (dirPath == null || dirPath.isEmpty())
				throw new JzrInitializationException("Invalid JFR translater configuration file " + configFilePath + ". JFR dump " + JZRA_DIRECTORY + " attribute not found.");
			
			dirPath = dirPath.replace('\\', '/');
			if (dirPath.equals(threadDumpDirectory.replace('\\', '/')))
				throw new JzrInitializationException("Failed to decompress the JFR recording. JFR dump directory cannot be equal to thread dump directory : " + dirPath);
			
			if (dirPath.equals(this.outputDir.replace('\\', '/')))
				throw new JzrInitializationException("Failed to decompress the JFR recording. JFR dump directory cannot be equal to the JZR snapshot output directory : " + dirPath);
			
			this.dumpDir = dirPath;
		}
	}

	private void loadLimits(Element compressionNode) {
		Element limitsNode = ConfigUtil.getFirstChildNode(compressionNode, JZRA_LIMITS);
		if (limitsNode == null) {
			logger.info("JFR uncompress limits not set. Defaulting on default limit values.");
			return;
		}
		
		String value = ConfigUtil.getAttributeValue(limitsNode, JZRA_FILE_SIZE_LIMIT);
		if (value == null || value.isEmpty() || value.startsWith("${")){
			logger.info("JFR uncompress file size limit not set. Defaulting on value : {} Mb", DEFAULT_FILE_SIZE_LIMIT);
		}
		else {
			try{
				this.fileSizeLimit = Integer.parseInt(value);
			}catch(java.lang.NumberFormatException e){
				logger.warn("JFR uncompress file size limit " + value + " is invalid. Defaulting on value : {} Mb", DEFAULT_FILE_SIZE_LIMIT);
			}
		}
		if (this.fileSizeLimit <= 0)
			logger.warn("JFR uncompress file size limit " + value + " is negative. Defaulting on value : {} Mb", DEFAULT_FILE_SIZE_LIMIT);
	}

	private void loadJFRUncompressDirectoryPath(Element compressionNode, String threadDumpDirectory) throws JzrInitializationException{
		String dirPath = ConfigUtil.getAttributeValue(compressionNode, JZRA_DIRECTORY);

		if (dirPath == null || dirPath.isEmpty())
			throw new JzrInitializationException("Invalid JFR translater configuration file " + configFilePath + ". Compression " + JZRA_DIRECTORY + " attribute not found.");
		
		dirPath = dirPath.replace('\\', '/');
		if (dirPath.equals(threadDumpDirectory.replace('\\', '/')))
			throw new JzrInitializationException("Failed to decompress the JFR recording. JFR decompress directory cannot be equal to thread dump directory : " + dirPath);
		
		this.outputDir = dirPath;
	}

	private void loadKeepFiles(Element compressionNode) {
		String value = ConfigUtil.getAttributeValue(compressionNode, JZRA_KEEP_FILES);
		if (value == null || value.isEmpty()){
			logger.warn("JFR translater configuration file is missing the {} attribute. Decompressed files will be discarded.", JZRA_KEEP_FILES);
		}
		this.keepTranslatedFiles = Boolean.valueOf(value);
	}
	
	private TimeZoneOrigin parseSource(String source) {
		if (JZRA_TIME_ZONE_SOURCE_JFR.equals(source))
			return TimeZoneOrigin.JFR;
		else if (JZRA_TIME_ZONE_SOURCE_PROCESS.equals(source))
			return TimeZoneOrigin.PROCESS;
		else if (JZRA_TIME_ZONE_SOURCE_CUSTOM.equals(source))
			return TimeZoneOrigin.CUSTOM;
		
		logger.warn("Invalid time zone source value : {} . Defaulting to source : {}", source, TimeZoneOrigin.JFR.name().toLowerCase());
		return TimeZoneOrigin.JFR;
	}
}
