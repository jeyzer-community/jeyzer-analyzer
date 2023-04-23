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




import static org.jeyzer.analyzer.output.poi.theme.AbstractTheme.STYLE_THEME_HEADER_ROW_NA;

import org.apache.poi.ss.usermodel.Cell;
import org.jeyzer.analyzer.config.report.ConfigDisplay;
import org.jeyzer.analyzer.data.ThreadAction;
import org.jeyzer.analyzer.output.poi.context.SheetDisplayContext;
import org.jeyzer.analyzer.output.poi.rule.AbstractDisplayRule;

public class JeyzerMXContextIdRule  extends AbstractDisplayRule implements RowHeader{

	public static final String RULE_NAME = "jzr_mx_context_id";
	
	private static final String DISPLAY_NAME = "Context Id";
	
	public static final String CELL_LABEL_COMMENT = "Applicative context id obtained through the Jeyzer MX interface";
	
	private boolean hasLink;
	
	public JeyzerMXContextIdRule(ConfigDisplay headerCfg, SheetDisplayContext context){
		super(headerCfg, context);
		hasLink = Boolean.parseBoolean((String)headerCfg.getValue(ACTION_LINK_FIELD));
	}

	@Override
	public boolean apply(ThreadAction action, Cell cell) {
		String value;
		
		if (action.getThreadStackJeyzerMXInfo() != null 
				&& action.getThreadStackJeyzerMXInfo().getId() != null 
				&& !action.getThreadStackJeyzerMXInfo().getId().isEmpty()){
			value = action.getThreadStackJeyzerMXInfo().getId();			
		}
		else{
			value = NOT_AVAILABLE;
			cell.setCellStyle(getThemeStyle(STYLE_THEME_HEADER_ROW_NA));
		}
		
		setColorHighlight(cell, value);
		setValue(cell, value);
		return true;
	}

	@Override
	public int displayLegend(int line, int pos) {
		return displayHighlightLegend(line, pos);
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
		return 12*256;
	}
	
	@Override
	public boolean hasActionLink() {
		return hasLink;
	}
	
	@Override
	public boolean hasLegend() {
		return hasHighlights();
	}
}
