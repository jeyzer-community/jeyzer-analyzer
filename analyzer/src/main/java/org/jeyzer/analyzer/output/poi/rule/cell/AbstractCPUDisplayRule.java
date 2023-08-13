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
import org.jeyzer.analyzer.output.poi.context.SequenceSheetDisplayContext;
import org.jeyzer.analyzer.output.poi.rule.highlight.Highlight;
import org.jeyzer.analyzer.output.poi.rule.highlight.Highlight.HighlightLegendElement;
import org.jeyzer.analyzer.output.stats.Stats;

public abstract class AbstractCPUDisplayRule extends AbstractCellDisplayRule {
	
	public static final String VALUE_ACTIVITY = "value_activity";
	
	protected static final String DESCRIPTION_PREFIX = "CPU consumption : ";
	
	protected static final String CPU_RANGE_01_15 = "01-15%";
	protected static final String CPU_RANGE_16_30 = "16-30%";
	protected static final String CPU_RANGE_31_40 = "31-40%";
	protected static final String CPU_RANGE_41_50 = "41-50%";
	protected static final String CPU_RANGE_51_75 = "51-75%";
	protected static final String CPU_RANGE_76_100 = "76-100%";	
	
	protected static final String LABEL_PERCENT = "%";
	protected static final String LABEL_PERCENT_CARRIAGE_RETURN = "%\n";
	protected static final String LABEL_CPU_USAGE = "cpu:";
	protected static final String LABEL_SYS_USAGE = "sys:";
	protected static final String LABEL_USR_USAGE = "usr:";
	protected static final String LABEL_ACT_USAGE = "act:";
	
	protected Stats cpuRange_01_15_Stats = null; // Extra stats appearance for CPU activity
	protected Stats cpuRange_16_30_Stats = null; // Extra stats appearance for CPU activity
	protected Stats cpuRange_31_40_Stats = null; // Extra stats appearance for CPU activity
	protected Stats cpuRange_41_50_Stats = null; // Extra stats appearance for CPU activity
	protected Stats cpuRange_51_75_Stats = null; // Extra stats appearance for CPU activity
	protected Stats cpuRange_76_100_Stats = null; // Extra stats appearance for CPU activity
	
	protected boolean valueActivity = false;
	
	public AbstractCPUDisplayRule(ConfigDisplay displayCfg, SequenceSheetDisplayContext context){
		super(displayCfg, context);
		
		String value = (String)displayCfg.getValue(VALUE_ACTIVITY);
		if (value != null && !value.isEmpty())
			valueActivity = Boolean.parseBoolean(value);
		
		this.cpuRange_01_15_Stats = duplicateStats();
		this.cpuRange_16_30_Stats = duplicateStats();
		this.cpuRange_31_40_Stats = duplicateStats();
		this.cpuRange_41_50_Stats = duplicateStats();
		this.cpuRange_51_75_Stats = duplicateStats();
		this.cpuRange_76_100_Stats = duplicateStats();
	}
	
	protected void displaySingleValue(Cell cell, String label, int value, ActionStatsContext actionStatsContext, int stackCount){
		StringBuilder cpuTimes;
		
		if (valueActivity && value <= 0)
			return;
		
		cpuTimes = new StringBuilder();
		cpuTimes.append(label);
		cpuTimes.append(value);
		cpuTimes.append(LABEL_PERCENT);

		setValue(cell, cpuTimes.toString());
		
		if (value == 0){ // non active, no color
			return;
		}
		
		updateStats(value, actionStatsContext, stackCount);
		
		setColorHighlight(cell, getRange(value));
	}
	
	protected String getRange(int cpuPercent){
		if (cpuPercent < 16)
			return CPU_RANGE_01_15;
		else if (cpuPercent < 31)
			return CPU_RANGE_16_30;
		else if (cpuPercent < 41)
			return CPU_RANGE_31_40;
		else if (cpuPercent < 51)
			return CPU_RANGE_41_50;
		else if (cpuPercent < 76)
			return CPU_RANGE_51_75;
		else
			return CPU_RANGE_76_100;
	}
	
	protected Stats getStats(String range){
		if (CPU_RANGE_01_15.equals(range))
			return cpuRange_01_15_Stats;
		if (CPU_RANGE_16_30.equals(range))
			return cpuRange_16_30_Stats;
		if (CPU_RANGE_31_40.equals(range))
			return cpuRange_31_40_Stats;
		if (CPU_RANGE_41_50.equals(range))
			return cpuRange_41_50_Stats;
		if (CPU_RANGE_51_75.equals(range))
			return cpuRange_51_75_Stats;

		return cpuRange_76_100_Stats;
	}	

	public int displayStats(int line, int pos) {
		Row row;
    	Cell cell;
    	
    	for (Highlight hl : this.highlights){
    		
    		List<HighlightLegendElement> legendElements = hl.getLegendElements();
    		
    		for (HighlightLegendElement legendElement : legendElements){
        		row = getRow(line);

        		String description = legendElement.getDescription();
        		Stats statsToDisplay = getStats(description);
        		
        		// code color + name
        		cell = row.createCell(pos);
        		this.setColorForeground(cell, hl.getColor());
        		setBorders(cell);
        		
        		// label
        		if(legendElement.getLabel() != null)
        			cell.setCellValue(legendElement.getLabel());

        		// action %
        		addStatsCell(row, pos+1, statsToDisplay.getActionPercentage(), STYLE_CELL_SMALL_NUMBER);
        		
        		// action count
        		addStatsCell(row, pos+2, statsToDisplay.getActionCount(), STYLE_CELL_SMALL_NUMBER);
        		
        		// stack %
        		addStatsCell(row, pos+3, statsToDisplay.getStackPercentage(), STYLE_CELL_SMALL_NUMBER);
        		
        		// stack count
        		addStatsCell(row, pos+4, statsToDisplay.getStackCount(), STYLE_CELL_SMALL_NUMBER);
        		
        		// label
        		addStatsCell(row, pos+5, "  " + DESCRIPTION_PREFIX + description, STYLE_CELL_SMALL_TEXT_LEFT_ALIGNED);
    			
        		++line;
    		}
    	}
    	
    	return line; 
	}
	
	private void updateStats(int cpuPercent, ActionStatsContext actionStatsContext, int stackCount) {
		if (cpuPercent < 16){
			actionStatsContext.cpuRange_01_15_StatsActionHit = hitStats(cpuRange_01_15_Stats, actionStatsContext.cpuRange_01_15_StatsActionHit, stackCount);
		}
		else if (cpuPercent < 31){
			actionStatsContext.cpuRange_01_15_StatsActionHit = hitStats(cpuRange_01_15_Stats, actionStatsContext.cpuRange_01_15_StatsActionHit, stackCount);
			actionStatsContext.cpuRange_16_30_StatsActionHit = hitStats(cpuRange_16_30_Stats, actionStatsContext.cpuRange_16_30_StatsActionHit, stackCount);
		}
		else if (cpuPercent < 41){
			actionStatsContext.cpuRange_01_15_StatsActionHit = hitStats(cpuRange_01_15_Stats, actionStatsContext.cpuRange_01_15_StatsActionHit, stackCount);
			actionStatsContext.cpuRange_16_30_StatsActionHit = hitStats(cpuRange_16_30_Stats, actionStatsContext.cpuRange_16_30_StatsActionHit, stackCount);
			actionStatsContext.cpuRange_31_10_StatsActionHit = hitStats(cpuRange_31_40_Stats, actionStatsContext.cpuRange_31_10_StatsActionHit, stackCount);
		}
		else if (cpuPercent < 51){
			actionStatsContext.cpuRange_01_15_StatsActionHit = hitStats(cpuRange_01_15_Stats, actionStatsContext.cpuRange_01_15_StatsActionHit, stackCount);
			actionStatsContext.cpuRange_16_30_StatsActionHit = hitStats(cpuRange_16_30_Stats, actionStatsContext.cpuRange_16_30_StatsActionHit, stackCount);
			actionStatsContext.cpuRange_31_10_StatsActionHit = hitStats(cpuRange_31_40_Stats, actionStatsContext.cpuRange_31_10_StatsActionHit, stackCount);
			actionStatsContext.cpuRange_40_50_StatsActionHit = hitStats(cpuRange_41_50_Stats, actionStatsContext.cpuRange_40_50_StatsActionHit, stackCount);
		}
		else if (cpuPercent < 76){
			actionStatsContext.cpuRange_01_15_StatsActionHit = hitStats(cpuRange_01_15_Stats, actionStatsContext.cpuRange_01_15_StatsActionHit, stackCount);
			actionStatsContext.cpuRange_16_30_StatsActionHit = hitStats(cpuRange_16_30_Stats, actionStatsContext.cpuRange_16_30_StatsActionHit, stackCount);
			actionStatsContext.cpuRange_31_10_StatsActionHit = hitStats(cpuRange_31_40_Stats, actionStatsContext.cpuRange_31_10_StatsActionHit, stackCount);
			actionStatsContext.cpuRange_40_50_StatsActionHit = hitStats(cpuRange_41_50_Stats, actionStatsContext.cpuRange_40_50_StatsActionHit, stackCount);
			actionStatsContext.cpuRange_51_75_StatsActionHit = hitStats(cpuRange_51_75_Stats, actionStatsContext.cpuRange_51_75_StatsActionHit, stackCount);
		}
		else{
			actionStatsContext.cpuRange_01_15_StatsActionHit = hitStats(cpuRange_01_15_Stats, actionStatsContext.cpuRange_01_15_StatsActionHit, stackCount);
			actionStatsContext.cpuRange_16_30_StatsActionHit = hitStats(cpuRange_16_30_Stats, actionStatsContext.cpuRange_16_30_StatsActionHit, stackCount);
			actionStatsContext.cpuRange_31_10_StatsActionHit = hitStats(cpuRange_31_40_Stats, actionStatsContext.cpuRange_31_10_StatsActionHit, stackCount);
			actionStatsContext.cpuRange_40_50_StatsActionHit = hitStats(cpuRange_41_50_Stats, actionStatsContext.cpuRange_40_50_StatsActionHit, stackCount);
			actionStatsContext.cpuRange_51_75_StatsActionHit = hitStats(cpuRange_51_75_Stats, actionStatsContext.cpuRange_51_75_StatsActionHit, stackCount);
			actionStatsContext.cpuRange_76_100_StatsActionHit = hitStats(cpuRange_76_100_Stats, actionStatsContext.cpuRange_76_100_StatsActionHit, stackCount);
		}
	}
	
	public static class ActionStatsContext{
		boolean cpuRange_01_15_StatsActionHit;
		boolean cpuRange_16_30_StatsActionHit;
		boolean cpuRange_31_10_StatsActionHit;
		boolean cpuRange_40_50_StatsActionHit;
		boolean cpuRange_51_75_StatsActionHit;
		boolean cpuRange_76_100_StatsActionHit;
	}
	
}
