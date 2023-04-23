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
import org.jeyzer.analyzer.data.ThreadAction;

public interface RowHeader {
	
	public static final String ACTION_LINK_FIELD = "action_link";
	
	public boolean apply(ThreadAction action, Cell cell);
	
	public int displayLegend(int line, int pos);
	
	public String getName();
	
	public String getDisplayName();
	
	public String getComment();
	
	public int getColumnWidth();
	
	public boolean hasActionLink();
	
	public boolean hasLegend();
	
}
