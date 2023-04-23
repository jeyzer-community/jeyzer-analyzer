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

public class ComputedProcessCPURule extends AbstractNumericDisplayHeaderRule implements Header{

	public static final String CELL_LABEL_COMMENT = "Computed process CPU usage\n"
	+ "Formula = percentage avg (sum of collected thread cpu times / global system time)\n"
	+ "Important : value may differ from the collected one as CPU data retrieval is not transactional.\n"
	+ "Can be greater than 100% in multi core system.";
	
	public static final String RULE_NAME = "computed_process_cpu";
	
	private static final String DISPLAY_NAME = "CPU Computed Process %";
	
	private static final Set<HeaderFunctionType> SUPPORTED_FUNCTIONS = EnumSet.of(
			HeaderFunctionType.AVERAGE, 
			HeaderFunctionType.MIN,
			HeaderFunctionType.MAX,
			HeaderFunctionType.VARIANCE,
			HeaderFunctionType.STANDARD_DEVIATION);
	
	public ComputedProcessCPURule(ConfigSheetHeader headerCfg, SheetDisplayContext context){
		super(headerCfg, context);
	}	
	
	@Override
	public boolean apply(List<ThreadDump> dumps, List<Cell> cells, HeaderFunction function) {
		Cell cell;
		double processCpu;
		double prevProcessCpu = -1;
		Double value;
		
		// cell value
		for (int j=0; j< dumps.size(); j++){
			value = null; // NA
			cell = cells.get(j);
			
			processCpu = dumps.get(j).getProcessComputedCPUUsage();
			if (Double.doubleToRawLongBits(processCpu) != DOUBLE_TO_LONG_NA){
				processCpu = Math.round(processCpu);
				value = processCpu;
			}
			
			setValue(cell, value);
			function.apply(value, cell);
			
			setValueBasedColorForeground(
					cell,
					processCpu,
					prevProcessCpu,
					Double.doubleToRawLongBits(processCpu) == DOUBLE_TO_LONG_NA
					);
			
			prevProcessCpu = processCpu;
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
