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
import org.jeyzer.analyzer.data.stack.ThreadStack;
import org.jeyzer.analyzer.output.poi.context.SheetDisplayContext;
import org.jeyzer.analyzer.output.poi.rule.AbstractDisplayRule;

public class GroupLockStateRule extends AbstractDisplayRule implements GroupRowHeader {

	public static final String RULE_NAME = "lock_state";
	
	public static final String CELL_LABEL_COMMENT = "Lock state\n"
			+ "  D = deadlock\n"
			+ "  L = locked / lock owner\n"
			+ "  S = suspended on debug breakpoint";
	
	private static final String DISPLAY_NAME = "Lock";
	
	private boolean hasLink;
	
	public GroupLockStateRule(ConfigDisplay headerCfg, SheetDisplayContext context) {
		super(headerCfg, context);
		hasLink = Boolean.parseBoolean((String)headerCfg.getValue(GROUP_LINK_FIELD));
	}

	@Override
	public boolean apply(ThreadStackGroupAction action, Cell cell) {
		ThreadStack ts;
		String value = null;
		
		for (int j=0; j< action.size(); j++){
			ts = action.getThreadStack(j);
			
			boolean locked = ts.isLocked();
			boolean locking = !ts.getLockedThreads().isEmpty();

			if (ts.isInDeadlock()){
				value = "D";
				break; // deadlock D gets priority over "L"
			}
			else if ((locked || locking) && value == null){
				value = "L";
			}
			else if (ts.isSuspended()){
				value = "S";
			}
		}
		
		if (value != null){
			setColorHighlight(cell, value);
			setValue(cell, value);
		}
		
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
	public int getColumnWidth() {
		return 9*256;
	}

	@Override
	public boolean hasActionLink() {
		return hasLink;
	}

	@Override
	public boolean hasLegend() {
		return true;
	}

	@Override
	public String getName() {
		return RULE_NAME;
	}

}
