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
import org.jeyzer.analyzer.data.DiskSpaceInfo;
import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.analyzer.math.FormulaHelper;
import org.jeyzer.analyzer.output.poi.context.SheetDisplayContext;
import org.jeyzer.analyzer.output.poi.rule.header.function.HeaderFunction;
import org.jeyzer.analyzer.output.poi.rule.header.function.HeaderFunction.HeaderFunctionType;

public class DiskSpaceUsedRule extends AbstractDiskSpaceRule implements Header{
	
	public static final String RULE_NAME = "used_disk_space";
	
	private static final Set<HeaderFunctionType> SUPPORTED_FUNCTIONS = EnumSet.of(
			HeaderFunctionType.AVERAGE, 
			HeaderFunctionType.MIN,
			HeaderFunctionType.MAX,
			HeaderFunctionType.VARIANCE,
			HeaderFunctionType.STANDARD_DEVIATION);
	
	public DiskSpaceUsedRule(ConfigSheetHeader headerCfg, SheetDisplayContext context){
		super(headerCfg, context);
	}
	
	@Override
	public boolean apply(List<ThreadDump> dumps, List<Cell> cells, HeaderFunction function) {
		Cell cell;
		long usedDiskSpace = NOT_SET;
		long prevUsedDiskSpace = NOT_SET;
		Long value;
		
		// cell value
		for (int j=0; j< dumps.size(); j++){
			value = null; // NA
			usedDiskSpace = NOT_SET;
			cell = cells.get(j);
			
			DiskSpaceInfo diskSpace = dumps.get(j).getDiskSpaces().getDiskSpace(this.id);
			if (diskSpace != null && diskSpace.hasUsedSpace()){
				// display in Gb
				usedDiskSpace = FormulaHelper.convertToGb(diskSpace.getUsedSpace());
				value = usedDiskSpace;				
			}
			
			setValue(cell, value);
			function.apply(value, cell);
			
			setValueBasedColorForeground(
					cell,
					usedDiskSpace,
					prevUsedDiskSpace,
					usedDiskSpace == NOT_SET
					);
			
			prevUsedDiskSpace = usedDiskSpace;
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
}
