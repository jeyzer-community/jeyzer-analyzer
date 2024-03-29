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


public class VirtualUnmountedThreadCounterRule extends AbstractCellDisplayRule implements DisplayRule {

	public static final String RULE_NAME = "Virtual unmounted thread count";
	
	public VirtualUnmountedThreadCounterRule (ConfigDisplay displayCfg, SequenceSheetDisplayContext context){
		super(displayCfg, context);
	}
	
	@Override
	public boolean apply(Action action, List<Cell> cells) {
		Cell cell;
		String stackSize;
		
		if (!action.isVirtual())
			return false;
		
		if (!action.getThreadStack(0).getState().isUnmountedVirtualThread())
			return false;
		
		// cell value
		for (int j=0; j< action.size(); j++){
			cell = cells.get(j);
			stackSize = "UVT x" + Integer.toString(action.getThreadStack(j).getInstanceCount());
			setValue(cell, stackSize);
		}
		
		return true;
	}

	@Override
	public int displayLegend(int line, int pos) {
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
