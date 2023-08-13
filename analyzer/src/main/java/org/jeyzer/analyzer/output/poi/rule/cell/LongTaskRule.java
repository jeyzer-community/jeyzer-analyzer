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
import org.jeyzer.analyzer.output.poi.context.SequenceSheetDisplayContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LongTaskRule extends AbstractCellDisplayRule implements DisplayRule{
	
	private static final Logger logger = LoggerFactory.getLogger(LongTaskRule.class);

	public static final String RULE_NAME = "long_running";
	public static final String THRESHOLD_NAME = "threshold";
	
	private int threshold = 7; // default
	private int thresholdTime; // in sec
	
	public LongTaskRule(ConfigDisplay displayCfg, SequenceSheetDisplayContext context){
		super(displayCfg, context);
		try{
			this.threshold = Integer.parseInt((String)displayCfg.getValue(THRESHOLD_NAME));
		}catch(Exception ex){
			logger.error("Failed to read threshold value for the long_running display rule", ex);
		}
		
		if (this.threshold < 2)
			this.threshold = 2;
		
		thresholdTime = context.getThreadDumpPeriod() * (this.threshold - 1);
	}	
	
	@Override
	public boolean apply(Action action, List<Cell> cells) {
		Cell cell;
		
		// color
		if (action.size() >= this.threshold){
			stats.hitAction();
			for (int j=0; j< action.size(); j++){
				cell = cells.get(j);
				setColorForeground(cell);
				stats.hitStack(action.getThreadStack(j).getInstanceCount());
			}
		}
		
		return true;
	}

	@Override
	public int displayLegend(int line, int pos) {
		return displayLegend("Long running task > " + getDisplayThreshold(), "Task", true, line, pos);
	}	

	@Override
	public int displayStats(int line, int pos) {
		return super.displayStats(this.stats, "Long running task > " + getDisplayThreshold(), "Task", true, line, pos);
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
	
	private String getDisplayThreshold() {
		if (thresholdTime > 60)
			return Math.round(thresholdTime / 60f) + " mn";
		else
			return thresholdTime + " sec";
	}

}
