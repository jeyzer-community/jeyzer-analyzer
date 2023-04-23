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





import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class TimestampPattern extends SnapshotFileNamePattern {

	private static final Logger logger = LoggerFactory.getLogger(TimestampPattern.class);	
	
	public static final String JZR_DESCRIPTION = "Timestamp format (see profile)";
	
	public static final String DATE_FORMAT_START = "<";
	public static final String DATE_FORMAT_END = ">";
	
	private String prefix;
	private String suffix; 
	private boolean suffixIgnored;
	private SimpleDateFormat sdf;
	
	public TimestampPattern(String pattern, boolean suffixIgnored){
		super(pattern);
		int pos = pattern.indexOf(DATE_FORMAT_START);
		int end = pattern.indexOf(DATE_FORMAT_END);
		this.prefix = pattern.substring(0, pos);
		this.dateFormatpattern = pattern.substring(pos+1, end);
		this.suffix = pattern.substring(end+1);
		// Locale US used to resolve strings like Mon, Jul
		// Thread dump names have to use US locale
		this.sdf = new SimpleDateFormat(dateFormatpattern, Locale.US);
		this.suffixIgnored = suffixIgnored;
	}
	
	@Override
	public boolean accept(String name) {
		boolean match = name.startsWith(prefix) &&
				(!suffixIgnored ? name.endsWith(suffix) : true) &&  // weird : without parenthesis, first test is ignored
				isTimeStampValid(name);
		if (match){
			logger.debug("TimestampPattern {} matched - file named : {}", this.pattern, name);
			return true;
		}
		else{
			logger.debug("TimestampPattern {} not matched - file named : {}", this.pattern, name);
			return false;			
		}
	}

	private boolean isTimeStampValid(String name) {
		String timestamp = extractDateSection(name);
		if (timestamp == null)
			return false;
		
		try {
			sdf.parse(timestamp);
		} catch (ParseException e) {
			return false;
		}
		return true;
	}

	@Override
	public String getDateFormat(){
		return this.dateFormatpattern;
	}
	
	public String extractDateSection(String name){
		if (name.length() < prefix.length() + suffix.length())
			return null;
		
		return name.substring(prefix.length(), name.length()-suffix.length());
	}

	@Override
	public String getDescriptor() {
		return JZR_DESCRIPTION;
	}
}
