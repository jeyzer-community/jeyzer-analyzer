package org.jeyzer.analyzer.output.poi.rule.header;

import java.util.ArrayList;

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
import org.jeyzer.analyzer.data.memory.MemoryPools;
import org.jeyzer.analyzer.math.FormulaHelper;
import org.jeyzer.analyzer.output.poi.context.SheetDisplayContext;
import org.jeyzer.analyzer.output.poi.rule.header.function.HeaderFunction;
import org.jeyzer.analyzer.output.poi.rule.header.function.HeaderFunction.HeaderFunctionType;

public class MemoryPoolGenericPercentRule extends AbstractNumericDisplayHeaderRule implements Header {
	
	public static final String RULE_NAME = "memory_pool_ratio";
	
	private static final String FRACTION_CATEGORY_NAME = "fraction_category";
	private static final String BASIS_CATEGORY_NAME = "basis_category";
	private static final String DISPLAY_NAME = "display";
	private static final String DESCRIPTION_NAME = "description";
	
	private static final Set<HeaderFunctionType> SUPPORTED_FUNCTIONS = EnumSet.of(
			HeaderFunctionType.AVERAGE, 
			HeaderFunctionType.MIN,
			HeaderFunctionType.MAX,
			HeaderFunctionType.VARIANCE,
			HeaderFunctionType.STANDARD_DEVIATION);
	
	private List<String> fractionCategories;
	private List<String> basisCategories;
	
	private String name;
	private String displayName;
	private String description;
	
	public MemoryPoolGenericPercentRule(ConfigSheetHeader headerCfg, SheetDisplayContext context) {
		super(headerCfg, context);
		
		this.fractionCategories = buildCategories(headerCfg, FRACTION_CATEGORY_NAME);
		this.basisCategories = buildCategories(headerCfg, BASIS_CATEGORY_NAME);
		this.displayName = (String)headerCfg.getValue(DISPLAY_NAME);
		this.name = RULE_NAME + "-" + this.displayName.toLowerCase().replace(' ', '-');
		this.description = (String)headerCfg.getValue(DESCRIPTION_NAME);
	}

	@Override
	public boolean apply(List<ThreadDump> dumps, List<Cell> cells, HeaderFunction function) {
		Cell cell;
		Long basisValue, fractionValue;
		double percent = 0;
		double prevPercent = -1;
		Double value;
		boolean available;
		
		// cell value
		for (int j=0; j< dumps.size(); j++){
			value = null; // NA
			cell = cells.get(j);
			
			MemoryPools memPools = dumps.get(j).getMemoryPools();
			fractionValue = memPools.getMemoryPoolValue(this.fractionCategories);
			basisValue = memPools.getMemoryPoolValue(this.basisCategories);
			available = !(fractionValue == null || fractionValue == -1 
					|| basisValue == null || basisValue == -1 || basisValue == 0);
			if (available){
				// display in Mb
				percent = FormulaHelper.percent(fractionValue, basisValue);
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
		return description;
	}

	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public boolean isPercentageBased(){
		return true;
	}
	
	@Override
	public String getStyle(){
		return getDecimalStyle();
	}
	
	@Override
	public boolean supportFunction(HeaderFunctionType function) {
		return SUPPORTED_FUNCTIONS.contains(function);
	}
	
	private List<String> buildCategories(ConfigSheetHeader headerCfg, String categoryPart) {
		List<String> candidates = new ArrayList<>();
		String value = (String)headerCfg.getValue(categoryPart);
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
