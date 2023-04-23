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



import static org.jeyzer.analyzer.math.FormulaHelper.DOUBLE_TO_LONG_NA;





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

public class GarbageCollectorCollectionTimePercentRule extends AbstractGarbageCollectionRule implements Header{

	public static final String RULE_NAME = "garbage_collection_time_percent";

	public static final String CELL_LABEL_COMMENT = "Garbage collection execution time % since last JZR recording snapshot\n"
			+ " Formula = percentage (GC execution time since last JZR recording snapshot / elapsed time since JZR recording snapshot)";	
	
	private static final Set<HeaderFunctionType> SUPPORTED_FUNCTIONS = EnumSet.of(
			HeaderFunctionType.AVERAGE, 
			HeaderFunctionType.MIN,
			HeaderFunctionType.MAX,
			HeaderFunctionType.VARIANCE,
			HeaderFunctionType.STANDARD_DEVIATION);
	
	public GarbageCollectorCollectionTimePercentRule(ConfigSheetHeader headerCfg, SheetDisplayContext context) {
		super(headerCfg, context);
	}

	@Override
	public boolean apply(List<ThreadDump> dumps, List<Cell> cells, HeaderFunction function) {
		Cell cell;
		double prevPercent = -1;
		double percent;
		Double value;
		boolean available;
		
		// cell value
		for (int j=0; j< dumps.size(); j++){
			value = null; // NA
			percent = -1;
			
			cell = cells.get(j);
			
			percent = accessCollectionTimePercent(dumps.get(j));
			
			available = !(Double.doubleToRawLongBits(percent) == DOUBLE_TO_LONG_NA);
			if (available){
				value = percent;
			}
			
			setValue(cell, value);
			function.apply(value, cell);

			setValueBasedColorForeground(
					cell,
					percent,
					prevPercent,
					!available
					);			
			
			prevPercent = percent;
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
		return true;
	}
	
	@Override
	public String getStyle(){
		return getDecimalStyle();
	}
	
	private double accessCollectionTimePercent(ThreadDump threadDump) {
		double percent = -1;
		
		GarbageCollection garbageCollectionData = threadDump.getGarbageCollection();

		if (garbageCollectionData == null)
			return -1;
		
		if (DISPLAY_GC_ALL.equals(gc)){
			percent = garbageCollectionData.getGcTimePercent();
		}else{
			GarbageCollectorInfo gcInfo = accessGarbageCollectionInfo(threadDump);
			if (gcInfo != null)
				percent = gcInfo.getCollectionTimePercent();
		}

		return percent;
	}
	
	@Override
	public boolean supportFunction(HeaderFunctionType function) {
		return SUPPORTED_FUNCTIONS.contains(function);
	}
	
}
