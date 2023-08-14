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







import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.jeyzer.analyzer.parser.JRockitParser;
import org.jeyzer.analyzer.parser.JcmdJsonParser;
import org.jeyzer.analyzer.parser.JcmdParser;
import org.jeyzer.analyzer.parser.JstackHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ThreadDumpFileDateHelper {

	private static final Logger logger = LoggerFactory.getLogger(ThreadDumpFileDateHelper.class);	
	
	public static Date getFileDate(SnapshotFileNameFilter filter, File file){
		SnapshotFileNamePattern pattern = filter.getFilePattern(file.getName());
		
		if (pattern instanceof TimestampPattern)
			return getFileDate((TimestampPattern)pattern, file);
		else if (pattern instanceof RegexPattern)
			return getFileDate((RegexPattern)pattern, file);
		else if (pattern instanceof JzrPattern)
			return getFileDate((JzrPattern)pattern, file);		
		else{
			logger.error("Unexpected error. Invalid file pattern. Cannot get thread dump file date for : " + file.getName());
			return null;
		}
	}
	
	private static Date getFileDate(TimestampPattern pattern, File file){
		String filename = file.getName();
		String datePattern = pattern.getDateFormat();
		String dateText = pattern.extractDateSection(filename);
		
		// ex : 2012-07-24---07-00-33
		// ex : Mon Jul  2 12_15_16 UTC 2018    (only US locale)
		SimpleDateFormat sdf = new SimpleDateFormat(datePattern, Locale.US);
		try {
			return sdf.parse(dateText);
		} catch (ParseException e) {
			logger.error("Failed to parse date for file :" + filename, e);
		}
		return null;
	}

	private static Date getFileDate(JzrPattern pattern, File file){
		String filename = file.getName();
		String datePattern = pattern.getDateFormat(); // no time zone
		String dateText = pattern.extractDateSection(filename);
		String timeZoneId = pattern.extractTimeZoneIdSection(filename);
		
		// ex : 2015-06-12---13-40-27-476-CST
		SimpleDateFormat sdf = new SimpleDateFormat(datePattern);
		sdf.setTimeZone(TimeZone.getTimeZone(timeZoneId));
		try {
			return sdf.parse(dateText); // Converted to local date
		} catch (ParseException e) {
			logger.error("Failed to parse date for file :" + filename, e);
		}
		return null;
	}
	
	private static Date getFileDate(RegexPattern pattern, File file){
		// let's read the date from the file content. Assume Jstack or JRockit only.
		Date date = getFileDateFromDump(file);
		if (date != null)
			return date;
		
		return new Date(file.lastModified());
	}	
	
	private static Date getFileDateFromDump(File file){
		try (	
				FileReader fr = new FileReader(file);
				BufferedReader reader = new BufferedReader(fr);
			)
		{
			String filename = file.getName();
			
			// Jstack : First 3 lines --> get date from first line
			/*
			 * 2014-03-18 19:06:00 
			 * Full thread dump Java HotSpot(TM) 64-Bit Server VM (20.4-b02 mixed mode): 
			 * <empty line>
			 */
			String dateText = reader.readLine();
			
			// ex : 2012-07-24 07-00-33
			SimpleDateFormat sdf = new SimpleDateFormat(JstackHelper.DATE_FORMAT);
			try {
				return sdf.parse(dateText);
			} catch (ParseException e) {
				logger.debug("Jstack date not found in file :" + filename);
			}
			
			// Jcmd : First 3 lines --> get date from second line
			// 9844
			// 2023-07-28T11:33:35.254146100Z
			// 20.0.1+9-29
			
			dateText = reader.readLine();
			
			sdf = new SimpleDateFormat(JcmdParser.DATE_FORMAT);
			try {
				if (dateText.length() >= JcmdParser.DATE_FORMAT.length()) {
					// remove the T and the end
					String date = dateText.substring(0,10);
					String time = dateText.substring(11,19);
					return sdf.parse(date + time);
				}
			} catch (ParseException e) {
				logger.debug("Jcmd date not found in file :" + filename);
			}

			// Jstack 1.5 : First 4 lines --> get date from second line
			/*
			 * <empty line> 
			 * 2012-11-29 10:15:46 
			 * Full thread dump Java HotSpot(TM) Server VM (1.5.0_22-b03 mixed mode): 
			 * <empty line>
			 */
			try {
				return sdf.parse(dateText);
			} catch (ParseException e) {
				logger.debug("Jstack 1.5 date not found in file :" + filename);
			}
			
			reader.readLine();
			dateText = reader.readLine();
			// Jcmd Json : get date on line 4
			// {
			//   "threadDump": {
			//     "processId": "47604",
            //	   "time": "2023-08-13T06:28:55.329818700Z",
			if (dateText.contains(JcmdJsonParser.FOURTH_LINE)) {
				// remove the T and the end
				String date = dateText.substring(13,23);
				String time = dateText.substring(24,32);
				return sdf.parse(date + time);	
			}
			
			// JRockit : date on line 4
			/*
			 * 17625:
			 * <empty line>
			 * ===== FULL THREAD DUMP ===============
			 * Fri Feb 12 13:59:23 2021
			 */
			sdf = new SimpleDateFormat(JRockitParser.DATE_FORMAT, Locale.US);
			try {
				return sdf.parse(dateText);
			} catch (ParseException ex) {
				logger.debug("Failed to parse JRockit date in file :" + filename);
			}
			
			return null;
			
		} catch (FileNotFoundException ex) {
			logger.info("Failed to open " + file.getAbsolutePath(), ex);
			return null;			
		} catch (Exception e) {
			logger.info("Failed to read date from thread dump {}", file.getName());
			return null;
		}
	}
}
