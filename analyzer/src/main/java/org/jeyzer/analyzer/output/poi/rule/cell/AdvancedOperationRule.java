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


public class AdvancedOperationRule extends AbstractCellDisplayRule implements DisplayRule{
	
	public static final String RULE_NAME = "advanced_operation";
	
	public static final String ALL_OPERATIONS_NAME = "all_operations";
	
	private boolean allOps = false;
	
	public AdvancedOperationRule (ConfigDisplay displayCfg, SequenceSheetDisplayContext context){
		super(displayCfg, context);
		
		String allOpsValue = (String)displayCfg.getValue(ALL_OPERATIONS_NAME);
		this.allOps = Boolean.parseBoolean(allOpsValue);
	}
	
	@Override
	public boolean apply(Action action, List<Cell> cells) {
		Cell cell;
		String operation;
		
		// cell value
		for (int j=0; j< action.size(); j++){
			cell = cells.get(j);
			
			if (!allOps)
				operation = action.getPrincipalOperation(j);
			else
				operation = action.getOperationTags(j);
			
			setColorHighlight(cell, action.getPrincipalOperation(j));

			setValue(cell, operation);
		}
		
		return true;
	}	
	
	@Override
	public int displayLegend(int line, int pos) {
		return displayHighlightLegend(line, pos);
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
	public boolean hasLegend() {
		return hasHighlights();
	}
	
	@Override
	public boolean hasStats() {
		return false; // see FunctionHistogram sheet for stats
	}	
}
