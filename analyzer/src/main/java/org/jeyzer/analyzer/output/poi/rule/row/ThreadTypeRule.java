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

public class ThreadTypeRule  extends AbstractDisplayRule implements RowHeader{

	public static final String RULE_NAME = "thread_type";
	
	private static final String DISPLAY_NAME = "T type";
	
	private static final String DISPLAY_VALUE_NATIVE = "Native";
	private static final String DISPLAY_VALUE_NATIVE_CARRIER = "Native carrier";
	private static final String DISPLAY_VALUE_VIRTUAL = "Virtual";
	private static final String DISPLAY_VALUE_VIRTUAL_UNMOUNTED = "Virtual unmounted";
	
	private boolean hasLink;
	
	public ThreadTypeRule(ConfigDisplay headerCfg, SheetDisplayContext context){
		super(headerCfg, context);
		hasLink = Boolean.parseBoolean((String)headerCfg.getValue(ACTION_LINK_FIELD));
	}

	@Override
	public boolean apply(ThreadAction action, Cell cell) {
		String value;
		
		if (action.isVirtual()) {
			if (action.getThreadStack(0).getState().isUnmountedVirtualThread())
				value = DISPLAY_VALUE_VIRTUAL_UNMOUNTED;
			else
				value = DISPLAY_VALUE_VIRTUAL;
		}
		else {
			if (action.getThreadStack(0).isCarrying())
				value = DISPLAY_VALUE_NATIVE_CARRIER;
			else
				value = DISPLAY_VALUE_NATIVE;
		}
		
		setColorHighlight(cell, action.getName());
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
		return null;
	}

	@Override
	public String getName() {
		return RULE_NAME;
	}

	@Override
	public int getColumnWidth() {
		return 24*256;
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
