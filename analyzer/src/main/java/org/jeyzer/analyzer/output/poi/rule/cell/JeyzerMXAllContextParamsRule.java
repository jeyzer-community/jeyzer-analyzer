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
import java.util.Map;
import java.util.Map.Entry;

import org.apache.poi.ss.usermodel.Cell;
import org.jeyzer.analyzer.config.report.ConfigDisplay;
import org.jeyzer.analyzer.data.Action;
import org.jeyzer.analyzer.output.poi.CellText;
import org.jeyzer.analyzer.output.poi.context.SequenceSheetDisplayContext;


public class JeyzerMXAllContextParamsRule extends AbstractCellDisplayRule implements DisplayRule{

	public static final String RULE_NAME = "jzr_mx_all_context_params";
	
	public JeyzerMXAllContextParamsRule (ConfigDisplay displayCfg, SequenceSheetDisplayContext context){
		super(displayCfg, context);
	}	
	
	@Override
	public boolean apply(Action action, List<Cell> cells) {
		Cell cell;

		// optimization
		if (action.getThreadStackJeyzerMXInfo() == null)
			return false;
		
		// cell value
		for (int j=0; j< action.size(); j++){
			StringBuilder valueBuilder = null;
			cell = cells.get(j);
			int depth = 0;
			int maxLength = CellText.COMMENT_COLUMN_CHARS_SIZE;

			if (action.getThreadStack(j).getThreadStackJeyzerMXInfo() != null){
				Map<String, String> params = action.getThreadStack(j).getThreadStackJeyzerMXInfo().getAllContextParams();
				if (!params.isEmpty()){
					valueBuilder = new StringBuilder(50*params.size());
					depth = params.size();
					for (Entry<String, String> entry : params.entrySet()){
						valueBuilder.append(entry.getKey());
						valueBuilder.append(" = ");
						valueBuilder.append(entry.getValue());
						valueBuilder.append("\n");

						int length = entry.getKey().length() + entry.getValue().length() + 1;
						if (length > maxLength)
							maxLength = length;
					}
				}
				
				if (valueBuilder != null){
					int commentLength = maxLength / CellText.COMMENT_COLUMN_CHARS_SIZE;
					addComment(cell, valueBuilder.toString(), depth, commentLength);
				}
			}
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
