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

public class FrozenCodeStateRule extends AbstractCellDisplayRule implements DisplayRule {

	public static final String RULE_NAME = "frozen_code_state";
	
	public FrozenCodeStateRule(ConfigDisplay displayCfg, SequenceSheetDisplayContext context) {
		super(displayCfg, context);
	}
	
	@Override
	public boolean apply(Action action, List<Cell> cells) {
		boolean actionFrozenHit = false;
		for (int j=0; j< action.size(); j++){
			ThreadStack stack = action.getThreadStack(j);
			
			// update stats
			if (stack.isFrozenStackCode())
				actionFrozenHit = hitStats(this.stats, actionFrozenHit);
			
			switch (stack.getStackCodeState()){
				case FREEZE_BEGIN :
					setBorders(cells.get(j), this.color, true, false, true, true);
					break;
				case FREEZE_MIDDLE:
					setBorders(cells.get(j), this.color, false, false, true, true);
					break;
				case FREEZE_END:	
					setBorders(cells.get(j), this.color, false, true, true, true);
					break;
				default:
					// do nothing
			}
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
		setBorders(cell, this.color, true, true, true, true);
		
		// label
		cell = row.createCell(pos+1);
		cell.setCellValue("  Frozen code stack");
		cell.setCellStyle(getStyle(STYLE_CELL_SMALL_TEXT_LEFT_ALIGNED));
		
		return ++line;
	}

	@Override
	public int displayStats(int line, int pos) {
		Row row;
		Cell cell;
		
		row = this.context.getSheet().getRow(line);
		if (row == null)
			row = this.context.getSheet().createRow(line);
			
		// code color + name
		cell = row.createCell(pos);
		setBorders(cell, this.color, true, true, true, true);
		
		// action %
		addStatsCell(row, pos+1, this.stats.getActionPercentage(), STYLE_CELL_SMALL_NUMBER);
		
		// action count
		addStatsCell(row, pos+2, this.stats.getActionCount(), STYLE_CELL_SMALL_NUMBER);
		
		// stack %
		addStatsCell(row, pos+3, this.stats.getStackPercentage(), STYLE_CELL_SMALL_NUMBER);
		
		// stack count
		addStatsCell(row, pos+4, this.stats.getStackCount(), STYLE_CELL_SMALL_NUMBER);
		
		// label
		addStatsCell(row, pos+5, "  Frozen code stack", STYLE_CELL_SMALL_TEXT_LEFT_ALIGNED);
		
		return ++line;
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
