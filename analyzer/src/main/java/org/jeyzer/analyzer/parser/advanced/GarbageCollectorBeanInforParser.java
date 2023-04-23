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
import org.jeyzer.analyzer.data.gc.GarbageCollection;
import org.jeyzer.analyzer.data.gc.GarbageCollectorInfo;
import org.jeyzer.analyzer.setup.GarbageCollectorSetupManager;
import org.jeyzer.analyzer.setup.MemoryPoolSetupManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.primitives.Longs;

public class GarbageCollectorBeanInforParser {

	public static final String GARBAGE_COLLECTOR_PREFIX = "gc:";
	public static final String GARBAGE_COLLECTOR_COUNT = ":count";
	public static final String GARBAGE_COLLECTOR_TIME = ":time";
	public static final String GARBAGE_COLLECTOR_LAST_GC_ID = ":last gc:id";
	public static final String GARBAGE_COLLECTOR_LAST_GC_DURATION = ":last gc:duration";
	public static final String GARBAGE_COLLECTOR_LAST_GC_START_TIME = ":last gc:start_time";
	public static final String GARBAGE_COLLECTOR_LAST_GC_END_TIME = ":last gc:end_time";
	public static final String GARBAGE_COLLECTOR_LAST_GC_BEFORE = ":last gc:before:";
	public static final String GARBAGE_COLLECTOR_LAST_GC_AFTER = ":last gc:after:";
	
	public static final char GARBAGE_COLLECTOR_SEPARATOR = ':';
	public static final char GARBAGE_COLLECTOR_EQUALS = '\t';
	
	private static final Logger logger = LoggerFactory.getLogger(GarbageCollectorBeanInforParser.class);
	
	private GarbageCollectorSetupManager gcSetupManager;
	private MemoryPoolSetupManager memPoolSetupManager;
	
	public GarbageCollectorBeanInforParser(GarbageCollectorSetupManager garbageCollectorSetupManager, MemoryPoolSetupManager memoryPoolSetupManager){
		this.gcSetupManager = garbageCollectorSetupManager;
		this.memPoolSetupManager = memoryPoolSetupManager;
	}
	
	public void parse(ThreadDump dump, String line) {
		String gcName;
		line = line.substring(GARBAGE_COLLECTOR_PREFIX.length());
		
		// J#>	gc:PS Scavenge:count	17
		// J#>	gc:PS Scavenge:time	119
		// J#>	gc:PS Scavenge:last gc:id	17
		// J#>	gc:PS Scavenge:last gc:duration	3
		// J#>	gc:PS Scavenge:last gc:start_time	92104
		// J#>	gc:PS Scavenge:last gc:end_time	92107
		// J#>	gc:PS Scavenge:last gc:before:PS Old Gen:usage:used	12325480
		// J#>	gc:PS Scavenge:last gc:before:PS Eden Space:usage:used	3670016
		// J#>	gc:PS Scavenge:last gc:after:PS Old Gen:usage:used	13227472
		// J#>	gc:PS Scavenge:last gc:after:PS Eden Space:usage:used	0		
		
		gcName = parseGarbageCollectorName(line);
		if (gcName == null)
			return;
		
		GarbageCollection gc = dump.getGarbageCollection();
		GarbageCollectorInfo gcInfo = gc.getGarbageCollectorInfo(gcName);
		if (gcInfo == null){
			gcInfo = gc.addGarbageCollectorInfo(gcName, memPoolSetupManager);
			if (this.gcSetupManager.isOldGarbageCollector(gcName))
				gc.setOldGarbageCollectorInfo(gcInfo);
			else if (this.gcSetupManager.isYoungGarbageCollector(gcName))
				gc.setYoungGarbageCollectorInfo(gcInfo);
		}

		line = line.substring(gcName.length());
		parseInfo(gcInfo, line);
	}
	
	private void parseInfo(GarbageCollectorInfo gcInfo, String line) {

		if (line.contains(GARBAGE_COLLECTOR_COUNT)){
			long value = parseValue(line);
			gcInfo.setTotalCollectionCount(value);
		}
		else if (line.contains(GARBAGE_COLLECTOR_TIME)){
			long value = parseValue(line);
			gcInfo.setTotalCollectionTime(value);
		}
		else if (line.contains(GARBAGE_COLLECTOR_LAST_GC_ID)){
			long value = parseValue(line);
			gcInfo.setLastGCId(value);
		}
		else if (line.contains(GARBAGE_COLLECTOR_LAST_GC_START_TIME)){
			long value = parseValue(line);
			gcInfo.setLastGCStartTime(value);
		}
		else if (line.contains(GARBAGE_COLLECTOR_LAST_GC_END_TIME)){
			long value = parseValue(line);
			gcInfo.setLastGCEndTime(value);
		}
		else if (line.contains(GARBAGE_COLLECTOR_LAST_GC_DURATION)){
			long value = parseValue(line);
			gcInfo.setLastGCDuration(value);
		}
		else if (line.contains(GARBAGE_COLLECTOR_LAST_GC_BEFORE)){
			line = line.substring(GARBAGE_COLLECTOR_LAST_GC_BEFORE.length());
			String category = parseCategory(line);
			long value = parseValue(line);
			gcInfo.addBeforeMemoryPoolEntry(category, value);
		}
		else if (line.contains(GARBAGE_COLLECTOR_LAST_GC_AFTER)){
			line = line.substring(GARBAGE_COLLECTOR_LAST_GC_AFTER.length());
			String category = parseCategory(line);
			long value = parseValue(line);
			gcInfo.addAfterMemoryPoolEntry(category, value);
		}
		else {
			logger.warn("Garbage collector line is unknown : {}", line);
		}
	}

	private String parseGarbageCollectorName(String line) {
		int endPos = line.indexOf(GARBAGE_COLLECTOR_SEPARATOR);
		if (endPos == -1){
			logger.warn("Failed to parse the garbage collector name on line : {}", line);
			return null;
		}
		return line.substring(0, endPos);
	}
	
	private String parseCategory(String line) {
		int endPos = line.indexOf(GARBAGE_COLLECTOR_EQUALS);
		if (endPos == -1){
			logger.warn("Failed to read garbage collector pool memory category on line : {}", line);
			return null;
		}
		return line.substring(0, endPos);
	}
	
	private long parseValue(String line) {
		int posStart = line.indexOf(GARBAGE_COLLECTOR_EQUALS) + 1;
		
		Long value = Longs.tryParse(line.substring(posStart));
		if (value == null){
			logger.warn("Failed to convert garbage collector value : {}", line.substring(posStart));
			value = -1L;
		}
		
		return value;
	}
}
