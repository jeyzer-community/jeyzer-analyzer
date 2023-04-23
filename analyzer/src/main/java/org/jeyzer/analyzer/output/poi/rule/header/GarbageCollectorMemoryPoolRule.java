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
import static org.jeyzer.analyzer.parser.advanced.GarbageCollectorBeanInforParser.*;





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

public class GarbageCollectorMemoryPoolRule  extends AbstractNumericDisplayHeaderRule implements Header {
	
	public static final String RULE_NAME = "garbage_collection_memory_pool";
	
	private static final String CATEGORY_NAME = "category";
	private static final String DISPLAY_NAME = "display";
	private static final String DESCRIPTION_NAME = "description";
	
	private static final Set<HeaderFunctionType> SUPPORTED_FUNCTIONS = EnumSet.of(
			HeaderFunctionType.AVERAGE, 
			HeaderFunctionType.MIN,
			HeaderFunctionType.MAX,
			HeaderFunctionType.VARIANCE,
			HeaderFunctionType.STANDARD_DEVIATION);

	private String displayName;
	private String description;
	
	private String gcName;
	private boolean after = false;
	private String poolField;
	
	public GarbageCollectorMemoryPoolRule(ConfigSheetHeader headerCfg, SheetDisplayContext context) {
		super(headerCfg, context);
		this.displayName = (String)headerCfg.getValue(DISPLAY_NAME);
		this.description = (String)headerCfg.getValue(DESCRIPTION_NAME);
		String category = (String)headerCfg.getValue(CATEGORY_NAME);
		
		gcName = parseGarbageCollectorName(category);
		parseInfo(gcName != null ? category.substring(gcName.length()) : category);
	}

	@Override
	public boolean apply(List<ThreadDump> dumps, List<Cell> cells, HeaderFunction function) {
		Cell cell;
		Long memoryPoolValue = null;
		Long prevMemoryPoolValue = (long)-1;
		Double value; 
		
		// cell value
		for (int j=0; j< dumps.size(); j++){
			value = null;  // NA
			
			cell = cells.get(j);
			
			GarbageCollection gc = dumps.get(j).getGarbageCollection();
			
			GarbageCollectorInfo gcInfo = gc.getGarbageCollectorInfo(gcName);
			if (gcInfo != null){
				if (after){
					memoryPoolValue = gcInfo.getAfterMemoryPoolValue(poolField); 
				}
				else{
					memoryPoolValue = gcInfo.getBeforeMemoryPoolValue(poolField);
				}
			}
			
			if (memoryPoolValue == null || memoryPoolValue != -1){
				// display in Mb
				memoryPoolValue = convertToMb(memoryPoolValue);
				value = (double)memoryPoolValue;
			}
			
			setValue(cell, value);
			function.apply(value, cell);
			
			setValueBasedColorForeground(
					cell,
					memoryPoolValue,
					prevMemoryPoolValue,
					memoryPoolValue == -1
					);
			
			prevMemoryPoolValue = memoryPoolValue;
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
		return description;
	}

	@Override
	public String getName() {
		return RULE_NAME + "-" + gcName + "-" + (this.after ? "after" : "before") + "-" + poolField;
	}
	
	@Override
	public boolean isPercentageBased(){
		return false;
	}
	
	@Override
	public String getStyle(){
		return getNumberStyle();
	}	

	private String parseGarbageCollectorName(String category) {
		int endPos = category.indexOf(GARBAGE_COLLECTOR_SEPARATOR);
		if (endPos == -1){
			logger.warn("Failed to parse the garbage collector name on category : {}", category);
			return null;
		}
		return category.substring(0, endPos);
	}	

	private void parseInfo(String category) {
		if (category.contains(GARBAGE_COLLECTOR_LAST_GC_BEFORE)){
			this.after = false;
			this.poolField = category.substring(GARBAGE_COLLECTOR_LAST_GC_BEFORE.length());
		}
		else if (category.contains(GARBAGE_COLLECTOR_LAST_GC_AFTER)){
			this.after = true;
			this.poolField =  category.substring(GARBAGE_COLLECTOR_LAST_GC_AFTER.length());
		}
		else {
			logger.warn("Garbage collector line is unknown : {}", category);
		}
	}	

	@Override
	public boolean supportFunction(HeaderFunctionType function) {
		return SUPPORTED_FUNCTIONS.contains(function);
	}
}
