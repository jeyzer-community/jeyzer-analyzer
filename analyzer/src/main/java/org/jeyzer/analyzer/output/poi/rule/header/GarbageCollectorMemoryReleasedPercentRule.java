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



import static org.jeyzer.analyzer.output.poi.style.DefaultCellStyles.*;





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

public class GarbageCollectorMemoryReleasedPercentRule extends AbstractGarbageCollectionMemoryRule implements Header {

	public static final String RULE_NAME = "garbage_collection_memory_released_percent";

	public static final String CELL_LABEL_COMMENT_PREFIX = "Garbage collection memory released percentage for";
	public static final String CELL_LABEL_COMMENT_SUFFIX = "Formula = percentage ( (Memory used before GC - Memory used after GC) / Pool(s) max memory )\n"
			+ "Value can be negative if memory has been transferred from another memory pool to this one.";
	
	private static final Set<HeaderFunctionType> SUPPORTED_FUNCTIONS = EnumSet.of(
			HeaderFunctionType.AVERAGE, 
			HeaderFunctionType.MIN,
			HeaderFunctionType.MAX,
			HeaderFunctionType.VARIANCE,
			HeaderFunctionType.STANDARD_DEVIATION);
	
	public GarbageCollectorMemoryReleasedPercentRule(ConfigSheetHeader headerCfg, SheetDisplayContext context) {
		super(headerCfg, context);
	}

	@Override
	public boolean apply(List<ThreadDump> dumps, List<Cell> cells, HeaderFunction function) {
		Cell cell;
		GCExecutionPercentValue memoryReleasedPercentValue;
		double prevMemoryReleasedPercentValue = -1;
		
		// cell value
		for (int j=0; j< dumps.size(); j++){
			cell = cells.get(j);
			
			GarbageCollectorInfo gcInfo = accessGarbageCollectionInfo(dumps.get(j));
			memoryReleasedPercentValue = buildGCExecutionPercentValue(gcInfo, 
					gcInfo != null ? gcInfo.getReleasedMemoryPercent():0,
							false
							);
			
			setValue(cell, memoryReleasedPercentValue, function);

			setValueBasedColorForeground(
					cell,
					memoryReleasedPercentValue,
					prevMemoryReleasedPercentValue
					);			
			
			prevMemoryReleasedPercentValue = memoryReleasedPercentValue.getDisplayableValue();
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
		return true;
	}
	
	@Override
	public String getStyle(){
		return STYLE_CELL_DECIMAL_NUMBER;
	}		
	
	@Override
	protected long getMemoryPoolValue(GarbageCollectorMemoryPool memPool){
		return Long.MAX_VALUE; // invalid value. Should not happen
	}

	@Override
	protected double getMemoryPoolPercentValue(GarbageCollectorMemoryPool memPool){
		return memPool.getReleasedMemoryPercent(); 
	}	

	@Override
	public boolean supportFunction(HeaderFunctionType function) {
		return SUPPORTED_FUNCTIONS.contains(function);
	}
}
