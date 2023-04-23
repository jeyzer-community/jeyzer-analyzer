package org.jeyzer.analyzer.parser.advanced;

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




import org.jeyzer.analyzer.data.ThreadDump;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.primitives.Longs;

public class DiskWriteBeanInfoParser {

	public static final String DISK_WRITE_PREFIX = "disk write:";
	public static final String TIME_FIELD = "prev:time";
	public static final String SIZE_FIELD = "prev:size";
	public static final String DISK_SPACE_EQUALS = "\t";
	
	private static final Logger logger = LoggerFactory.getLogger(DiskWriteBeanInfoParser.class);
	
	public void parse(ThreadDump dump, String line) {
		// Jz>	disk write:prev:time	5
		// Jz>	disk write:prev:size	14812
		
		String fieldValue = parseValue(line);
		if (line.startsWith(DISK_WRITE_PREFIX + TIME_FIELD)){
			long value = convertValue(fieldValue);
			dump.setWriteTime(value);
		}
		else if (line.startsWith(DISK_WRITE_PREFIX + SIZE_FIELD)){
			long value = convertValue(fieldValue);
			dump.setWriteSize(value);
		}
		else{
			logger.warn("Failed to interpret disk write line : {}", line);
		}
	}
	
	private long convertValue(String text) {
		Long value = Longs.tryParse(text);
		if (value == null){
			logger.warn("Failed to convert disk space value : {}", text);
			value = -1L;
		}
		return value;
	}
	
	private String parseValue(String line) {
		int posStart = line.indexOf(DISK_SPACE_EQUALS) + 1;
		String value = line.substring(posStart);
		if (value == null)
			logger.warn("Failed to read disk write info value : {}", line.substring(posStart));
		return value;
	}
}
