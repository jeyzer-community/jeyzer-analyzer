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
import org.jeyzer.analyzer.output.poi.context.SequenceSheetDisplayContext;
import org.jeyzer.analyzer.output.poi.rule.cell.AbstractCellDisplayRule;
import org.jeyzer.monitor.engine.event.MonitorEvent;

public class EventLevelRule extends AbstractCellDisplayRule implements MonitorDisplayRule {

	public static final String RULE_NAME = "event_level";	
	
	public EventLevelRule(ConfigDisplay displayCfg, SequenceSheetDisplayContext context) {
		super(displayCfg, context);
	}

	@Override
	public boolean apply(MonitorEvent event, List<Cell> cells) {
		Cell cell;
		
		// cell value
		for (int j=0; j< cells.size(); j++){
			cell = cells.get(j);
			// Just highlight
			setColorHighlight(cell, event.getLevel().toString());
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

}
