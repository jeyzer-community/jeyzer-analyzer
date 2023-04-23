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





import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.jeyzer.analyzer.config.report.headers.ConfigSheetHeader;
import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.analyzer.data.gc.GarbageCollectorInfo;
import org.jeyzer.analyzer.data.gc.GarbageCollectorMemoryPool;
import org.jeyzer.analyzer.output.poi.context.SheetDisplayContext;
import org.jeyzer.analyzer.output.poi.rule.header.function.HeaderFunction;
import org.jeyzer.analyzer.output.poi.rule.header.function.HeaderFunction.HeaderFunctionType;

public class GarbageCollectorHealthIndicationRule extends AbstractGarbageCollectionMemoryRule implements Header {

	public static final String RULE_NAME = "garbage_collection_health_indication";

	public static final String CELL_LABEL_COMMENT_PREFIX = "Garbage collection health indication for ";
	public static final String CELL_LABEL_COMMENT_SUFFIX =
			" This is composite indication : <% used memory after GC>.<% of before GC memory which as been released> \n"
			+ " Examples : \n"
			+ "   90.15  =  very bad. GC released only small amount of memory\n"
			+ "   5.90   =  very good. GC released high amount of memory (90% of the used memory)\n"
			+ "   5.05   =  quiet. 5% of the used memory was released.\n"
			+ " A null decimal part indicates memory pool stability or growth";
	
	public GarbageCollectorHealthIndicationRule(ConfigSheetHeader headerCfg, SheetDisplayContext context) {
		super(headerCfg, context);
	}
	
	@Override
	public boolean apply(List<ThreadDump> dumps, List<Cell> cells, HeaderFunction function) {
		Cell cell;
		GCExecutionPercentValue memoryPoolHealthIndicationValue;
		double prevMemoryPoolHealthIndicationValue = -1;
		
		// cell value
		for (int j=0; j< dumps.size(); j++){
			cell = cells.get(j);
			
			GarbageCollectorInfo gcInfo = accessGarbageCollectionInfo(dumps.get(j));
			memoryPoolHealthIndicationValue = buildGCExecutionPercentValue(gcInfo, 
					gcInfo != null ? gcInfo.getHealthMemPoolIndication():0,
							true
							);
			
			setValue(cell, memoryPoolHealthIndicationValue, null);

			setValueBasedColorForeground(
					cell,
					memoryPoolHealthIndicationValue,
					prevMemoryPoolHealthIndicationValue
					);
			
			prevMemoryPoolHealthIndicationValue = memoryPoolHealthIndicationValue.getDisplayableValue();
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
		return STYLE_CELL_DECIMAL_2_NUMBER;
	}		

	@Override
	protected long getMemoryPoolValue(GarbageCollectorMemoryPool memPool){
		return Long.MAX_VALUE; // invalid value. Should not happen
	}

	@Override
	protected double getMemoryPoolPercentValue(GarbageCollectorMemoryPool memPool){
		return memPool.getHealthMemPoolIndication(); 
	}
	
	@Override
	public boolean supportFunction(HeaderFunctionType function) {
		return false;
	}
	
}
