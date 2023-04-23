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



import static org.jeyzer.analyzer.output.poi.style.DefaultCellStyles.*;





import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.jeyzer.analyzer.config.report.ConfigDisplay;
import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.analyzer.output.poi.context.SheetDisplayContext;
import org.jeyzer.analyzer.output.poi.rule.AbstractDisplayRule;
import org.jeyzer.analyzer.output.poi.rule.header.function.HeaderFunction;
import org.jeyzer.analyzer.output.poi.rule.header.function.HeaderFunction.HeaderFunctionType;

public class ProcessUpTimeRule extends AbstractDisplayRule implements Header{

	public static final String RULE_NAME = "process_up_time";
	
	public static final String CELL_LABEL_COMMENT = "Collected process up time";
	
	private static final String DISPLAY_NAME = "Process up time";
	
	public ProcessUpTimeRule(ConfigDisplay headerCfg, SheetDisplayContext context) {
		super(headerCfg, context);
	}

	@Override
	public boolean apply(List<ThreadDump> dumps, List<Cell> cells, HeaderFunction function) {
		Cell cell;
		long upTime;
		String value; // NA
		
		// cell value
		for (int j=0; j< dumps.size(); j++){
			cell = cells.get(j);
			
			upTime = dumps.get(j).getProcessUpTime();
			if (upTime != -1){
				value = displayTime(upTime);
			}
			else{
				value = AbstractDisplayRule.NOT_AVAILABLE;
			}
			
			setValue(cell, value);
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
	public String getStyle(){
		return STYLE_CELL_ALIGN_RIGHT;
	}	


	private String displayTime(long upTime) {
		long displayUpTime = upTime / 1000L; // convert to sec
		if (displayUpTime > 86400)
			return String.format("%dd %02d:%02d:%02d",
					displayUpTime / 86400 ,  // day
					(displayUpTime % 86400) / 3600 ,  // hour
					(displayUpTime % 3600) / 60 ,  // mn 
					displayUpTime % 60);  // sec
		else if (displayUpTime > 3600)
			return String.format("%02d:%02d:%02d",
					(displayUpTime % 86400) / 3600 ,  // hour
					(displayUpTime % 3600) / 60 ,  // mn 
					displayUpTime % 60);  // sec
		else 
			return String.format("%02d:%02d",
					(displayUpTime % 3600) / 60 ,  // mn 
					displayUpTime % 60);  // sec
	}
	
	@Override
	public boolean supportFunction(HeaderFunctionType function) {
		return false;
	}
	
}
