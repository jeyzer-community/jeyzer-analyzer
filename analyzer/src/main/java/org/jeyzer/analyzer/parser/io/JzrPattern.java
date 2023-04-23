package org.jeyzer.analyzer.parser.io;

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



import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JzrPattern extends SnapshotFileNamePattern{

	private static final Logger logger = LoggerFactory.getLogger(JzrPattern.class);	
	
	public static final String JZR_DESCRIPTION = "JZR snapshot";
	
	public static final String JZR_FILE_DATE_FORMAT_NO_TIME_ZONE = "yyyy-MM-dd---HH-mm-ss-SSS";
	
	private static final String JZR_FILE_NAME_REGEX = "snap\\-(jzr|c|p|jfr)\\-(.*).jzr$"; 	
	private static final String JZR_TIME_ZONE_REGEX = "snap\\-(jzr|c|p|jfr)\\-\\d\\d\\d\\d-\\d\\d-\\d\\d---\\d\\d-\\d\\d-\\d\\d-\\d\\d\\d-(.*).jzr$"; 
	
	private Pattern regex = Pattern.compile(JZR_FILE_NAME_REGEX);	
	private Pattern timeZoneRegex = Pattern.compile(JZR_TIME_ZONE_REGEX);
	
	public JzrPattern(String pattern) {
		super(pattern);
	}

	@Override
	public boolean accept(String name) {
		Matcher matcher = regex.matcher(name);
		if (matcher.find()){
			logger.debug("JzrPattern matched - file named : {}", name);
			return true;
		}
		else{
			logger.debug("JzrPattern not matched - file named : {}", name);
			return false;
		}
	}
	
	@Override
	public String getDateFormat(){
		return JZR_FILE_DATE_FORMAT_NO_TIME_ZONE;
	}
	
	public String extractDateSection(String name){
		Matcher matcher = regex.matcher(name);
		if (!matcher.find()){
			logger.error("Failed to extract date from file name. JzrPattern {} not matched - file named : {}", JZR_FILE_NAME_REGEX, name);
			return null;
		}
		
		String dateSection = matcher.group(2);
		if (dateSection == null){
			logger.error("Failed to extract date from file name : {}. Date must be provided as part of the 2nd group of {}", name, JZR_FILE_NAME_REGEX);
			return null;
		}
			
		// Put back any original char
		return dateSection.replace('@', ':').replace('$', '/');
	}

	public String extractTimeZoneOriginSection(String name){
		Matcher matcher = regex.matcher(name);
		if (!matcher.find()){
			logger.error("Failed to extract time zone origin from file name. JzrPattern {} not matched - file named : {}", JZR_FILE_NAME_REGEX, name);
			return null;
		}
		
		String timeZoneOriginSection = matcher.group(1);
		if (timeZoneOriginSection == null){
			logger.error("Failed to extract time zone origin from file name : {}. Time zone origin must be provided as part of the 1st group of {}", name, JZR_FILE_NAME_REGEX);
			return null;
		}
			
		return timeZoneOriginSection;
	}
	
	public String extractTimeZoneIdSection(String name){
		Matcher matcher = timeZoneRegex.matcher(name);
		if (!matcher.find()){
			logger.error("Failed to extract time zone id from file name. JzrPattern {} not matched - file named : {}", JZR_TIME_ZONE_REGEX, name);
			return null;
		}
		
		String timeZoneIdSection = matcher.group(2);
		if (timeZoneIdSection == null){
			logger.error("Failed to extract time zone id from file name : {}. Time zone id must be provided as part of the 1st group of {}", name, JZR_TIME_ZONE_REGEX);
			return null;
		}
		
		// Put back any original char
		return timeZoneIdSection.replace('@', ':').replace('$', '/');
	}

	@Override
	public String getDescriptor() {
		return JZR_DESCRIPTION;
	}
}
