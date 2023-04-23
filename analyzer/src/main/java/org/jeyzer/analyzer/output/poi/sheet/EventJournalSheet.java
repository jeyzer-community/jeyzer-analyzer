package org.jeyzer.analyzer.output.poi.sheet;

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





import org.apache.poi.ss.usermodel.Row;
import org.jeyzer.analyzer.config.report.ConfigMonitoringSheet;
import org.jeyzer.analyzer.output.poi.context.DisplayContext;
import org.jeyzer.analyzer.session.JzrSession;
import org.jeyzer.monitor.engine.event.MonitorApplicativeEvent;
import org.jeyzer.monitor.engine.event.MonitorEvent;

import static org.jeyzer.analyzer.output.poi.style.DefaultCellStyles.*;
import static org.jeyzer.monitor.engine.event.MonitorEvent.PARAM_DURATION_VALUE_INDEX;
import static org.jeyzer.monitor.engine.event.MonitorEvent.PARAM_END_DATE_VALUE_INDEX;

import java.util.List;

public class EventJournalSheet extends MonitoringSheet {
	
	public EventJournalSheet(ConfigMonitoringSheet config, JzrSession session, DisplayContext displayContext) {
		super(config, session, displayContext);
	}
	
	@Override
	protected int displayStartDate(Row row, int rowPos, MonitorEvent event, String rank) {
		if (event instanceof MonitorApplicativeEvent) {
			MonitorApplicativeEvent appEvent = (MonitorApplicativeEvent) event;
			addCell(row, rowPos++, convertToTimeZone(appEvent.getApplicativeStartDate()), STYLE_CELL_GREEN_DATE_CENTERED);
		}else {
			addCell(row, rowPos++, convertToTimeZone(event.getStartDate()), STYLE_CELL_GREY_DATE_CENTERED);
		}
		return rowPos;
	}
	
	@Override
	protected int displayEndDate(MonitorEvent event, List<String> params, Row row, int rowPos) {
		if (event instanceof MonitorApplicativeEvent) {
			MonitorApplicativeEvent appEvent = (MonitorApplicativeEvent) event;
			if (appEvent.getApplicativeEndDate() != null) {
				addCell(row, rowPos++, convertToTimeZone(appEvent.getApplicativeEndDate()), STYLE_CELL_DATE_CENTERED);				
			}
			else {
				addCell(row, rowPos++, IN_PROGRESS_VALUE, STYLE_CELL_CENTERED_SMALL_ITALIC);
			}
		}else {
			String endDate =  params.get(PARAM_END_DATE_VALUE_INDEX);
			if (NA_VALUE.equalsIgnoreCase(endDate))
				addCell(row, rowPos++, NA_VALUE, STYLE_CELL_GREY_CENTERED_SMALL_ITALIC);	
			else if (IN_PROGRESS_VALUE.equalsIgnoreCase(endDate))
				addCell(row, rowPos++, IN_PROGRESS_VALUE, STYLE_CELL_GREY_CENTERED_SMALL_ITALIC);
			else 
				addCell(row, rowPos++, convertToTimeZone(event.getEndDate()), STYLE_CELL_GREY_DATE_CENTERED);
		}
		return rowPos;
	}
	
	@Override
	protected int displayDuration(MonitorEvent event, List<String> params, Row row, int rowPos) {
		if (event instanceof MonitorApplicativeEvent) {
			MonitorApplicativeEvent appEvent = (MonitorApplicativeEvent) event;
			if (appEvent.getApplicativeEndDate() != null) {
				String duration = event.getPrintableDuration(appEvent.getApplicativeEndDate().getTime() - appEvent.getApplicativeStartDate().getTime());
				addCell(row, rowPos++, duration, STYLE_CELL_CENTERED);				
			}
			else {
				addCell(row, rowPos++, IN_PROGRESS_VALUE, STYLE_CELL_CENTERED_SMALL_ITALIC);
			}
		}else {
			String duration =  params.get(PARAM_DURATION_VALUE_INDEX);

			if (!NA_VALUE.equalsIgnoreCase(duration))
				addCell(row, rowPos++, duration, STYLE_CELL_GREY_CENTERED);
			else
				addCell(row, rowPos++, NA_VALUE, STYLE_CELL_GREY_CENTERED_SMALL_ITALIC);
		}

		return rowPos;
	}
}
