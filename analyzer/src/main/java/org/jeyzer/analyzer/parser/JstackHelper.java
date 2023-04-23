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







import java.io.File;

import org.jeyzer.analyzer.error.JzrParsingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.primitives.Longs;

public final class JstackHelper {

	// date as header
	public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	
	private static final String TDG_HEADER_PREFIX = "Full Java thread dump from Jstack";
	private static final String TDG_HEADER_CAPTURE_TIME = "capture time\t";

	private static final Logger logger = LoggerFactory.getLogger(JstackHelper.class);
	
	private JstackHelper(){
	}

	public static final boolean detectTDGHeader(String line){
		return line.startsWith(TDG_HEADER_PREFIX);
	}

	public static final long parseCaptureTime(String line) {
		int posStart = line.indexOf(TDG_HEADER_CAPTURE_TIME) + TDG_HEADER_CAPTURE_TIME.length();

		Long captureTime = Longs.tryParse(line.substring(posStart).trim());
		if (captureTime == null){
			logger.warn("Failed to convert capture time : ", line.substring(posStart));
			captureTime = -1L;
		}
		
		return captureTime;
	}
	
	public static final void checkEmptyThreadDumpVariant(File file, String line) throws JzrParsingException {
		if (line == null) {
			// Variant of empty thread dump :
			// File contains only one error line.
			// Example : "2859: Connection refused"
			// WARNING : not verified under jdk 1.5
			logger.warn("Thread dump file is empty : " + file.getName());
			throw new JzrParsingException("Thread dump file is empty : " + file.getName());
		}
	}
	
}
