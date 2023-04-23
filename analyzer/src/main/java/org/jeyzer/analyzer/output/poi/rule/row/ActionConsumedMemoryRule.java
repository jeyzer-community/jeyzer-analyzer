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




import static org.jeyzer.analyzer.math.FormulaHelper.convertToMb;

import org.apache.poi.ss.usermodel.Cell;
import org.jeyzer.analyzer.config.report.ConfigDisplay;
import org.jeyzer.analyzer.data.ThreadAction;
import org.jeyzer.analyzer.output.poi.CellColor;
import org.jeyzer.analyzer.output.poi.context.SheetDisplayContext;
import org.jeyzer.analyzer.output.poi.rule.AbstractNumericDisplayRule;

public class ActionConsumedMemoryRule extends AbstractNumericDisplayRule implements RowHeader{

	public static final String RULE_NAME = "action_consumed_memory";
	
	public static final String CELL_LABEL_COMMENT = "Allocated memory in Mb for the current action";
	
	private static final String DISPLAY_NAME = "Mem";
	
	private static final String COLOR_NOT_AVAILABLE = "GREY_25_PERCENT";
	private static final String COLOR_ACTIVITY_NAME = "color_activity";
	
	private boolean hasLink;
	private Object colorActivity = null;
	
	public ActionConsumedMemoryRule(ConfigDisplay headerCfg, SheetDisplayContext context){
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
		long consumedMemory = -1;
		
		// color
		if (action.getAllocatedMemory() == -1)
			value = NOT_AVAILABLE;
		else{
			consumedMemory = action.getAllocatedMemory();
			consumedMemory = convertToMb(consumedMemory);
			value = Long.toString(consumedMemory) + " Mb";
		}
			
		setValue(cell, value);

		if (consumedMemory == -1)
			setColorForeground(cell,COLOR_NOT_AVAILABLE);
		else if (consumedMemory >= this.threshold)
			setColorForeground(cell);
		else if (colorActivity != null && consumedMemory > 0)
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
	public String getName() {
		return RULE_NAME;
	}

	@Override
	public int getColumnWidth() {
		return 10*256;
	}

	@Override
	public boolean isPercentageBased() {
		return false;
	}

	@Override
	public boolean hasActionLink() {
		return hasLink;
	}

	@Override
	public boolean hasLegend() {
		return false;
	}
	
}
