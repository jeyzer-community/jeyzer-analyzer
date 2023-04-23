package org.jeyzer.web.config;

import java.io.File;

/*-
 * ---------------------------LICENSE_START---------------------------
 * Jeyzer Web
 * --
 * Copyright (C) 2020 Jeyzer
 * --
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 * ----------------------------LICENSE_END----------------------------
 */


import java.time.Duration;
import java.time.format.DateTimeParseException;

import javax.servlet.ServletConfig;

import org.jeyzer.analyzer.util.ZipParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.primitives.Ints;

public class ConfigWeb {
	
	private static final Logger logger = LoggerFactory.getLogger(ConfigWeb.class);

	private static final String ENV_WEB_JEYZER_DEBUG = "JEYZER_WEB_DEBUG";
	private static final String ENV_WEB_JEYZER_DISPLAY_FUNCTION_DISCOVERY = "JEYZER_WEB_DISPLAY_FUNCTION_DISCOVERY";
	private static final String ENV_WEB_JEYZER_DISPLAY_UFO_STACK_FILE_LINK = "JEYZER_WEB_DISPLAY_UFO_STACK_FILE_LINK";
	private static final String ENV_WEB_JEYZER_ANALYZER_THREAD_POOL_SIZE = "JEYZER_WEB_ANALYZER_THREAD_POOL_SIZE";
	private static final String ENV_WEB_JEYZER_TEMP_UPLOAD_DIRECTORY = "JEYZER_WEB_TEMP_UPLOAD_DIRECTORY";
	private static final String ENV_WEB_JEYZER_TEMP_UPLOAD_RECORDING_MAX_RETENTION_TIME = "JEYZER_WEB_TEMP_UPLOAD_RECORDING_MAX_RETENTION_TIME";
	private static final String ENV_WEB_JEYZER_TEMP_WORK_DIRECTORY = "JEYZER_WEB_TEMP_WORK_DIRECTORY";
	private static final String ENV_WEB_JEYZER_UPLOAD_RECORDING_MAX_SIZE = "JEYZER_WEB_UPLOAD_RECORDING_MAX_SIZE"; // in Mb
	private static final String ENV_WEB_JEYZER_UPLOAD_RECORDING_UNCOMPRESSED_MAX_SIZE = "JEYZER_WEB_UPLOAD_RECORDING_UNCOMPRESSED_MAX_SIZE"; // in Mb
	private static final String ENV_WEB_JEYZER_UPLOAD_RECORDING_UNCOMPRESSED_MAX_FILES = "JEYZER_WEB_UPLOAD_RECORDING_UNCOMPRESSED_MAX_FILES";
	private static final String ENV_WEB_JEYZER_WEB_DEFAULT_SUBMITTER_EMAIL = "JEYZER_WEB_DEFAULT_SUBMITTER_EMAIL";
	private static final String ENV_WEB_JEYZER_WEB_DEFAULT_ISSUE_DESCRIPTION = "JEYZER_WEB_DEFAULT_ISSUE_DESCRIPTION";

	public static final String SERVLET_PARAM_FUNCTION_DISCOVERY_DISPLAY = "function-discovery-display";
	public static final String SERVLET_PARAM_UFO_STACK_FILE_LINK_DISPLAY = "ufo-stack-file-link-display";
	public static final String SERVLET_PARAM_ANALYZER_THREAD_POOL_SIZE = "analyzer-thread-pool-size";
	public static final String SERVLET_PARAM_TEMP_UPLOAD_RECORDING_MAX_RETENTION_TIME = "temp-upload-recording-max-retention-time";
	public static final String SERVLET_PARAM_UPLOAD_RECORDING_MAX_SIZE = "upload-recording-max-size";
	public static final String SERVLET_PARAM_UPLOAD_RECORDING_UNCOMPRESSED_MAX_SIZE = "upload-recording-uncompressed-max-size";
	public static final String SERVLET_PARAM_UPLOAD_RECORDING_UNCOMPRESSED_MAX_FILES = "upload-recording-uncompressed-max-files";
	public static final String SERVLET_PARAM_DEFAULT_SUBMITTER_EMAIL = "default-submitter-email";
	public static final String SERVLET_PARAM_DEFAULT_ISSUE_DESCRIPTION = "default-issue-description";
	
	public static final String ISO_8601_DURATION_PREFIX = "PT";
	
	private static final int DEFAULT_ANALYZER_THREAD_POOL_SIZE = 2;
	private static final long DEFAULT_TEMP_UPLOAD_RECORDING_MAX_RETENTION_TIME = 300000L; // 5mn
	
	public static final String JZR_WEB_JEYZER_DEFAULT_WORK_DIRECTORY = "Jeyzer-work";
	
	private boolean debugMode = false;
    private boolean functionDiscoveryDisplayEnabled = false;
    private boolean ufoStackFileLinkDisplayEnabled = false;
    private Integer analyzerThreadPoolSize;
    private String tempUploadDirectory;
    private long tempUploadRecordingMaxRetentionTime;
    private String workDirectory;
    private int uploadRecordingMaxSize; // in Mb
    private int uploadRecordingUncompressedMaxSize; // in Mb
    private int uploadRecordingUncompressedMaxFiles;
    private String defaultSubmitterEmail;
    private String defaultIssueDescription;
    
    private ConfigPortal portalCfg;
    
    public ConfigWeb(ServletConfig servletConfig){    	
    	debugMode= Boolean.valueOf(System.getenv(ENV_WEB_JEYZER_DEBUG));
    	
		functionDiscoveryDisplayEnabled = ConfigUtil.loadBoolAttribute(servletConfig, ENV_WEB_JEYZER_DISPLAY_FUNCTION_DISCOVERY, SERVLET_PARAM_FUNCTION_DISCOVERY_DISPLAY);
		ufoStackFileLinkDisplayEnabled = ConfigUtil.loadBoolAttribute(servletConfig, ENV_WEB_JEYZER_DISPLAY_UFO_STACK_FILE_LINK, SERVLET_PARAM_UFO_STACK_FILE_LINK_DISPLAY);
		
		analyzerThreadPoolSize = ConfigUtil.loadIntAttribute(servletConfig, ENV_WEB_JEYZER_ANALYZER_THREAD_POOL_SIZE, SERVLET_PARAM_ANALYZER_THREAD_POOL_SIZE);
		if (analyzerThreadPoolSize == null){
			logger.warn("The thread pool size is not specified. Defaulting to pool size : " + DEFAULT_ANALYZER_THREAD_POOL_SIZE);
			analyzerThreadPoolSize = DEFAULT_ANALYZER_THREAD_POOL_SIZE;
		}
		
    	tempUploadDirectory = System.getenv(ENV_WEB_JEYZER_TEMP_UPLOAD_DIRECTORY);
    	
    	String value = ConfigUtil.loadStringAttribute(servletConfig, ENV_WEB_JEYZER_TEMP_UPLOAD_RECORDING_MAX_RETENTION_TIME, SERVLET_PARAM_TEMP_UPLOAD_RECORDING_MAX_RETENTION_TIME);
    	Duration duration = parseDuration(value);
    	if (duration == null)
    		logger.warn("Temp upload recording max retention time is defaulted to " + DEFAULT_TEMP_UPLOAD_RECORDING_MAX_RETENTION_TIME / 60000L + " minutes.");
    	tempUploadRecordingMaxRetentionTime = duration != null ? duration.toMillis() : DEFAULT_TEMP_UPLOAD_RECORDING_MAX_RETENTION_TIME;
    	
    	workDirectory = System.getenv(ENV_WEB_JEYZER_TEMP_WORK_DIRECTORY);
    	if (workDirectory == null)
    		workDirectory = System.getProperty("java.io.tmpdir") + File.separator + JZR_WEB_JEYZER_DEFAULT_WORK_DIRECTORY; // default if not specified. Ensure upgrade
    	
    	uploadRecordingMaxSize = ConfigUtil.loadIntAttribute(servletConfig, ENV_WEB_JEYZER_UPLOAD_RECORDING_MAX_SIZE, SERVLET_PARAM_UPLOAD_RECORDING_MAX_SIZE);
    	uploadRecordingUncompressedMaxSize = ConfigUtil.loadIntAttribute(servletConfig, ENV_WEB_JEYZER_UPLOAD_RECORDING_UNCOMPRESSED_MAX_SIZE, SERVLET_PARAM_UPLOAD_RECORDING_UNCOMPRESSED_MAX_SIZE);
    	uploadRecordingUncompressedMaxFiles = ConfigUtil.loadIntAttribute(servletConfig, ENV_WEB_JEYZER_UPLOAD_RECORDING_UNCOMPRESSED_MAX_FILES, SERVLET_PARAM_UPLOAD_RECORDING_UNCOMPRESSED_MAX_FILES);
    	
    	defaultSubmitterEmail = ConfigUtil.loadStringAttribute(servletConfig, ENV_WEB_JEYZER_WEB_DEFAULT_SUBMITTER_EMAIL, SERVLET_PARAM_DEFAULT_SUBMITTER_EMAIL);
    	defaultIssueDescription = ConfigUtil.loadStringAttribute(servletConfig, ENV_WEB_JEYZER_WEB_DEFAULT_ISSUE_DESCRIPTION, SERVLET_PARAM_DEFAULT_ISSUE_DESCRIPTION);
    	defaultIssueDescription = defaultIssueDescription.replaceAll("\\\\n", "\n");
    	
    	portalCfg = new ConfigPortal(servletConfig);
    }
    
	public boolean isDebugMode() {
		return debugMode;
	}

	public boolean isFunctionDiscoveryDisplayEnabled() {
		return functionDiscoveryDisplayEnabled;
	}
	
	public boolean isUfoStackFileLinkDisplayEnabled() {
		return ufoStackFileLinkDisplayEnabled;
	}

	public Integer getAnalyzerThreadPoolSize() {
		return analyzerThreadPoolSize;
	}
	
	public String getTempUploadDirectory() {
		return tempUploadDirectory;
	}

	public long getTempUploadRecordingMaxRetentionTime() {
		return tempUploadRecordingMaxRetentionTime;
	}
	
	public String getWorkDirectory() {
		return workDirectory;
	}
	
	public long getUploadRecordingMaxSizeInBytes() {
		return (long)uploadRecordingMaxSize * ConfigUtil.BYTES_IN_1_MB;
	}
	
	public int getUploadRecordingMaxSize() {
		return uploadRecordingMaxSize;
	}
	
	public int getUploadRecordingUncompressedMaxSize() {
		return uploadRecordingUncompressedMaxSize;
	}

	public int getUploadRecordingUncompressedMaxFiles() {
		return uploadRecordingUncompressedMaxFiles;
	}

	public String getDefaultSubmitterEmail() {
		return defaultSubmitterEmail;
	}
	
	public String getDefaultIssueDescription() {
		return defaultIssueDescription;
	}

	public ConfigPortal getConfigPortal() {
		return this.portalCfg;
	}
	
	public ZipParams getConfigGzipParams() {
		return new ConfigGzipParams(this);
	}
	
	/**
	 * Creates a duration from an ISO-8601 date. Examples : 10m, 1H30M, 30s.
	 * The "PT" prefix is optional. Any contained variable is expanded first.
	 * See https://docs.oracle.com/javase/8/docs/api/java/time/Duration.html#parse-java.lang.CharSequence-.
	 */
	private Duration parseDuration(String value){
		if (value == null || value.isEmpty())
			return null;
		
		// digits only, convert it in seconds by default
		Integer testValue = Ints.tryParse(value);
		if (testValue != null){
			logger.info("Time value given without ISO-8601 time unit : " + testValue + ". Defaulting to time unit in seconds.");
			value += "s";
		}
	
		if (!value.startsWith(ISO_8601_DURATION_PREFIX))
			value = ISO_8601_DURATION_PREFIX + value;
		
		try{
			return Duration.parse(value);
		}catch(DateTimeParseException ex){
			logger.warn("Failed to parse the given time value : " + value + "  Time value is not ISO-8601 compliant. Note that Jeyzer adds the ISO PT prefix if not present. See https://docs.oracle.com/javase/8/docs/api/java/time/Duration.html");
			return null;
		}
	}
}
