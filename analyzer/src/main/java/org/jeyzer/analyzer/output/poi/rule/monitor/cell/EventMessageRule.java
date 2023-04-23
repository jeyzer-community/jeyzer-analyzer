package org.jeyzer.analyzer.output.poi.rule.monitor.cell;

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






import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.jeyzer.analyzer.config.report.ConfigDisplay;
import org.jeyzer.analyzer.output.poi.CellText;
import org.jeyzer.analyzer.output.poi.context.SequenceSheetDisplayContext;
import org.jeyzer.analyzer.output.poi.rule.cell.AbstractCellDisplayRule;
import org.jeyzer.monitor.engine.event.MonitorApplicativeEvent;
import org.jeyzer.monitor.engine.event.MonitorEvent;

public class EventMessageRule extends AbstractCellDisplayRule implements MonitorDisplayRule {

	public static final String RULE_NAME = "event_message";	
	
	public EventMessageRule(ConfigDisplay displayCfg, SequenceSheetDisplayContext context) {
		super(displayCfg, context);
	}

	@Override
	public boolean apply(MonitorEvent event, List<Cell> cells) {
		Cell cell;
		
		// cell value
		for (int j=0; j< cells.size(); j++){
			if (j % 10 != 0)
				continue;
			
			cell = cells.get(j);
			
			String message = event instanceof MonitorApplicativeEvent ? event.getRef() : event.getMessage() + "\n" + event.getRef();
			addComment(cell, message, getDepth(message), getLength(message));
		}
		
		return true;
	}

	@Override
	public boolean hasStats() {
		return false;
	}

	@Override
	public String getName() {
		return RULE_NAME;
	}

	private int getDepth(String message) {
		int depth = 1;
		
		int pos = 0;
		while(pos < message.length()){
			pos = message.indexOf('\n', pos);
			if (pos == -1)
				break;
				
			depth++;
			pos++;
		}
		
		return depth;
	}
	
	private int getLength(String message) {
		int max = CellText.COMMENT_COLUMN_CHARS_SIZE;
		int start = 0;
		int pos = 0;
		
		while(pos < message.length()){
			pos = message.indexOf('\n', pos);
			if (pos == -1) {
				if (message.length() - start  > max)
					max = message.length() - start;
				break;
			}
			
			if ((pos - start) > max)
				max = pos - start;
			
			start = pos;
			pos++;
		}
		
		return max / CellText.COMMENT_COLUMN_CHARS_SIZE;
	}
}
