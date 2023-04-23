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

public class JeyzerMXContextParamStringRule extends AbstractDisplayRule implements RowHeader{

	public static final String RULE_NAME = "jzr_mx_context_param";

	private static final String DISPLAY_NAME = "display";
	private static final String DISPLAY_COMMENT_NAME = "display_comment";
	private static final String PARAM_NAME = "name";
	
	private String displayName;
	private String displayCommentName;
	private String paramName;
	
	private boolean hasLink;
	
	public JeyzerMXContextParamStringRule(ConfigDisplay headerCfg, SheetDisplayContext context) {
		super(headerCfg, context);
		this.displayName = (String)headerCfg.getValue(DISPLAY_NAME);
		this.displayCommentName = (String)headerCfg.getValue(DISPLAY_COMMENT_NAME);
		this.paramName = (String)headerCfg.getValue(PARAM_NAME);
		
		this.hasLink = Boolean.parseBoolean((String)headerCfg.getValue(ACTION_LINK_FIELD));
	}

	@Override
	public boolean apply(ThreadAction action, Cell cell) {
		String value; // NA
		String paramValue;
		
		// cell value
		if (action.getThreadStackJeyzerMXInfo()!=null){
			paramValue = action.getThreadStackJeyzerMXInfo().getContextParam(paramName);
			if(paramValue != null){
				value = paramValue; 
			}
			else{
				value = AbstractDisplayRule.NOT_AVAILABLE;
				cell.setCellStyle(getThemeStyle(STYLE_THEME_HEADER_ROW_NA));
			}
		}
		else{
			value = AbstractDisplayRule.NOT_AVAILABLE;
			cell.setCellStyle(getThemeStyle(STYLE_THEME_HEADER_ROW_NA));
		}
		
		setValue(cell, value);
		
		return true;
	}

	@Override
	public int displayLegend(int line, int pos) {
		return line;
	}

	@Override
	public String getName() {
		return RULE_NAME + "-" + paramName;
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}
	
	@Override
	public String getComment() {
		return displayCommentName;
	}

	@Override
	public int getColumnWidth() {
		return 25*256;
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
