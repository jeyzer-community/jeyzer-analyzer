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







import org.apache.poi.ss.usermodel.Cell;
import org.jeyzer.analyzer.config.report.ConfigDisplay;
import org.jeyzer.analyzer.data.ThreadAction;
import org.jeyzer.analyzer.data.stack.ThreadStack;
import org.jeyzer.analyzer.output.poi.context.SheetDisplayContext;
import org.jeyzer.analyzer.output.poi.rule.AbstractNumericDisplayRule;

public class FrozenCodeStateRule  extends AbstractNumericDisplayRule implements RowHeader {

	public static final String RULE_NAME = "frozen_code_state";
	
	public static final String CELL_LABEL_COMMENT = "Frozen code stack : number of stacks which were consecutively identical";
	
	private static final String DISPLAY_NAME = "#Froz";
	
	private boolean hasLink;
	
	public FrozenCodeStateRule(ConfigDisplay headerCfg, SheetDisplayContext context) {
		super(headerCfg, context);
		hasLink = Boolean.parseBoolean((String)headerCfg.getValue(ACTION_LINK_FIELD));
	}

	@Override
	public boolean apply(ThreadAction action, Cell cell) {
		ThreadStack ts;
		Long value = (long) 0;
		
		for (int j=0; j< action.size(); j++){
			ts = action.getThreadStack(j);
			if (ts.isFrozenStackCode())
				value++;
		}
		
		if (value != null && value>0){
			setValueBasedColorForeground(cell, value, -1, false);
			setValue(cell, value);
		}
		
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
		return 9*256;
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
	public String getName() {
		return RULE_NAME;
	}

	@Override
	public boolean isPercentageBased() {
		return false;
	}

}
