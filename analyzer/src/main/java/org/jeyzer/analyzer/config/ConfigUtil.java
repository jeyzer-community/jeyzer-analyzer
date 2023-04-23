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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.Enumeration;
import java.util.Properties;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.analyzer.util.SystemHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.primitives.Ints;

public class ConfigUtil {

	private static final Logger logger = LoggerFactory.getLogger(ConfigUtil.class);	
	
	public static final String VARIABLE_PREFIX = "${";
	public static final String VARIABLE_SUFFIX = "}";

	private static final String VAR_TYPE_THREAD_LOCAL = "thread local";
	private static final String VAR_TYPE_SYSTEM_PROPERTY = "system property";
	private static final String VAR_TYPE_ENV_VARIABLE = "environment variable";
	
	public static final String ISO_8601_DURATION_PREFIX = "PT";
	
	private ConfigUtil(){}
	
	public static Properties loadPropertyFile(File file){
		Properties props = new Properties();
		try {
			props.load(new FileInputStream(file.getPath()));
		} catch (IOException ex) {
			logger.error("Failed to open " + file.getPath(), ex);
		}
		return props;
	}
	
	public static Document loadXMLFile(String path){
		Document doc = null;
		if (ConfigUtil.isValidURI(path)){
			// uri : http, https, ftp, file..
			doc = ConfigUtil.loadDOM(path);	
		} 
		else{
			// classic file path
			File profileConfigFile = new File(path);
			doc = ConfigUtil.loadDOM(profileConfigFile);
		}
		return doc;
	}	
	
	public static boolean isValidURI(String candidate){
		try {
			URL u = new URL(candidate);
			u.toURI();
			return true;
		} catch (Exception e) {
			return false;
		}		
	}
	
	public static Document loadDOM(File file){
		Document doc = null;
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
			dbf.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = db.parse(file);
			doc.getDocumentElement().normalize();
		} catch (IOException e) {
			logger.error("Failed to open " + file.getPath(), e);
		} catch (ParserConfigurationException e) {
			logger.error("Failed to parse " + file.getPath(), e);
		} catch (SAXException e) {
			logger.error("Failed to parse " + file.getPath(), e);
		}
		return doc;
	}

	public static Document loadDOM(String uri){
		Document doc = null;
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
			dbf.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = db.parse(uri);
			doc.getDocumentElement().normalize();
		} catch (FileNotFoundException e) {
			logger.error("Failed to open " + uri, e);
		} catch (IOException e) {
			logger.error("Failed to open " + uri, e);
		} catch (ParserConfigurationException e) {
			logger.error("Failed to parse " + uri, e);
		} catch (SAXException e) {
			logger.error("Failed to parse " + uri, e);
		}
		return doc;
	}	
	
	public static Element getFirstChildNode(Element node, String name){
		NodeList nodes = node.getElementsByTagName(name);
		if (nodes.getLength() == 0)
			return null;
		else
			return (Element)nodes.item(0);
	}
	
	public static String getNodeText(Element node) {
		if(node == null)
			return null;

		NodeList nodes = node.getChildNodes();
		for (int i=0; i<nodes.getLength(); i++) {
			Node candidate = nodes.item(i);
			if (candidate.getNodeType() == Node.TEXT_NODE)
				return candidate.getNodeValue();
		}
		return null;
	}
	
	/**
	 * Reads the node attribute and resolves variables ${VARIABLE} if present, 
	 * looking first for thread local property, second for system property, third for environment variable.
	 * Returns an empty string if not found
	 */
	public static String getAttributeValue(Element node, String name){
		String value;
		
		value = node.getAttribute(name);
		
		return resolveValue(value);
	}
	
	/**
	 * Creates a duration from an ISO-8601 date. Examples : 10m, 1H30M, 30s.
	 * The "PT" prefix is optional. Any contained variable is expanded first.
	 * See https://docs.oracle.com/javase/8/docs/api/java/time/Duration.html#parse-java.lang.CharSequence-.
	 */
	public static Duration getAttributeDuration(Element node, String attribute){
		String value = ConfigUtil.getAttributeValue(node, attribute);
		return parseDuration(value);
	}
	
	/**
	 * Creates a duration from an ISO-8601 date. Examples : 10m, 1H30M, 30s.
	 * The "PT" prefix is optional. Any contained variable is expanded first.
	 * See https://docs.oracle.com/javase/8/docs/api/java/time/Duration.html#parse-java.lang.CharSequence-.
	 */
	public static Duration parseDuration(String value){
		if (value == null || value.isEmpty())
			return null;
		
		// digits only, convert it in seconds by default
		Integer testValue = Ints.tryParse(value);
		if (testValue != null){
			if (testValue != -1)
				logger.info("Time value given without ISO-8601 time unit : " + testValue + ". Defaulting to time unit in seconds.");
			value += "s";
		}
	
		if (!value.startsWith(ISO_8601_DURATION_PREFIX))
			value = ISO_8601_DURATION_PREFIX + value;
		
		try{
			return Duration.parse(value);
		}catch(DateTimeParseException ex){
			logger.error("Failed to parse the given time value : " + value + "  Time value is not ISO-8601 compliant. Note that Jeyzer adds the ISO PT prefix if not present. See https://docs.oracle.com/javase/8/docs/api/java/time/Duration.html");
			return null;
		}
	}
	
	public static String resolveVariable(String value){
		return resolveVariable(value, true);
	}
	
	public static boolean isVariableUnresolved(String value){
		return value.startsWith(VARIABLE_PREFIX) && value.endsWith(VARIABLE_SUFFIX);
	}
	
	/**
	 * Resolves the variables ${VARIABLE} if present, 
	 * looking first for thread local property, second for system property, third for environment variable.
	 */
	public static String resolveVariable(String value, boolean emitWarning){

		if (value !=null && isVariableUnresolved(value)){
			String variable = value.substring(2, value.length()-1);
			String resolvedValue;
			
			// exotic case, return ${}
			if (variable.length()==0)
				return value;
			
			resolvedValue = ConfigThreadLocal.get(variable);
			if (resolvedValue != null)
				return resolveInnerVariable(resolvedValue, value, VAR_TYPE_THREAD_LOCAL);
			
			resolvedValue = System.getProperty(variable);
			if (resolvedValue != null)
				return resolveInnerVariable(resolvedValue, value, VAR_TYPE_SYSTEM_PROPERTY);
			
			resolvedValue = System.getenv(variable);
			if (resolvedValue != null)
				return resolveInnerVariable(resolvedValue, value, VAR_TYPE_ENV_VARIABLE);				
			if (emitWarning)
				logger.warn("Variable {} cannot be resolved. Returning variable name.", value.replace('\\', '/'));
		}
		
		return value;
	}

	private static String resolveInnerVariable(String resolvedValue, String originalValue, String type){
		String result = resolvedValue;
		if (logger.isDebugEnabled())
			logger.debug("Variable \"{}\" resolved through {}. Value : {}", originalValue, type, resolvedValue.replace('\\', '/'));
		if (resolvedValue.contains(VARIABLE_PREFIX)){
			if (logger.isDebugEnabled())
				logger.debug("Resolving inner variable for value \"{}\".", resolvedValue.replace('\\', '/'));
			result = resolveValue(resolvedValue);
			logger.debug("Inner variable resolved");
		}
		return result;
	}
	
	/**
	 * Resolves the variables ${VARIABLE} if present, 
	 * looking first for thread local property, second for system property, third for environment variable.
	 */
	public static String resolveValue(String value){
		StringBuilder resolvedValue = new StringBuilder(10);
		int end = 0;
		int pos = 0;
		int prev = 0;
		
		while(pos != -1){
			pos = value.indexOf(VARIABLE_PREFIX, pos);
			
			if (pos != -1){
				// get the beginning
				resolvedValue.append(value.substring(prev, pos));
								
				end = value.indexOf(VARIABLE_SUFFIX, pos);
				if (end == -1){
					logger.warn("Incomplete variable definition");
					resolvedValue.append(value.substring(pos));
					return resolvedValue.toString();
				}
				else{
					resolvedValue.append(resolveVariable(value.substring(pos, end+1)));
				}
				
				prev = end +1;
				end++;
				pos = end;
			}else {
				// end reached
				resolvedValue.append(value.substring(end, value.length()));
			}
			
		}
		
		if (end != 0 && logger.isDebugEnabled())
			logger.debug("Value loaded \"{}\" mapped to \"{}\"", value, resolvedValue.toString().replace('\\', '/'));
		
		return resolvedValue.toString();
	}

	public static void validate(Properties props, String[] params) throws JzrInitializationException{
		for (int i=0;i<params.length;i++){
			String param = params[i];
			if (!props.containsKey(param))
				throw new JzrInitializationException("Missing parameter  : " + param);
			if (props.getProperty(param) == null)
				throw new JzrInitializationException("Missing parameter value for param : " + param);
		}
		
	}
	
	public static Properties loadDevVariables(String jzrDevFilePath, String devPropFile){
		Properties config = null;
		
		if (jzrDevFilePath==null || jzrDevFilePath.isEmpty()){
			// for dev environment, load config.properties
			String configPropsPath = System.getProperty("user.dir") + "/config/" + devPropFile;

			logger.info("Configuration file path not set on the command line. Defaulting to the dev config.properties : " 
					+ SystemHelper.sanitizePathSeparators(configPropsPath));
			
			File configPropsFile = new File(configPropsPath);
			if (! configPropsFile.exists()){
				logger.warn("Development config.properties file {} not found.", 
						SystemHelper.sanitizePathSeparators(configPropsPath));
				return null;
			}
			
			config = loadPropertyFile(configPropsFile);
			
			// propagate the variable for later resolution
			createSystemProperties(config);
			
			// propagate the variable for later resolution
			resolveProperties(config);
		}
		
		return config;
	}	

	private static void createSystemProperties(Properties config){
		Enumeration<?> e = config.propertyNames();

		while (e.hasMoreElements()) {
			String key = (String) e.nextElement();
			System.setProperty(key, config.getProperty(key));
		}
	}

	private static void resolveProperties(Properties config){
		Enumeration<?> e = config.propertyNames();

		while (e.hasMoreElements()) {
			String key = (String) e.nextElement();
			config.setProperty(key, ConfigUtil.resolveValue(config.getProperty(key)));
		}		
	}
	
	/*
	 * Test
	 */
//	public static void main(String[] args) {
//		String result;
//		
//		System.setProperty("China", "Pekin");
//		System.setProperty("France", "Paris");
//		System.setProperty("Corea", "Seoul");
//
//		System.out.println("==========================");
//		String test = "Hello world";
//		System.out.println(test);
//		result = resolveValue(test);
//		System.out.println(result);
//		System.out.println("==========================");
//
//		System.out.println("==========================");
//		test = "${China} Hello world";
//		System.out.println(test);
//		result = resolveValue(test);
//		System.out.println(result);
//		System.out.println("==========================");
//
//		System.out.println("==========================");
//		test = "Hello world ${China}";
//		System.out.println(test);
//		result = resolveValue(test);
//		System.out.println(result);	
//		System.out.println("==========================");
//
//		System.out.println("==========================");
//		test = "Hello ${China} world";
//		System.out.println(test);
//		result = resolveValue(test);
//		System.out.println(result);		
//		System.out.println("==========================");
//
//		System.out.println("==========================");
//		test = "Hello ${China}${France} world ${Corea}";
//		System.out.println(test);
//		result = resolveValue(test);
//		System.out.println(result);
//		System.out.println("==========================");
//
//		System.out.println("==========================");
//		test = "${China} Hello ${France} world ${Corea}";
//		System.out.println(test);
//		result = resolveValue(test);
//		System.out.println(result);		
//		System.out.println("==========================");
//
//		System.out.println("==========================");
//		test = "Hello ${China}${France} world ${}";
//		System.out.println(test);
//		result = resolveValue(test);
//		System.out.println(result);
//		System.out.println("==========================");
//
//		System.out.println("==========================");
//		test = "Hello ${China${France} world ${}";
//		System.out.println(test);
//		result = resolveValue(test);
//		System.out.println(result);
//		System.out.println("==========================");
//		
//		System.out.println("==========================");
//		test = "";
//		System.out.println(test);
//		result = resolveValue(test);
//		System.out.println(result);
//		System.out.println("==========================");
//		
//		System.out.println("==========================");
//		test = "${";
//		System.out.println(test);
//		result = resolveValue(test);
//		System.out.println(result);
//		System.out.println("==========================");
//				
//	}	
	
	
}
