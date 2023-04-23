package org.jeyzer.analyzer.output.poi.rule.monitor.row;

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
import org.jeyzer.monitor.engine.event.MonitorEvent;

public interface MonitorRowHeader {
	
	public static final String EVENT_LINK_FIELD = "event_link";
	
	public boolean apply(MonitorEvent event, Cell cell);
	
	public String getName();
	
	public String getDisplayName();
	
	public String getComment();
	
	public int getColumnWidth();
	
	public boolean hasEventLink();
	
}
