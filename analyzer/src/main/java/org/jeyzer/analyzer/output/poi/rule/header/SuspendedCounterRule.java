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







import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.jeyzer.analyzer.config.report.headers.ConfigSheetHeader;
import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.analyzer.output.poi.context.SheetDisplayContext;
import org.jeyzer.analyzer.output.poi.rule.header.function.HeaderFunction;
import org.jeyzer.analyzer.output.poi.rule.header.function.HeaderFunction.HeaderFunctionType;

public class SuspendedCounterRule extends AbstractNumericDisplayHeaderRule implements Header{

	public static final String RULE_NAME = "suspended_counter";
	private static final String DISPLAY_NAME = "Suspensions";
	public static final String CELL_LABEL_COMMENT = "Count of the threads suspended on a debug breakpoint";
	
	private static final Set<HeaderFunctionType> SUPPORTED_FUNCTIONS = EnumSet.of(
			HeaderFunctionType.MIN,
			HeaderFunctionType.MAX);
	
	public SuspendedCounterRule(ConfigSheetHeader displayCfg, SheetDisplayContext context) {
		super(displayCfg, context);
	}

	@Override
	public boolean apply(List<ThreadDump> dumps, List<Cell> cells, HeaderFunction function) {
		Cell cell;
		double countValue;
		double prevCountValue = (long) -1;
		Double value = null; // NA
		
		// cell value
		for (int j=0; j< dumps.size(); j++){
			cell = cells.get(j);

			countValue = dumps.get(j).hasSuspendedThreads();
			value = countValue;
			
			setValue(cell, value);
			function.apply(value, cell);

			setValueBasedColorForeground(
					cell,
					countValue,
					prevCountValue,
					false
					);
			
			prevCountValue = countValue;
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
		return DISPLAY_NAME;
	}

	@Override
	public String getComment() {
		return CELL_LABEL_COMMENT;
	}

	@Override
	public boolean isPercentageBased() {
		return false;
	}

	@Override
	public String getName() {
		return RULE_NAME;
	}
	
	@Override
	public String getStyle(){
		return getNumberStyle();
	}
	
	@Override
	public boolean supportFunction(HeaderFunctionType function) {
		return SUPPORTED_FUNCTIONS.contains(function);
	}
}
