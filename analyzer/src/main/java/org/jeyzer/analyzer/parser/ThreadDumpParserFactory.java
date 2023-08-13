package org.jeyzer.analyzer.parser;

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
import java.util.ArrayList;
import java.util.List;

import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.analyzer.error.JzrParsingException;
import org.jeyzer.analyzer.setup.JzrSetupManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadDumpParserFactory {

	// logger
	private static final Logger logger = LoggerFactory.getLogger(ThreadDumpParserFactory.class);
	
	private static final String LOG_FORMAT_DETECTED_PREFIX = "Thread dump format detected : ";
	
	private static final int LINES_LIMIT = 8;

	private ThreadDumpParserFactory() {
	}
	
	// factory method
	public static ThreadDumpParser getThreadDumpParser(JzrSetupManager setupMgr, File file) throws JzrInitializationException, JzrParsingException {
		logger.debug("Opening thread dump file {} to determine the thread dump parser to use.",file.getName());

		ThreadDumpParser parser = null;
		List<String> lines = new ArrayList<>(LINES_LIMIT);		
		
		try (
				FileReader fr = new FileReader(file);
				BufferedReader reader = new BufferedReader(fr);
			)
		{
			// Read 8 lines max
			for (int i = 0; i<LINES_LIMIT; i++){
				String line = reader.readLine();
				if (line == null)
					break;
				lines.add(line);
			}
			
			// let's try Agent advanced format : 1st line starts with "Full Agent Java Advanced thread dump"
			if (!lines.isEmpty() && lines.get(0).startsWith(AdvancedAgentStackParser.FIRST_LINE)) {
				logger.info(LOG_FORMAT_DETECTED_PREFIX + AdvancedAgentStackParser.FORMAT_SHORT_NAME);
				parser = new AdvancedAgentStackParser(setupMgr);
				return parser;
			}
			
			// let's try JFR format : 1st line starts with "JFR Recording"
			if (!lines.isEmpty() && lines.get(0).startsWith(JFRStackParser.FIRST_LINE)) {
				logger.info(LOG_FORMAT_DETECTED_PREFIX + JFRStackParser.FORMAT_SHORT_NAME);
				parser = new JFRStackParser(setupMgr);
				return parser;
			}
			
			// let's try JCMD format : 1st line starts with process id
			if (!lines.isEmpty() && lines.get(0).matches("\\d+(\\.\\d+)?")) {
				logger.info(LOG_FORMAT_DETECTED_PREFIX + JFRStackParser.FORMAT_SHORT_NAME);
				parser = new JcmdParser(setupMgr);
				return parser;
			}
			
			// let's try JMX advanced format : 1st line starts with "Full Java Advanced thread dump"
			if (!lines.isEmpty() && lines.get(0).startsWith(AdvancedJMXStackParser.FIRST_LINE)) {
				logger.info(LOG_FORMAT_DETECTED_PREFIX + AdvancedJMXStackParser.FORMAT_SHORT_NAME);
				parser = new AdvancedJMXStackParser(setupMgr);
				return parser;
			}

			// let's try JStack format : 2nd line or 5th line (if JZR header) starts with : "Full thread dump"
			//   and Jstack 15 format starts also with "Full thread dump" (lines 3 or 6)
			if (isJstackPatternDetected(lines)) {
				// Now detect Jstack 15 format
				if (isJstack15PatternDetected(lines)) {
					logger.info(LOG_FORMAT_DETECTED_PREFIX + Jstack15Parser.FORMAT_SHORT_NAME);
					parser = new Jstack15Parser();
				}
				else {
					logger.info(LOG_FORMAT_DETECTED_PREFIX + JstackParser.FORMAT_SHORT_NAME);
					parser = new JstackParser();
				}
				return parser;
			}
			
			// let's try JStack hung format : 1st line line or 5th line starts with : "Deadlock Detection:"
			if ((!lines.isEmpty() && lines.get(0).startsWith(JstackHungParser.FIRST_OR_FIFTH_LINE)) // Linux
					|| (lines.size()>=4 && lines.get(3).startsWith(JstackHungParser.FIRST_OR_FIFTH_LINE)) // JZR variant - Linux
					|| (lines.size()>=5 && lines.get(4).startsWith(JstackHungParser.FIRST_OR_FIFTH_LINE)) // Windows
					|| (lines.size()>=8 && lines.get(7).startsWith(JstackHungParser.FIRST_OR_FIFTH_LINE)) // JZR variant - Windows
					) {
				logger.info(LOG_FORMAT_DETECTED_PREFIX + JstackHungParser.FORMAT_SHORT_NAME);
				parser = new JstackHungParser();
				return parser;
			}
			
			// let's try JMX format : 1st line starts with "Full Java thread dump"
			if (!lines.isEmpty() && lines.get(0).startsWith(JMXStackParser.FIRST_LINE)) {
				logger.info(LOG_FORMAT_DETECTED_PREFIX + JMXStackParser.FORMAT_SHORT_NAME);
				parser = new JMXStackParser();
				return parser;
			}
			
			// let's try JRockit Mission Control format : 3nd line and 5th lines have ===== FULL THREAD DUMP =============== and Oracle JRockit
			if (isJRockitDetected(lines)){
				logger.info(LOG_FORMAT_DETECTED_PREFIX + JRockitParser.FORMAT_SHORT_NAME);
				parser = new JRockitParser();
				return parser;
			}
			
			// Let's try TDA format : first line start with a stack header
			if (!lines.isEmpty() && lines.get(0).startsWith(TDASimpleParser.HEADER_START)){
				logger.info(LOG_FORMAT_DETECTED_PREFIX + TDASimpleParser.FORMAT_SHORT_NAME);
				parser = new TDASimpleParser();
				return parser;
			}
			
			// let's try IBM core format : 4th line starts with : "1TISIGINFO"
			if (lines.size()>=4 && lines.get(3).startsWith(IBMCoreParser.FOURTH_LINE)) {
				logger.info(LOG_FORMAT_DETECTED_PREFIX + IBMCoreParser.FORMAT_SHORT_NAME);
				parser = new IBMCoreParser();
				return parser;
			}
			
			// Let's try Instana format : first line start with a stack header without quotes (so non TDA parser)
			if (!lines.isEmpty() && !lines.get(0).startsWith(TDASimpleParser.HEADER_START)){
				logger.info(LOG_FORMAT_DETECTED_PREFIX + InstanaParser.FORMAT_SHORT_NAME);
				parser = new InstanaParser();
				return parser;
			}
		} catch (FileNotFoundException ex) {
			logger.error("Failed to open " + file.getAbsolutePath(), ex);
			throw new JzrInitializationException("Failed to open " + file.getAbsolutePath());
		} catch (Exception e) {
			logger.error("Failed to create thread dump format parser based on file : {}", file.getAbsoluteFile(), e);
			throw new JzrInitializationException("Failed to create thread dump format parser based on file : " + file.getName(), e);
		}
		
		logger.error("Failed to detect thread dump format based on file : {}", file.getAbsoluteFile());
		logger.error("File header extract :");
		for (String line :lines){
			logger.error(line);	
		}
		throw new JzrParsingException("Thread dump format not recognized. Based on file : " + file.getName());
	}

	private static boolean isJRockitDetected(List<String> lines) {
		boolean jRockitIndicator1 = false;
		boolean jRockitIndicator2 = false;
		for (int i=0; i < lines.size(); i++) {
			if (lines.get(i).startsWith(JRockitParser.INDICATOR_UPPER_CASE_THREAD_DUMP))
				jRockitIndicator1 = true;
			else if (lines.get(i).startsWith(JRockitParser.INDICATOR_ORACLE_JROCKIT))
				jRockitIndicator2 = true;
		}
		return jRockitIndicator1 && jRockitIndicator2;
	}

	private static boolean isJstack15PatternDetected(List<String> lines) {
		for (String line : lines)
			if (line != null && line.contains(Jstack15Parser.ID_TAG))
				return true;
		return false;
	}

	private static boolean isJstackPatternDetected(List<String> lines) {
		return (lines.size()>=2 && lines.get(1).startsWith(JstackParser.TD_HEADER_PATTERN))
		   || (lines.size()>=3 && lines.get(2).startsWith(Jstack15Parser.TD_HEADER_PATTERN))
		   || (lines.size()>=5 && lines.get(4).startsWith(JstackParser.TD_HEADER_PATTERN))
		   || (lines.size()>=6 && lines.get(5).startsWith(Jstack15Parser.TD_HEADER_PATTERN));
	}

}
