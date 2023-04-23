package org.jeyzer.analyzer.output.poi.rule.cell;

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







import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.jeyzer.analyzer.config.report.ConfigDisplay;
import org.jeyzer.analyzer.data.Action;
import org.jeyzer.analyzer.data.stack.ThreadStack;
import org.jeyzer.analyzer.output.poi.context.SequenceSheetDisplayContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class StackSizeRule extends AbstractCellDisplayRule implements DisplayRule {

	private static final Logger logger = LoggerFactory.getLogger(StackSizeRule.class);
	
	public static final String RULE_NAME = "stack_size";
	public static final String THRESHOLD_NAME = "threshold";
	
	private int threshold = 10;
	
	public StackSizeRule (ConfigDisplay displayCfg, SequenceSheetDisplayContext context){
		super(displayCfg, context);
		try{
			this.threshold = Integer.parseInt((String)displayCfg.getValue(THRESHOLD_NAME));
		}catch(Exception ex){
			logger.error("Failed to read threshold value for the " + RULE_NAME + " display rule", ex);
		}
	}
	
	@Override
	public boolean apply(Action action, List<Cell> cells) {
		Cell cell;
		String function;
		int size;
		
		// cell value
		for (int j=0; j< action.size(); j++){
			cell = cells.get(j);
			function = action.getFunctionTags(j);
			size = action.getThreadStack(j).getStackHandler().getCodeLines().size();
			if (ThreadStack.FUNC_TO_BE_IDENTIFIED.equals(function) && size<=this.threshold){
				setColorForeground(cell);
			}
			cell.setCellValue(size);
		}
		
		return true;
	}

	@Override
	public int displayLegend(int line, int pos) {
		return displayLegend("Action to be identified with small stack", "(stack size)", true, line, pos);
	}

	@Override
	public int displayStats(int line, int pos) {
		// do nothing
		return line;
	}	
	
	@Override
	public String getName() {
		return RULE_NAME;
	}
	
	@Override
	public boolean hasLegend() {
		return true;
	}
	
	@Override
	public boolean hasStats() {
		return false;
	}	
}
