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



import static org.jeyzer.analyzer.output.poi.style.DefaultCellStyles.STYLE_CELL_MX_PARAM_NA;





import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.jeyzer.analyzer.config.report.ConfigDisplay;
import org.jeyzer.analyzer.data.Action;
import org.jeyzer.analyzer.output.poi.context.SequenceSheetDisplayContext;
import org.jeyzer.analyzer.output.poi.rule.AbstractDisplayRule;

public class JeyzerMXContextParamStringRule extends AbstractCellDisplayRule implements DisplayRule{

	public static final String RULE_NAME = "jzr_mx_context_param";
	
	private static final String PARAM_NAME = "name";
	
	private String paramName;
	
	public JeyzerMXContextParamStringRule (ConfigDisplay displayCfg, SequenceSheetDisplayContext context){
		super(displayCfg, context);
		
		this.paramName = (String)displayCfg.getValue(PARAM_NAME);
	}	
	
	@Override
	public boolean apply(Action action, List<Cell> cells) {
		Cell cell;
		String value;
		
		// cell value
		for (int j=0; j< action.size(); j++){
			value = null;
			cell = cells.get(j);
			
			if (action.getThreadStack(j).getThreadStackJeyzerMXInfo() != null)
				value = action.getThreadStack(j).getThreadStackJeyzerMXInfo().getContextParam(paramName);

			if(value == null){
				value = AbstractDisplayRule.NOT_AVAILABLE;
				cell.setCellStyle(this.getStyle(STYLE_CELL_MX_PARAM_NA));
			}
			else{
				setColorHighlight(cell, value);
			}
			
			setValue(cell, value);
		}
		
		return true;
	}

	@Override
	public int displayLegend(int line, int pos) {
		// do nothing
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
		return false;  // see FunctionHistogram sheet for stats
	}

	@Override
	public boolean hasStatsToDisplay() {
		return false;
	}

}
