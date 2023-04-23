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
import org.jeyzer.analyzer.data.stack.ThreadStack;
import org.jeyzer.analyzer.output.poi.context.SequenceSheetDisplayContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LongStackRule extends AbstractCellDisplayRule implements DisplayRule{
	
	private static final Logger logger = LoggerFactory.getLogger(LongStackRule.class);

	public static final String RULE_NAME = "long_stack";
	
	public static final String THRESHOLD_NAME = "threshold";
	public static final String SIZE_NAME = "font_size";
	public static final String ATBI_ONLY_NAME = "atbi_only";
	
	private int threshold = 15; // default
	private short size = 15; // default
	private boolean atbiOnly;
	
	public LongStackRule(ConfigDisplay displayCfg, SequenceSheetDisplayContext context){
		super(displayCfg, context);
		try{
			this.threshold = Integer.parseInt((String)displayCfg.getValue(THRESHOLD_NAME));
		}catch(Exception ex){
			logger.error("Failed to read threshold value for the long_stack display rule", ex);
		}
		if (this.threshold < 2)
			this.threshold = 2;
		
		try{
			this.size = Short.parseShort((String)displayCfg.getValue(SIZE_NAME));
		}catch(Exception ex){
			logger.error("Failed to read the font size value for the long_stack display rule", ex);
		}
		if (this.threshold < 10)
			this.threshold = 10;
		
		this.atbiOnly = Boolean.parseBoolean((String)displayCfg.getValue(ATBI_ONLY_NAME));
	}	
	
	@Override
	public boolean apply(Action action, List<Cell> cells) {
		for (int j=0; j< action.size(); j++){
			ThreadStack stack = action.getThreadStack(j);
			if (this.atbiOnly && !stack.isATBI())
				continue;
			if (stack.getStackHandler().getCodeLines().size() >= threshold)
				changeCellTextSize(cells.get(j), size);
		}
		
		return true;
	}

	@Override
	public int displayLegend(int line, int pos) {
		String message = (this.atbiOnly ? "ATBI stack" : "Stack") + " with size > " + this.threshold + " lines";
		return displayLegend(message, this.atbiOnly ? "Long ATBI stack" : "Long stack", true, this.size, line, pos);
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
		return true;
	}
	
	@Override
	public boolean hasStats() {
		return false;
	}
}
