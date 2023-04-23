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




import org.jeyzer.analyzer.data.DiskSpaceInfo;
import org.jeyzer.analyzer.data.DiskSpaces;
import org.jeyzer.analyzer.data.ThreadDump;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.primitives.Longs;

public class DiskSpaceBeanInfoParser {

	public static final String DISK_SPACE_PREFIX = "disk space:";
	public static final String DIRECTORY_FIELD = "directory";
	public static final String FREE_SPACE_FIELD = "free_space";
	public static final String USED_SPACE_FIELD = "used_space";
	public static final String TOTAL_SPACE_FIELD = "total_space";
	public static final String DISK_SPACE_SEPARATOR = ":";
	public static final String DISK_SPACE_EQUALS = "\t";
	
	private static final Logger logger = LoggerFactory.getLogger(DiskSpaceBeanInfoParser.class);
	
	public void parse(ThreadDump dump, String line) {
		// Jz>	disk space:work:directory	C:\demo-pg\jeyzer-recorder\demo\bin
		// Jz>	disk space:work:free_space	52226626
		// Jz>	disk space:work:used_space	26666625
		// Jz>	disk space:work:total_space	78893251
		String name = parseToken(line, DISK_SPACE_PREFIX, DISK_SPACE_SEPARATOR);
		if (name == null)
			return;
		
		DiskSpaces diskSpaces = dump.getDiskSpaces();
		DiskSpaceInfo info = diskSpaces.getDiskSpace(name);
		
		if (info == null){
			info = new DiskSpaceInfo(name);
			diskSpaces.addDiskSpaceInfo(name, info);
		}
		
		String diskSpacePrefix = DISK_SPACE_PREFIX + name + DISK_SPACE_SEPARATOR;
		
		String fieldName = parseToken(line, diskSpacePrefix, DISK_SPACE_EQUALS);
		String fieldValue = parseValue(line);
		if (DIRECTORY_FIELD.equals(fieldName)){
			info.setDirectory(fieldValue);
		}
		else if (FREE_SPACE_FIELD.equals(fieldName)){
			long value = convertValue(fieldValue);
			info.setFreeSpace(value);
		}
		else if (USED_SPACE_FIELD.equals(fieldName)){
			long value = convertValue(fieldValue);
			info.setUsedSpace(value);
		}
		else if (TOTAL_SPACE_FIELD.equals(fieldName)){
			long value = convertValue(fieldValue);
			info.setTotalSpace(value);
		}
		else{
			logger.warn("Failed to interpret disk space line : {}", line);
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
	
	private String parseToken(String line, String prefix, String suffix) {
		int startPos = prefix.length();
		int endPos = line.indexOf(suffix, startPos);
		if (endPos == -1){
			logger.warn("Failed to read disk space info on line : {}", line);
			return null;
		}
		return line.substring(startPos, endPos);
	}
	
	private String parseValue(String line) {
		int posStart = line.indexOf(DISK_SPACE_EQUALS) + 1;
		String value = line.substring(posStart);
		if (value == null)
			logger.warn("Failed to read disk space info value : {}", line.substring(posStart));
		return value;
	}

}
