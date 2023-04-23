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
import org.jeyzer.analyzer.output.poi.context.SheetDisplayContext;
import org.jeyzer.analyzer.output.poi.rule.header.function.HeaderFunction;
import org.jeyzer.analyzer.output.poi.rule.header.function.HeaderFunction.HeaderFunctionType;

public class ApplicativeMemoryActivityRule extends AbstractNumericDisplayHeaderRule implements Header{

	public static final String RULE_NAME = "applicative_memory_activity";
	
	public static final String CELL_LABEL_COMMENT = "Computed applicative memory activity usage\n"
	+ "Formula = percentage avg (sum of collected thread allocated bytes / process memory peak)";
	
	private static final String DISPLAY_NAME = "Applicative Memory Activity %";
	
	public ApplicativeMemoryActivityRule(ConfigSheetHeader headerCfg, SheetDisplayContext context){
		super(headerCfg, context);
	}
	
	private static final Set<HeaderFunctionType> SUPPORTED_FUNCTIONS = EnumSet.of(
			HeaderFunctionType.AVERAGE, 
			HeaderFunctionType.VARIANCE,
			HeaderFunctionType.STANDARD_DEVIATION);
	
	@Override
	public boolean apply(List<ThreadDump> dumps, List<Cell> cells, HeaderFunction function) {
		Cell cell;
		double memoryActivityUsage;
		double prevMemoryActivityUsage = -1;
		Double value;
		
		// cell value
		for (int j=0; j< dumps.size(); j++){
			value = null; // NA
			cell = cells.get(j);
			
			memoryActivityUsage = dumps.get(j).getApplicativeMemoryActivityUsage();
			if (Double.doubleToRawLongBits(memoryActivityUsage) != DOUBLE_TO_LONG_NA){
				memoryActivityUsage = Math.round(memoryActivityUsage);
				value = memoryActivityUsage;
			}
			
			setValue(cell, value);
			function.apply(value, cell);

			setValueBasedColorForeground(
					cell,
					memoryActivityUsage,
					prevMemoryActivityUsage,
					Double.doubleToRawLongBits(memoryActivityUsage) == DOUBLE_TO_LONG_NA
					);
			
			prevMemoryActivityUsage = memoryActivityUsage;
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
	public String getName() {
		return RULE_NAME;
	}

	@Override
	public String getDisplayName() {
		return DISPLAY_NAME;
	}

	@Override
	public String getComment() {
		return CELL_LABEL_COMMENT;
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
}
