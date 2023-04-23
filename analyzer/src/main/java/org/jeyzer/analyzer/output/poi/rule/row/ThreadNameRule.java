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
import org.jeyzer.analyzer.output.poi.context.SheetDisplayContext;
import org.jeyzer.analyzer.output.poi.rule.AbstractDisplayRule;

public class ThreadNameRule  extends AbstractDisplayRule implements RowHeader{

	public static final String RULE_NAME = "thread_name";
	
	private static final String DISPLAY_NAME = "Thread name";
	
	private boolean hasLink;
	
	public ThreadNameRule(ConfigDisplay headerCfg, SheetDisplayContext context){
		super(headerCfg, context);
		hasLink = Boolean.parseBoolean((String)headerCfg.getValue(ACTION_LINK_FIELD));
	}

	@Override
	public boolean apply(ThreadAction action, Cell cell) {
		setColorHighlight(cell, action.getName());
		setValue(cell, action.getName());
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
		return null;
	}

	@Override
	public String getName() {
		return RULE_NAME;
	}

	@Override
	public int getColumnWidth() {
		return 44*256;
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
