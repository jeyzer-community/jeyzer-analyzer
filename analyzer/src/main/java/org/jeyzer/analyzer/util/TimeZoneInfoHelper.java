package org.jeyzer.analyzer.util;

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
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jeyzer.analyzer.data.TimeZoneInfo;
import org.jeyzer.analyzer.data.TimeZoneInfo.TimeZoneOrigin;
import org.jeyzer.analyzer.parser.io.JzrPattern;
import org.jeyzer.analyzer.parser.io.SnapshotFileNameFilter;
import org.jeyzer.analyzer.parser.io.SnapshotFileNamePattern;
import org.jeyzer.analyzer.parser.io.TimestampPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeZoneInfoHelper {

	private static final String DATE_FORMAT_GENERAL_TIME_ZONE_MARKER = "z";
	private static final String DATE_FORMAT_RFC_822_TIME_ZONE_MARKER = "Z";
	private static final String DATE_FORMAT_ISO_8601_TIME_ZONE_MARKER = "X";
	
	public static final Pattern TIME_ZONE_GMT_PATTERN = Pattern.compile("GMT(\\+|-)\\d\\d:\\d\\d");
	
	private static final Logger logger = LoggerFactory.getLogger(TimeZoneInfoHelper.class);
	
	public static TimeZoneInfo getTimeZoneInfo(SnapshotFileNameFilter filter, String fileName){
		if (fileName == null || filter == null)
			return new TimeZoneInfo();
		
		if (fileName.endsWith(ZipHelper.JFR_EXTENSION)) // JFR in zip file
			return new TimeZoneInfo(TimeZoneOrigin.JFR, "UTC");
		
		SnapshotFileNamePattern pattern = filter.getFilePattern(fileName);
		
		if (pattern instanceof JzrPattern){
			return buildTimeZoneInfo((JzrPattern)pattern, fileName);
		}
		else if (pattern instanceof TimestampPattern){
			return buildTimeZoneInfo((TimestampPattern)pattern, fileName);
		}
			
		return new TimeZoneInfo();
	}
	
	public static boolean isValidTimeZone(String candidate) {
		if (candidate == null || candidate.isEmpty())
			return false;
		
		// GMT case  
		// Example : GMT+02:00
		Matcher matcher = TIME_ZONE_GMT_PATTERN.matcher(candidate);
		if (matcher.matches())
			return true;
		
		List<String> ids = Arrays.asList(TimeZone.getAvailableIDs());
		
		if (!ids.contains(candidate)){
			logger.warn("Invalid time zone id provided : {}", candidate);
			return false;
		}
		
		return true;
	}

	private static TimeZoneInfo buildTimeZoneInfo(TimestampPattern tsPattern, String fileName) {
		String format = tsPattern.getDateFormat();
		String dateText = tsPattern.extractDateSection(fileName);
		Date date;
		
		// if format contains time zone info, extract it
		if (format.contains(DATE_FORMAT_GENERAL_TIME_ZONE_MARKER) 
				|| format.contains(DATE_FORMAT_ISO_8601_TIME_ZONE_MARKER)
				|| format.contains(DATE_FORMAT_RFC_822_TIME_ZONE_MARKER)){
			SimpleDateFormat sdf = new SimpleDateFormat(tsPattern.getDateFormat(), Locale.US);
			try {
				date = sdf.parse(dateText);  // this will update the time zone on the parser 
			} catch (ParseException e) {
				logger.error("Failed to parse date for file : " + fileName, e);
				return new TimeZoneInfo();
			}
			
			String zoneCode = buildZoneCode(date, sdf.getTimeZone());
			return new TimeZoneInfo(sdf.getTimeZone(), zoneCode);
		}
		
		return new TimeZoneInfo();
	}

	private static TimeZoneInfo buildTimeZoneInfo(JzrPattern pattern, String fileName) {
		String origin = pattern.extractTimeZoneOriginSection(fileName);
		String timeZoneId = pattern.extractTimeZoneIdSection(fileName);		
		if (timeZoneId != null)
			return new TimeZoneInfo(origin, TimeZone.getTimeZone(timeZoneId), timeZoneId);
		else
			return new TimeZoneInfo();
	}

	private static String buildZoneCode(Date date, TimeZone zone) {
		SimpleDateFormat sdfCode = new SimpleDateFormat("z");
		sdfCode.setTimeZone(zone);
		return sdfCode.format(date); 
	}
}
