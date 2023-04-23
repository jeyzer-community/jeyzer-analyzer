package org.jeyzer.analyzer.output.poi.rule.header;

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







import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.jeyzer.analyzer.config.report.headers.ConfigSheetHeader;
import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.analyzer.data.gc.GarbageCollection;
import org.jeyzer.analyzer.data.gc.GarbageCollectorInfo;
import org.jeyzer.analyzer.output.poi.context.SheetDisplayContext;
import org.jeyzer.analyzer.output.poi.rule.header.function.HeaderFunction;
import org.jeyzer.analyzer.output.poi.rule.header.function.HeaderFunction.HeaderFunctionType;

public class GarbageCollectorCollectionTimeRule extends AbstractGarbageCollectionRule implements Header {

	public static final String RULE_NAME = "garbage_collection_time";

	public static final String CELL_LABEL_COMMENT = "Garbage collection execution time since last JZR recording snapshot in ms";	
	
	private static final Set<HeaderFunctionType> SUPPORTED_FUNCTIONS = EnumSet.of(
			HeaderFunctionType.AVERAGE, 
			HeaderFunctionType.MIN,
			HeaderFunctionType.MAX,
			HeaderFunctionType.CUMULATIVE,
			HeaderFunctionType.VARIANCE,
			HeaderFunctionType.STANDARD_DEVIATION);
	
	public GarbageCollectorCollectionTimeRule(ConfigSheetHeader headerCfg, SheetDisplayContext context) {
		super(headerCfg, context);
	}

	@Override
	public boolean apply(List<ThreadDump> dumps, List<Cell> cells, HeaderFunction function) {
		Cell cell;
		long timeValue;
		long prevTimeValue = (long) -1;
		Long value;
		boolean available;
		
		// cell value
		for (int j=0; j< dumps.size(); j++){
			value = null; // NA
			
			cell = cells.get(j);

			timeValue = accessCollectionTime(dumps.get(j));
			
			available = !(timeValue == -1);
			if (available){
				value = timeValue;
			}
			
			setValue(cell, value);
			function.apply(value, cell);

			setValueBasedColorForeground(
					cell,
					timeValue,
					prevTimeValue,
					!available
					);			
			
			prevTimeValue = timeValue;
		}
		
		return true;
	}		

	@Override
	public int displayLegend(int line, int pos) {
		return line;
	}

	@Override
	public int displayStats(int line, int pos) {
		return line;
	}

	@Override
	public String getDisplayName() {
		return this.displayName;
	}

	@Override
	public String getComment() {
		return CELL_LABEL_COMMENT;
	}

	@Override
	public String getName() {
		return RULE_NAME + "-" + gc;
	}

	@Override
	public boolean isPercentageBased(){
		return false;
	}
	
	@Override
	public String getStyle(){
		return getNumberStyle();
	}	

	private long accessCollectionTime(ThreadDump threadDump) {
		long timeValue = -1;
		
		GarbageCollection garbageCollectionData = threadDump.getGarbageCollection();

		if (garbageCollectionData == null)
			return -1;
		
		if (DISPLAY_GC_ALL.equals(gc)){
			timeValue = garbageCollectionData.getGcTime();
		}else{
			GarbageCollectorInfo gcInfo = accessGarbageCollectionInfo(threadDump);
			if (gcInfo != null)
				timeValue = gcInfo.getCollectionTime();
		}

		return timeValue;
	}	

	@Override
	public boolean supportFunction(HeaderFunctionType function) {
		return SUPPORTED_FUNCTIONS.contains(function);
	}
}
