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



import static org.jeyzer.analyzer.output.poi.style.DefaultCellStyles.*;





import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.jeyzer.analyzer.config.report.ConfigDisplay;
import org.jeyzer.analyzer.data.Action;
import org.jeyzer.analyzer.data.stack.ThreadStack;
import org.jeyzer.analyzer.output.poi.context.SequenceSheetDisplayContext;

public class BiasedLockRule extends AbstractCellDisplayRule implements DisplayRule{
	
	public static final String RULE_NAME = "biased_lock";	
	
	public BiasedLockRule(ConfigDisplay displayCfg, SequenceSheetDisplayContext context) {
		super(displayCfg, context);
	}

	@Override
	public boolean apply(Action action, List<Cell> cells) {
		for (int j=0; j< action.size(); j++){
			ThreadStack stack = action.getThreadStack(j);
			
			// update stats
			if (stack.hasBiasedLocks())
				underlineCellText(cells.get(j));
		}
		return true;
	}

	@Override
	public int displayLegend(int line, int pos) {
		Row row;
    	Cell cell;
		
		row = getRow(line);
		
		// code color + name
		cell = row.createCell(pos);
		cell.setCellValue("(Function)");
		underlineCellText(cell);
		setBorders(cell);
		
		// label
		cell = row.createCell(pos+1);
		cell.setCellValue("  Biased lock");
		cell.setCellStyle(getStyle(STYLE_CELL_SMALL_TEXT_LEFT_ALIGNED));
		
		return ++line;
	}

	@Override
	public int displayStats(int line, int pos) {
		return line;
	}

	@Override
	public boolean hasLegend() {
		return true;
	}

	@Override
	public boolean hasStats() {
		return false;
	}

	@Override
	public String getName() {
		return RULE_NAME;
	}

}
 
