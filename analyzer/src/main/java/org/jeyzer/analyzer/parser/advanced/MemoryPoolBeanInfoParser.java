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
import org.jeyzer.analyzer.data.memory.MemoryPools;
import org.jeyzer.analyzer.setup.MemoryPoolSetupManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.primitives.Longs;

public class MemoryPoolBeanInfoParser {

	public static final String MEMORY_PREFIX = "memory:";
	public static final String MEMORY_POOL_PREFIX = "mem pool:";
	public static final char MEMORY_POOL_SEPARATOR = ':';
	public static final char MEMORY_POOL_EQUALS = '\t';
	
	private static final Logger logger = LoggerFactory.getLogger(MemoryPoolBeanInfoParser.class);
	
	private MemoryPoolSetupManager memPoolSetup;
	
	public MemoryPoolBeanInfoParser(MemoryPoolSetupManager memoryPoolSetupManager) {
		this.memPoolSetup = memoryPoolSetupManager;
	}

	public void parse(ThreadDump dump, String line, boolean memoryPool) {
		String category;
		Long value;
		
		if (memoryPool)
			// J#>	mem pool:PS Old Gen:usage:used	96286648
			line = line.substring(MemoryPoolBeanInfoParser.MEMORY_POOL_PREFIX.length());
		else
			// J#>	memory:heap:used	5840184
			line = line.substring(MemoryPoolBeanInfoParser.MEMORY_PREFIX.length());
		
		category = parseCategory(line);
		value = parseValue(line);
		
		MemoryPools memPools = dump.getMemoryPools();
		if (category != null){
			memPools.addMemoryPoolEntry(category, value);
			if (memPoolSetup.isOldMemoryPool(category))
				memPools.addOldMemoryPoolEntry(category, value);
			else if (memPoolSetup.isYoungMemoryPool(category))
				memPools.addYoungMemoryPoolEntry(category, value);
		}

	}

	private String parseCategory(String line) {
		int endPos = line.indexOf(MEMORY_POOL_EQUALS);
		if (endPos == -1){
			logger.warn("Failed to read memory category on line : {}", line);
			return null;
		}
		return line.substring(0, endPos);
	}
	
	private long parseValue(String line) {
		int posStart = line.indexOf(MEMORY_POOL_EQUALS) + 1;
		Long value = Longs.tryParse(line.substring(posStart));
		if (value == null){
			logger.warn("Failed to convert memory value : {}", line.substring(posStart));
			value = -1L;
		}
		return value;
	}

}
