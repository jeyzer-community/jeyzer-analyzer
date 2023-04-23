package org.jeyzer.monitor.config;

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







import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.jeyzer.analyzer.error.JzrException;
import org.jeyzer.analyzer.util.SystemHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigMonitorSession {

	private static final Logger logger = LoggerFactory.getLogger(ConfigMonitorSession.class);	
	
	private static final String JZRM_PROPERTIES_FILE = "session.properties";
	
	public static final String JZRM_PREVIOUS_SESSION_DATE = "previous.session.date";
	
	private Properties props;
	private String sessionPath;
	private String filePath;
	
	public ConfigMonitorSession(String sessionPath, String profile){
		this.sessionPath = sessionPath;
		this.filePath = sessionPath + "/" + profile + "_" + JZRM_PROPERTIES_FILE;
		this.props = loadSessionProperties();
	}
	
	private Properties loadSessionProperties(){
		Properties properties = new Properties();
		 
		try (
				InputStream input = new FileInputStream(filePath);
			)
		{ 
			properties.load(input);
		} catch (IOException ex) {
			logger.info("Monitoring session properties file not found : {}. File will be created automatically.", 
					SystemHelper.sanitizePathSeparators(filePath));
		}
		
		return properties;
	}
	
	public void saveSession(){
		try {
			SystemHelper.createDirectory(sessionPath);
		} catch (JzrException ex) {
			logger.warn("Session directory creation failed : {}", 
					SystemHelper.sanitizePathSeparators(filePath), ex);
		}
		try (
				OutputStream output = new FileOutputStream(filePath);
			)
		{
			props.store(output, null);
		} catch (Exception io) {
			logger.warn("Session properties file saving failed : {}", 
					SystemHelper.sanitizePathSeparators(filePath), io);
		}
	}
	
	public String getProperty(String key){
		return this.props.getProperty(key);
	}

	public String getProperty(String key, String defaultValue){
		return getProperty(key) != null ? getProperty(key) : defaultValue;
	}	
	
	public void setProperty(String key, String value){
		this.props.setProperty(key, value);
	}	
	
}
