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





import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.jeyzer.analyzer.config.report.ConfigDisplay;
import org.jeyzer.analyzer.output.poi.context.SequenceSheetDisplayContext;
import org.jeyzer.analyzer.output.poi.rule.AbstractDisplayRule;
import org.jeyzer.analyzer.output.stats.CollectedStats;
import org.jeyzer.analyzer.output.stats.Stats;
import org.jeyzer.analyzer.output.stats.VoidStats;

public abstract class AbstractCellDisplayRule extends AbstractDisplayRule {
	
	protected Stats stats = null; // Stats apperance for that type of cells
	
	public AbstractCellDisplayRule(ConfigDisplay displayCfg, SequenceSheetDisplayContext context) {
		super(displayCfg, context);
		
		if (hasStats() && Boolean.parseBoolean((String)displayCfg.getValue(STATS_FIELD)) && context.hasStatsSupport())
			stats = (Stats)context.createStats();
		else
			stats = VoidStats.VOID_STATS;
	}
	
	public boolean hasStatsToDisplay(){
		return hasStats() && stats != VoidStats.VOID_STATS;
	}
	
	public abstract boolean hasStats();
	
	protected int displayStats(Stats statsToDisplay, String description, String label, boolean color, int line, int pos) {
		Row row;
		Cell cell;
		
		row = getRow(line);
		
		// code color + name
		cell = row.createCell(pos);
		if (color)
			setColorForeground(cell);
		if(label != null)
			cell.setCellValue(label);
		setBorders(cell);
		
		// action %
		addStatsCell(row, pos+1, statsToDisplay.getActionPercentage(), STYLE_CELL_SMALL_NUMBER);
		
		// action count
		addStatsCell(row, pos+2, statsToDisplay.getActionCount(), STYLE_CELL_SMALL_NUMBER);
		
		// stack %
		addStatsCell(row, pos+3, statsToDisplay.getStackPercentage(), STYLE_CELL_SMALL_NUMBER);
		
		// stack count
		addStatsCell(row, pos+4, statsToDisplay.getStackCount(), STYLE_CELL_SMALL_NUMBER);
		
		// label
		addStatsCell(row, pos+5, "  " + description, STYLE_CELL_SMALL_TEXT_LEFT_ALIGNED);
				
		return ++line;
	}
	
	protected Cell addStatsCell(Row row, int pos, String text, String style){
		Cell cell = row.createCell(pos);
		cell.setCellValue(text);
		cell.setCellStyle(getStyle(style));
		return cell;
	}
	
	protected Cell addStatsCell(Row row, int pos, int value, String style){
		Cell cell = row.createCell(pos);
		cell.setCellValue(value);
		cell.setCellStyle(getStyle(style));
		return cell;
	}	
	
	protected Stats duplicateStats(){
		if (this.stats != VoidStats.VOID_STATS){
			return new CollectedStats((CollectedStats)this.stats); 
		}else{
			return VoidStats.VOID_STATS;
		}
	}
	
	protected boolean hitStats(Stats statsToUpdate, boolean actionAlreadyHit, int stackCount){
		statsToUpdate.hitStack(stackCount);
		if (!actionAlreadyHit){
			statsToUpdate.hitAction();
		}
		return true; // action hit
	}

}
