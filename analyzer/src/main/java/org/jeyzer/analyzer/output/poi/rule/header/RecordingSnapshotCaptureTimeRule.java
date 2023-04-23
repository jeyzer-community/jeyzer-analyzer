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

public class RecordingSnapshotCaptureTimeRule extends AbstractNumericDisplayHeaderRule implements Header {

	public static final String RULE_NAME = "capture_time";
	private static final String DISPLAY_NAME = "Recording snapshot capture time";	
	public static final String CELL_LABEL_COMMENT = "Recording snapshot capture time in ms.\n"
			+ "Includes also time for any JMX operation.";
	
	private static final Set<HeaderFunctionType> SUPPORTED_FUNCTIONS = EnumSet.of(
			HeaderFunctionType.AVERAGE, 
			HeaderFunctionType.MIN,
			HeaderFunctionType.MAX,
			HeaderFunctionType.CUMULATIVE,
			HeaderFunctionType.VARIANCE,
			HeaderFunctionType.STANDARD_DEVIATION);
	
	public RecordingSnapshotCaptureTimeRule(ConfigSheetHeader displayCfg, SheetDisplayContext context) {
		super(displayCfg, context);
	}

	@Override
	public boolean apply(List<ThreadDump> dumps, List<Cell> cells, HeaderFunction function) {
		Cell cell;
		long timeValue;
		long prevtimeValue = (long) -1;
		Long value;
		
		// cell value
		for (int j=0; j< dumps.size(); j++){
			value = null; // NA
			cell = cells.get(j);

			timeValue = dumps.get(j).getCaptureTime();
			if (timeValue != -1){
				value = timeValue;
			}			
			
			setValue(cell, value);
			function.apply(value, cell);

			setValueBasedColorForeground(
					cell,
					timeValue,
					prevtimeValue,
					timeValue == -1
					);			
			
			prevtimeValue = timeValue;
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
