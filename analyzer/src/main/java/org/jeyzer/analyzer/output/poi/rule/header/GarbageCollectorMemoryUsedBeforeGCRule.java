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




import static org.jeyzer.analyzer.math.FormulaHelper.convertToMb;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.jeyzer.analyzer.config.report.headers.ConfigSheetHeader;
import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.analyzer.data.gc.GarbageCollectorInfo;
import org.jeyzer.analyzer.data.gc.GarbageCollectorMemoryPool;
import org.jeyzer.analyzer.output.poi.context.SheetDisplayContext;
import org.jeyzer.analyzer.output.poi.rule.header.function.HeaderFunction;
import org.jeyzer.analyzer.output.poi.rule.header.function.HeaderFunction.HeaderFunctionType;

public class GarbageCollectorMemoryUsedBeforeGCRule extends AbstractGarbageCollectionMemoryRule implements Header {

	public static final String RULE_NAME = "garbage_collection_memory_used_before_gc";

	public static final String CELL_LABEL_COMMENT = "Memory in use before garbage garbage collection in Mb for";	
	
	private static final Set<HeaderFunctionType> SUPPORTED_FUNCTIONS = EnumSet.of(
			HeaderFunctionType.AVERAGE, 
			HeaderFunctionType.MIN,
			HeaderFunctionType.MAX,
			HeaderFunctionType.VARIANCE,
			HeaderFunctionType.STANDARD_DEVIATION);
	
	public GarbageCollectorMemoryUsedBeforeGCRule(ConfigSheetHeader headerCfg, SheetDisplayContext context) {
		super(headerCfg, context);
	}

	@Override
	public boolean apply(List<ThreadDump> dumps, List<Cell> cells, HeaderFunction function) {
		Cell cell;
		GCExecutionValue memoryUsedValue;
		long prevMemoryUsedValue = -1;
		
		// cell value
		for (int j=0; j< dumps.size(); j++){
			cell = cells.get(j);
			
			GarbageCollectorInfo gcInfo = accessGarbageCollectionInfo(dumps.get(j));
			memoryUsedValue = buildGCExecutionValue(gcInfo, 
					gcInfo != null ? gcInfo.getUsedMemoryBefore():0,
					gcInfo != null ? gcInfo.isStarted():false
				);
			
			// display in Mb
			if (GC_EXECUTION_STATE.EXECUTED.equals(memoryUsedValue.getState()) || GC_EXECUTION_STATE.NOT_EXECUTED.equals(memoryUsedValue.getState()))
				memoryUsedValue.setValue(convertToMb(memoryUsedValue.getValue()));
			
			setValue(cell, memoryUsedValue, function);

			setValueBasedColorForeground(
					cell,
					memoryUsedValue,
					prevMemoryUsedValue
					);			
			
			prevMemoryUsedValue = memoryUsedValue.getDisplayableValue();
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
		return CELL_LABEL_COMMENT + displayCommentPoolAndGCName();
	}

	@Override
	public String getName() {
		return RULE_NAME + getNameSuffix();
	}

	@Override
	public boolean isPercentageBased(){
		return false;
	}
	
	@Override
	public String getStyle(){
		return getNumberStyle();
	}

	@Override
	protected long getMemoryPoolValue(GarbageCollectorMemoryPool memPool){
		return memPool.getUsedMemoryBefore();
	}

	@Override
	protected double getMemoryPoolPercentValue(GarbageCollectorMemoryPool memPool){
		return -10000; // invalid value. Should not happen
	}
	
	@Override
	public boolean supportFunction(HeaderFunctionType function) {
		return SUPPORTED_FUNCTIONS.contains(function);
	}
}
