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



import static org.jeyzer.analyzer.math.FormulaHelper.*;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.jeyzer.analyzer.config.report.headers.ConfigSheetHeader;
import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.analyzer.data.memory.MemoryPools;
import org.jeyzer.analyzer.output.poi.context.SheetDisplayContext;
import org.jeyzer.analyzer.output.poi.rule.header.function.HeaderFunction;
import org.jeyzer.analyzer.output.poi.rule.header.function.HeaderFunction.HeaderFunctionType;

public class MemoryPoolGenericRule extends AbstractNumericDisplayHeaderRule implements Header {
	
	public static final String RULE_NAME = "memory_pool";
	
	private static final String CATEGORY_NAME = "category";
	private static final String DISPLAY_NAME = "display";
	private static final String DESCRIPTION_NAME = "description";

	private static final Set<HeaderFunctionType> SUPPORTED_FUNCTIONS = EnumSet.of(
			HeaderFunctionType.AVERAGE, 
			HeaderFunctionType.MIN,
			HeaderFunctionType.MAX,
			HeaderFunctionType.VARIANCE,
			HeaderFunctionType.STANDARD_DEVIATION);

	private String name;
	private List<String> categories;
	private String displayName;
	private String description;
	
	public MemoryPoolGenericRule(ConfigSheetHeader headerCfg, SheetDisplayContext context) {
		super(headerCfg, context);

		this.categories = buildCategories(headerCfg);
		this.displayName = (String)headerCfg.getValue(DISPLAY_NAME);
		this.name = RULE_NAME + "-" + this.displayName.toLowerCase().replace(' ', '-');
		this.description = (String)headerCfg.getValue(DESCRIPTION_NAME);
	}

	@Override
	public boolean apply(List<ThreadDump> dumps, List<Cell> cells, HeaderFunction function) {
		Cell cell;
		Long memoryPoolValue;
		Long prevMemoryPoolValue = (long)-1;
		Double value;
		
		// cell value
		for (int j=0; j< dumps.size(); j++){
			value = null; // NA
			cell = cells.get(j);
			
			MemoryPools memPools = dumps.get(j).getMemoryPools();
			memoryPoolValue = memPools.getMemoryPoolValue(this.categories);
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
		return this.name;
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
	public boolean supportFunction(HeaderFunctionType function) {
		return SUPPORTED_FUNCTIONS.contains(function);
	}
	
	private List<String> buildCategories(ConfigSheetHeader headerCfg) {
		List<String> candidates = new ArrayList<>();
		String value = (String)headerCfg.getValue(CATEGORY_NAME);
		int separatorIndex = value.indexOf(':');
		
		if (separatorIndex == -1)
			return candidates;
			
		String categorySuffix = value.substring(separatorIndex);
		String categoryPrefix = value.substring(0, separatorIndex);
		String[] categoryPrefixes = categoryPrefix.split("\\|");
		
		for (int i=0; i<categoryPrefixes.length ; i++) {
			if (!categoryPrefixes[i].isEmpty() && !categorySuffix.isEmpty())
				candidates.add(categoryPrefixes[i] + categorySuffix);
		}
		
		return candidates;
	}
}
