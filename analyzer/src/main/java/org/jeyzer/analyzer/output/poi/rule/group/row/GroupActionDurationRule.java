package org.jeyzer.analyzer.output.poi.rule.group.row;

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
import org.jeyzer.analyzer.data.ThreadStackGroupAction;
import org.jeyzer.analyzer.output.poi.context.SheetDisplayContext;
import org.jeyzer.analyzer.output.poi.rule.AbstractNumericDisplayRule;

public class GroupActionDurationRule extends AbstractNumericDisplayRule implements GroupRowHeader{

	public static final String RULE_NAME = "action_duration";
	
	public static final String CELL_LABEL_COMMENT = "Similar stacks minimum duration in seconds";
	
	private static final String DISPLAY_NAME = "Duration";
	
	private boolean hasLink;
	
	public GroupActionDurationRule(ConfigDisplay headerCfg, SheetDisplayContext context){
		super(headerCfg, context);
		hasLink = Boolean.parseBoolean((String)headerCfg.getValue(GROUP_LINK_FIELD));
	}

	@Override
	public boolean apply(ThreadStackGroupAction action, Cell cell) {
		Long value = action.getMinDuration();
		setValue(cell, value);
		setValueBasedColorForeground(cell, value, -1, false);
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
