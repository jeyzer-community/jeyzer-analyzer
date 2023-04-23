package org.jeyzer.analyzer.output.poi.rule.row;

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



import static org.jeyzer.analyzer.math.FormulaHelper.DOUBLE_TO_LONG_NA;





import org.apache.poi.ss.usermodel.Cell;
import org.jeyzer.analyzer.config.report.ConfigDisplay;
import org.jeyzer.analyzer.data.ThreadAction;
import org.jeyzer.analyzer.output.poi.CellColor;
import org.jeyzer.analyzer.output.poi.context.SheetDisplayContext;
import org.jeyzer.analyzer.output.poi.rule.AbstractNumericDisplayRule;

public class ActionConsumedCPURule extends AbstractNumericDisplayRule implements RowHeader {

	public static final String RULE_NAME = "action_consumed_cpu";
	
	public static final String CELL_LABEL_COMMENT = "CPU usage of the current action";
	
	private static final String DISPLAY_NAME = "CPU";
	
	private static final String COLOR_NOT_AVAILABLE = "GREY_25_PERCENT";
	private static final String COLOR_ACTIVITY_NAME = "color_activity";
	
	private boolean hasLink;
	private Object colorActivity = null;
	
	public ActionConsumedCPURule(ConfigDisplay headerCfg, SheetDisplayContext context) {
		super(headerCfg, context);
		hasLink = Boolean.parseBoolean((String)headerCfg.getValue(ACTION_LINK_FIELD));
		
		try{
			this.colorActivity = CellColor.buildColor((String)headerCfg.getValue(COLOR_ACTIVITY_NAME));
		}catch(Exception ex){
			logger.error("Failed to read activity color value for the " + RULE_NAME + " row header rule", ex);
		}
	}

	@Override
	public boolean apply(ThreadAction action, Cell cell) {
		String value;
		double consumedCpu = -1;
		
		// color
		if (Double.doubleToRawLongBits(action.getCpuUsage()) == DOUBLE_TO_LONG_NA)
			value = NOT_AVAILABLE;
		else{
			consumedCpu = action.getCpuUsage();
			consumedCpu = Math.round(consumedCpu);
			value = Double.toString(consumedCpu) + " %";
		}
			
		setValue(cell, value);
		
		if (Double.doubleToRawLongBits(consumedCpu) == DOUBLE_TO_LONG_NA)
			setColorForeground(cell,COLOR_NOT_AVAILABLE);
		else if (consumedCpu >= this.threshold)
			setColorForeground(cell);
		else if (colorActivity != null && consumedCpu > 0)
			setColorForeground(cell, this.colorActivity);
		
		return true;
	}

	@Override
	public int displayLegend(int line, int pos) {
		return line;
	}

	@Override
	public String getDisplayName() {
		return DISPLAY_NAME;
	}

	@Override
	public String getComment() {
		return CELL_LABEL_COMMENT;
	}

	@Override
	public int getColumnWidth() {
		return 10*256;
	}

	@Override
	public boolean hasActionLink() {
		return hasLink;
	}

	@Override
	public boolean hasLegend() {
		return false;
	}

	@Override
	public boolean isPercentageBased() {
		return true;
	}

	@Override
	public String getName() {
		return RULE_NAME;
	}

}
