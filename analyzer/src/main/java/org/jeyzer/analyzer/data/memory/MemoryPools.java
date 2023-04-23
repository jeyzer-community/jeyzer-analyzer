package org.jeyzer.analyzer.data.memory;

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



import static org.jeyzer.analyzer.math.FormulaHelper.DOUBLE_TO_LONG_MINUS_TWO;





import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jeyzer.analyzer.math.FormulaHelper;

public class MemoryPools {

	private static final String MEMORY_POOL_CATEGORY_USED_SUFFIX = "usage:used";
	private static final String MEMORY_POOL_CATEGORY_MAX_SUFFIX = "usage:max";
	
	public static final String G1_EDEN_SPACE_POOL_MAX_USAGE = "G1 Eden Space:usage:max";
	
	private Map<String, Long> allMemoryPoolEntries = new HashMap<>(10);

	private Long oldUsageUsed;
	private Long oldUsageMax;
	private Long youngUsageUsed;
	private Long youngUsageMax;
	
	// G1 Eden max is never set
	protected boolean youngUsageMaxOptional = false;

	//computed when accessed
	private double heapUsedPercent = -2;
	
	private String oldPoolName;
	private String youngPoolName;
	
	public String getOldPoolName() {
		return oldPoolName;
	}

	public String getYoungPoolName() {
		return youngPoolName;
	}

	public void addMemoryPoolEntry(String category, Long value){
		allMemoryPoolEntries.put(category, value);
	}

	public void addOldMemoryPoolEntry(String category, Long value) {
		if (category.endsWith(MEMORY_POOL_CATEGORY_USED_SUFFIX))
			// ex : mem pool:PS Old Gen:usage:used
			this.oldUsageUsed = value;
		else if (category.endsWith(MEMORY_POOL_CATEGORY_MAX_SUFFIX))
			// ex : mem pool:PS Old Gen:usage:max
			this.oldUsageMax = value;
		if (oldPoolName == null)
			oldPoolName = category.substring(0, category.indexOf(':'));  // ex : PS Old Gen:usage:used
	}

	public void addYoungMemoryPoolEntry(String category, Long value) {
		if (G1_EDEN_SPACE_POOL_MAX_USAGE.equalsIgnoreCase(category))
			this.youngUsageMaxOptional = true;
		if (category.endsWith(MEMORY_POOL_CATEGORY_USED_SUFFIX))
			// ex : mem pool:PS Eden Space:usage:used
			this.youngUsageUsed = value;
		else if (category.endsWith(MEMORY_POOL_CATEGORY_MAX_SUFFIX))
			// ex : mem pool:PS Eden Space:usage:max
			this.youngUsageMax = value;
		if (youngPoolName == null)
			youngPoolName = category.substring(0, category.indexOf(':'));  // ex : PS Old Gen:usage:used
	}
	
	public Long getMemoryPoolValue(String category){
		Long value;
		value = allMemoryPoolEntries.get(category);
		if (value == null)
			return (long)-1;
		else
			return value;
	}
	
	public Long getMemoryPoolValue(List<String> categories) {
		Long value;
		for (String category : categories) {
			value = allMemoryPoolEntries.get(category);
			if (value != null)
				return value;
		}
		return (long)-1;  // not found
	}

	public Long getOldGenUsedValue() {
		return oldUsageUsed;  
	}
	
	public Long getOldGenMaxValue() {
		return oldUsageMax;
	}	
	
	public Long getYoungSpaceUsedValue() {
		return youngUsageUsed;
	}

	public Long getYoungSpaceMaxValue() {
		return youngUsageMax;
	}
	
	public boolean isYoungSpaceMaxOptional() {
		return youngUsageMaxOptional;
	}
	
	public double getHeapUsage(){
		if (Double.doubleToRawLongBits(this.heapUsedPercent) == DOUBLE_TO_LONG_MINUS_TWO)
			heapUsedPercent = computeHeap();

		return heapUsedPercent;
	}

	private double computeHeap() {
		double percent = -1;
		
		if (oldUsageUsed == null 
			|| youngUsageUsed == null
			|| oldUsageMax == null
			|| youngUsageMax == null
			|| oldUsageUsed == -1
			|| youngUsageUsed == -1
			|| oldUsageMax == -1
			|| (!youngUsageMaxOptional && youngUsageMax == -1)
			)
			return percent;
		
		percent = FormulaHelper.percent(oldUsageUsed + youngUsageUsed, youngUsageMaxOptional? oldUsageMax : (oldUsageMax + youngUsageMax));

		return percent;
	}
}
