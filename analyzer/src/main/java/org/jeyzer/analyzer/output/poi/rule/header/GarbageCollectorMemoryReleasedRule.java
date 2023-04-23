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

public class GarbageCollectorMemoryReleasedRule extends AbstractGarbageCollectionMemoryRule implements Header{
	
	public static final String RULE_NAME = "garbage_collection_memory_released";

	public static final String CELL_LABEL_COMMENT_PREFIX = "Memory released through garbage collection in Mb for";
	public static final String CELL_LABEL_COMMENT_SUFFIX = "Value can be negative if memory has been transferred from another memory pool to this one.";
	
	private static final Set<HeaderFunctionType> SUPPORTED_FUNCTIONS = EnumSet.of(
			HeaderFunctionType.AVERAGE, 
			HeaderFunctionType.MIN,
			HeaderFunctionType.MAX,
			HeaderFunctionType.VARIANCE,
			HeaderFunctionType.STANDARD_DEVIATION);
	
	public GarbageCollectorMemoryReleasedRule(ConfigSheetHeader headerCfg, SheetDisplayContext context) {
		super(headerCfg, context);
	}

	@Override
	public boolean apply(List<ThreadDump> dumps, List<Cell> cells, HeaderFunction function) {
		Cell cell;
		GCExecutionValue memoryReleasedValue;
		long prevMemoryReleasedValue = -1;
		
		// cell value
		for (int j=0; j< dumps.size(); j++){
			cell = cells.get(j);
			
			GarbageCollectorInfo gcInfo = accessGarbageCollectionInfo(dumps.get(j));
			memoryReleasedValue = buildGCExecutionValue(gcInfo, 
					gcInfo != null ? gcInfo.getReleasedMemory():0,
							false
							);
			
			// display in Mb
			if (GC_EXECUTION_STATE.EXECUTED.equals(memoryReleasedValue.getState()))
				memoryReleasedValue.setValue(convertToMb(memoryReleasedValue.getValue()));
			
			setValue(cell, memoryReleasedValue, function);

			setValueBasedColorForeground(
					cell,
					memoryReleasedValue,
					prevMemoryReleasedValue
					);			
			
			prevMemoryReleasedValue = memoryReleasedValue.getDisplayableValue();
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
		return CELL_LABEL_COMMENT_PREFIX + displayCommentPoolAndGCName() + CELL_LABEL_COMMENT_SUFFIX;
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
		return memPool.getReleasedMemory();
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
