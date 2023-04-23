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




import static org.jeyzer.analyzer.output.poi.theme.AbstractTheme.STYLE_THEME_HEADER_DATE_ROW;

import org.apache.poi.ss.usermodel.Cell;
import org.jeyzer.analyzer.config.report.ConfigDisplay;
import org.jeyzer.analyzer.output.poi.context.SheetDisplayContext;
import org.jeyzer.analyzer.output.poi.rule.AbstractDisplayRule;
import org.jeyzer.monitor.engine.event.MonitorEvent;
import org.jeyzer.monitor.util.MonitorHelper;

public class EventStartDateRule extends AbstractDisplayRule implements MonitorRowHeader{

	public static final String RULE_NAME = "event_start_date";
	
	public static final String CELL_LABEL_COMMENT = "Monitoring event start date";
	
	private static final String DISPLAY_NAME = "Start date";
	
	private boolean hasLink;
	
	public EventStartDateRule(ConfigDisplay headerCfg, SheetDisplayContext context){
		super(headerCfg, context);
		hasLink = Boolean.parseBoolean((String)headerCfg.getValue(EVENT_LINK_FIELD));
	}

	@Override
	public boolean apply(MonitorEvent event, Cell cell) {
		cell.setCellStyle(this.context.getCellStyles().getThemeStyles().getThemeStyle(STYLE_THEME_HEADER_DATE_ROW));		
		cell.setCellValue(MonitorHelper.convertToTimeZone(event.getStartDate()));
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
		return 16*256;
	}

	@Override
	public boolean hasEventLink() {
		return hasLink;
	}	

}
