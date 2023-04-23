package org.jeyzer.analyzer.output.poi.rule.group.cell;

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
import org.jeyzer.analyzer.data.ThreadStackGroupAction;
import org.jeyzer.analyzer.output.poi.context.SequenceSheetDisplayContext;
import org.jeyzer.analyzer.output.poi.rule.cell.AbstractCellDisplayRule;
import org.jeyzer.analyzer.output.poi.rule.cell.DisplayRule;


public class GroupSizeRule extends AbstractCellDisplayRule implements DisplayRule {
	
	public static final String RULE_NAME = "group_size";
	public static final String THRESHOLD_NAME = "threshold";
	
	private int threshold;
	
	public GroupSizeRule (ConfigDisplay displayCfg, SequenceSheetDisplayContext context){
		super(displayCfg, context);
		try{
			this.threshold = Integer.parseInt((String)displayCfg.getValue(THRESHOLD_NAME));
		}catch(Exception ex){
			this.threshold = -1;
		}
	}
	
	@Override
	public boolean apply(Action action, List<Cell> cells) {
		Cell cell;
		int size;
		
		if (!(action instanceof ThreadStackGroupAction))
			return false;
		
		ThreadStackGroupAction groupAction = (ThreadStackGroupAction) action;
		
		// cell value
		for (int j=0; j< groupAction.size(); j++){
			cell = cells.get(j);
			size = groupAction.getGroupSize(j);
			if (size >1)
				setValue(cell, " x" + size, false);
			if (this.threshold != -1 && size > this.threshold)
				setColorForeground(cell);
		}
		
		return true;
	}

	@Override
	public int displayLegend(int line, int pos) {
		if (this.threshold != -1)
			return displayLegend("Number of stacks > " + this.threshold, "(value)", true, line, pos);
		else
			return line;
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
		return false;
	}
	
	@Override
	public boolean hasStats() {
		return false;
	}	
}
