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
import org.jeyzer.analyzer.output.poi.context.SheetDisplayContext;
import org.jeyzer.analyzer.output.poi.rule.header.function.HeaderFunction;
import org.jeyzer.analyzer.output.poi.rule.header.function.HeaderFunction.HeaderFunctionType;

public class DiskSpaceFreePercentRule extends AbstractDiskSpaceRule implements Header{
	
	public static final String RULE_NAME = "free_disk_space_percent";
	
	private static final Set<HeaderFunctionType> SUPPORTED_FUNCTIONS = EnumSet.of(
			HeaderFunctionType.AVERAGE, 
			HeaderFunctionType.MIN,
			HeaderFunctionType.MAX,
			HeaderFunctionType.VARIANCE,
			HeaderFunctionType.STANDARD_DEVIATION);
	
	public DiskSpaceFreePercentRule(ConfigSheetHeader headerCfg, SheetDisplayContext context){
		super(headerCfg, context);
	}
	
	@Override
	public boolean apply(List<ThreadDump> dumps, List<Cell> cells, HeaderFunction function) {
		Cell cell;
		double freeDiskSpacePercent = NOT_SET;
		double prevFreeDiskSpacePercent = NOT_SET;
		Double value;
		
		// cell value
		for (int j=0; j< dumps.size(); j++){
			value = null; // NA
			freeDiskSpacePercent = NOT_SET;
			cell = cells.get(j);
			
			DiskSpaceInfo diskSpace = dumps.get(j).getDiskSpaces().getDiskSpace(this.id);
			if (diskSpace != null){
				double result = diskSpace.getFreeSpacePercent();
				if (result > DiskSpaceInfo.NOT_AVAILABLE){
					freeDiskSpacePercent = result;
					value = freeDiskSpacePercent;
				}
			}
			
			setValue(cell, value);
			function.apply(value, cell);
			
			setValueBasedColorForeground(
					cell,
					freeDiskSpacePercent,
					prevFreeDiskSpacePercent,
					freeDiskSpacePercent == NOT_SET
					);
			
			prevFreeDiskSpacePercent = freeDiskSpacePercent;
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
