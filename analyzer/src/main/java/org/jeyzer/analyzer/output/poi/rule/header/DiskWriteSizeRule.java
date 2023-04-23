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
import org.jeyzer.analyzer.math.FormulaHelper;
import org.jeyzer.analyzer.output.poi.context.SheetDisplayContext;
import org.jeyzer.analyzer.output.poi.rule.header.function.HeaderFunction;
import org.jeyzer.analyzer.output.poi.rule.header.function.HeaderFunction.HeaderFunctionType;

public class DiskWriteSizeRule extends AbstractNumericDisplayHeaderRule implements Header{
	
	public static final String RULE_NAME = "recording_write_size";
		
	private static final String DISPLAY_NAME = "Recording snapshot file size";
	public static final String CELL_LABEL_COMMENT = "JZR recording snapshot file size in Kb.\nOnly available in Jeyzer agent mode";
	
	private static final Set<HeaderFunctionType> SUPPORTED_FUNCTIONS = EnumSet.of(
			HeaderFunctionType.AVERAGE, 
			HeaderFunctionType.MIN,
			HeaderFunctionType.MAX,
			HeaderFunctionType.CUMULATIVE,
			HeaderFunctionType.VARIANCE,
			HeaderFunctionType.STANDARD_DEVIATION);
	
	public DiskWriteSizeRule(ConfigSheetHeader displayCfg, SheetDisplayContext context) {
		super(displayCfg, context);
	}

	@Override
	public boolean apply(List<ThreadDump> dumps, List<Cell> cells, HeaderFunction function) {
		Cell cell;
		long writeSizeValue;
		long prevWriteSizeTime = (long) -1;
		Long value;
		
		// cell value
		for (int j=0; j< dumps.size(); j++){
			value = null; // NA
			cell = cells.get(j);

			writeSizeValue = dumps.get(j).getWriteSize();
			if (writeSizeValue == -1)
				writeSizeValue = dumps.get(j).getFileSize(); // revert to file size
			if (writeSizeValue != -1){
				// display in Kb
				writeSizeValue = FormulaHelper.convertToKb(writeSizeValue);
				value = writeSizeValue;
			}
			
			setValue(cell, value);
			function.apply(value, cell);

			setValueBasedColorForeground(
					cell,
					writeSizeValue,
					prevWriteSizeTime,
					writeSizeValue == -1
					);
			
			prevWriteSizeTime = writeSizeValue;
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
