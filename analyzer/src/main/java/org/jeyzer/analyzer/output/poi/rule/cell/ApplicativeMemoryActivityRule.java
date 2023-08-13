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


public class ApplicativeMemoryActivityRule extends AbstractCellDisplayRule implements DisplayRule{
	
	private static final Logger logger = LoggerFactory.getLogger(ApplicativeMemoryActivityRule.class);		

	public static final String RULE_NAME = "applicative_memory_activity";
	public static final String THRESHOLD_NAME = "threshold";
	
	private static final String NOT_AVAILABLE = "NA";
	private static final String COLOR_NOT_AVAILABLE = "GREY_25_PERCENT";
	
	private int threshold = 70; // default
	
	public ApplicativeMemoryActivityRule(ConfigDisplay displayCfg, SequenceSheetDisplayContext context){
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
		ThreadStack stack;
		String value;
		long applicativeMemoryActivity = -1;
		
		// color
		boolean actionHit = false;
		for (int j=0; j< action.size(); j++){
			cell = cells.get(j);
			stack = action.getThreadStack(j);
			if (stack.getMemoryInfo() == null)
				value = NOT_AVAILABLE;
			else{
				applicativeMemoryActivity = Math.round(stack.getMemoryInfo().getApplicativeActivityUsage());
				if (applicativeMemoryActivity != -1){
					value = Long.toString(applicativeMemoryActivity) + "%";
				}else{
					value = NOT_AVAILABLE;
				}
			}
			
			setValue(cell, value);

			if (applicativeMemoryActivity == -1)
				setColorForeground(cell,COLOR_NOT_AVAILABLE);
			else if (applicativeMemoryActivity >= this.threshold){
				setColorForeground(cell);
				actionHit = hitStats(this.stats, actionHit, stack.getInstanceCount());
			}
		}
		
		return true;
	}

	@Override
	public int displayLegend(int line, int pos) {
		return displayLegend("Applicative memory activity > " + this.threshold + "%", "(value)", true, line, pos);
	}	

	@Override
	public int displayStats(int line, int pos) {
		return super.displayStats(
				this.stats,
				"Applicative memory activity > " + this.threshold + "%", 
				"(value)",
				true, 
				line,
				pos);
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
		return true;
	}	

}
