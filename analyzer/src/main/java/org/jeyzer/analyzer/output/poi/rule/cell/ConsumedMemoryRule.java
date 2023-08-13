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



import static org.jeyzer.analyzer.math.FormulaHelper.*;





import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.jeyzer.analyzer.config.report.ConfigDisplay;
import org.jeyzer.analyzer.data.Action;
import org.jeyzer.analyzer.data.stack.ThreadStack;
import org.jeyzer.analyzer.output.poi.CellColor;
import org.jeyzer.analyzer.output.poi.context.SequenceSheetDisplayContext;
import org.jeyzer.analyzer.output.stats.Stats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ConsumedMemoryRule extends AbstractCellDisplayRule implements DisplayRule{
	
	private static final Logger logger = LoggerFactory.getLogger(ConsumedMemoryRule.class);		

	public static final String RULE_NAME = "consumed_memory";
	public static final String THRESHOLD_NAME = "threshold";	
	public static final String VALUE_ACTIVITY = "value_activity";
	
	public static final String COLOR_ACTIVITY_NAME = "color_activity";
	
	private static final String NOT_AVAILABLE = "NA";
	private static final String COLOR_NOT_AVAILABLE = "GREY_25_PERCENT";
	
	private int threshold = 100; // default : 100Mb
	
	private boolean valueActivity = false;
	private Object colorActivity = null;
	protected Stats colorActivityStats = null; // Extra stats appearance for memory activity
	
	public ConsumedMemoryRule(ConfigDisplay displayCfg, SequenceSheetDisplayContext context){
		super(displayCfg, context);
		try{
			this.threshold = Integer.parseInt((String)displayCfg.getValue(THRESHOLD_NAME));
		}catch(Exception ex){
			logger.error("Failed to read threshold value for the " + RULE_NAME + " display rule", ex);
		}

		try{
			this.colorActivity = CellColor.buildColor((String)displayCfg.getValue(COLOR_ACTIVITY_NAME));
		}catch(Exception ex){
			logger.error("Failed to read activity color value for the " + RULE_NAME + " display rule", ex);
		}
		
		String value = (String)displayCfg.getValue(VALUE_ACTIVITY);
		if (value != null && !value.isEmpty())
			valueActivity = Boolean.parseBoolean(value);
		
		this.colorActivityStats = duplicateStats();
	}
	
	@Override
	public boolean apply(Action action, List<Cell> cells) {
		Cell cell;
		ThreadStack stack;
		String value;
		long consumedMemory = -1;
		
		// color
		boolean actionThresholdHit = false;
		boolean actionActivityHit = false;
		for (int j=0; j< action.size(); j++){
			cell = cells.get(j);
			stack = action.getThreadStack(j);
			if (stack.getMemoryInfo() == null)
				value = NOT_AVAILABLE;
			else{
				consumedMemory = stack.getMemoryInfo().getAllocatedMemory();
				if (consumedMemory != -1){
					consumedMemory = convertToMb(consumedMemory);
					value = Long.toString(consumedMemory) + " Mb";
				}else{
					value = NOT_AVAILABLE;
				}
			}
			
			if (!valueActivity){
				setValue(cell, value); // always display
			}
			else if (consumedMemory > 0){
				setValue(cell, value); // display only if activity
			}

			if (consumedMemory == -1)
				setColorForeground(cell,COLOR_NOT_AVAILABLE);
			else if (consumedMemory >= this.threshold){
				setColorForeground(cell);
				actionThresholdHit = hitStats(this.stats, actionThresholdHit, stack.getInstanceCount());
				actionActivityHit = hitStats(this.colorActivityStats, actionActivityHit, stack.getInstanceCount());
			}
			else if (colorActivity != null && consumedMemory > 0){
				setColorForeground(cell, this.colorActivity);
				actionActivityHit = hitStats(this.colorActivityStats, actionActivityHit, stack.getInstanceCount());
			}
		}
		
		return true;
	}

	@Override
	public int displayLegend(int line, int pos) {
		return displayLegend("Consumed memory  > " + this.threshold + "Mb", "(value)", true, line, pos);
	}

	@Override
	public int displayStats(int line, int pos) {
		line = super.displayStats(this.stats,"Consumed memory  > " + this.threshold + "Mb", "(value)", true, line, pos);
		line = super.displayStats(this.colorActivityStats,"Consumed memory  > 0 Mb", "(value)", true, line, pos);
		return line;
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
