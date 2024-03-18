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
import org.jeyzer.analyzer.config.report.ConfigDisplay;
import org.jeyzer.analyzer.output.poi.context.SheetDisplayContext;
import org.jeyzer.analyzer.output.poi.rule.AbstractNumericDisplayRule;
import org.jeyzer.monitor.engine.event.MonitorEvent;

public class EventExtIdRule extends AbstractNumericDisplayRule implements MonitorRowHeader{

	public static final String RULE_NAME = "event_ext_id";
	
	public static final String CELL_LABEL_COMMENT = "Unique event external id\n For display only, valid for the scope of this analysis\n May not be strictly time ordered";
	
	private static final String DISPLAY_NAME = "Id";
	
	private boolean hasLink;
	
	public EventExtIdRule(ConfigDisplay headerCfg, SheetDisplayContext context){
		super(headerCfg, context);
		hasLink = Boolean.parseBoolean((String)headerCfg.getValue(EVENT_LINK_FIELD));
	}

	@Override
	public boolean apply(MonitorEvent event, Cell cell) {
		setValue(cell, Long.parseLong(event.getExtId()));
		return true;
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
		return 6*256;
	}

	@Override
	public boolean hasEventLink() {
		return hasLink;
	}

	@Override
	public boolean isPercentageBased() {
		return false;
	}
}
