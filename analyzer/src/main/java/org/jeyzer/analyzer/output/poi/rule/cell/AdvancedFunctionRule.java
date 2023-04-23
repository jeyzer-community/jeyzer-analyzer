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


public class AdvancedFunctionRule extends AbstractCellDisplayRule implements DisplayRule{

	public static final String RULE_NAME = "advanced_function";
	public static final String DISPLAY_OPERATION_FIELD = "display_operation";
	public static final String DISPLAY_CONTENTION_TYPE_FIELD = "display_contention_type";
	
	protected boolean displayOperation = false;
	protected boolean displayContentionType = false;
	
	public AdvancedFunctionRule (ConfigDisplay displayCfg, SequenceSheetDisplayContext context){
		super(displayCfg, context);
		displayOperation = Boolean.parseBoolean((String)displayCfg.getValue(DISPLAY_OPERATION_FIELD));
		displayContentionType = Boolean.parseBoolean((String)displayCfg.getValue(DISPLAY_CONTENTION_TYPE_FIELD));
	}
	
	@Override
	public boolean apply(Action action, List<Cell> cells) {
		Cell cell;
		String function, operation, contentionType;
		StringBuilder label;
		
		// cell value
		for (int j=0; j< action.size(); j++){
			cell = cells.get(j);
			function = action.getPrincipalFunction(j);
			label = new StringBuilder(function);
			
			if (displayOperation){
				operation = action.getPrincipalOperation(j);
				label.append("/" + operation); 
			}
			
			if (displayContentionType){
				contentionType = action.getPrincipalContentionType(j);
				label.append("/" + contentionType); 
			}
			
			setColorHighlight(cell, function);

			setValue(cell, label.toString());
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
