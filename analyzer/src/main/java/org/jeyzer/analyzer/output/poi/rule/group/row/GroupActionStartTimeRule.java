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


import static org.jeyzer.analyzer.output.poi.theme.AbstractTheme.STYLE_THEME_HEADER_DATE_ROW;

import org.apache.poi.ss.usermodel.Cell;
import org.jeyzer.analyzer.config.report.ConfigDisplay;
import org.jeyzer.analyzer.data.ThreadStackGroupAction;
import org.jeyzer.analyzer.output.poi.context.SheetDisplayContext;
import org.jeyzer.analyzer.output.poi.rule.AbstractDisplayRule;

public class GroupActionStartTimeRule extends AbstractDisplayRule implements GroupRowHeader{

	public static final String RULE_NAME = "action_start_time";
	
	private static final String DISPLAY_NAME = "Start time";
	
	private boolean hasLink;
	
	public GroupActionStartTimeRule(ConfigDisplay headerCfg, SheetDisplayContext context){
		super(headerCfg, context);
		hasLink = Boolean.parseBoolean((String)headerCfg.getValue(GROUP_LINK_FIELD));
	}

	@Override
	public boolean apply(ThreadStackGroupAction action, Cell cell) {
		cell.setCellStyle(this.context.getCellStyles().getThemeStyles().getThemeStyle(STYLE_THEME_HEADER_DATE_ROW));		
		cell.setCellValue(action.getStartDate());
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
		return null;
	}

	@Override
	public String getName() {
		return RULE_NAME;
	}

	@Override
	public int getColumnWidth() {
		return 18*256;
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
