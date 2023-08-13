package org.jeyzer.analyzer.output.poi.rule.cell;

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
import org.jeyzer.analyzer.data.Action;
import org.jeyzer.analyzer.data.stack.ThreadStack;
import org.jeyzer.analyzer.data.stack.ThreadStackCPUInfo;
import org.jeyzer.analyzer.output.poi.context.SequenceSheetDisplayContext;

public class CPUUsageRule extends AbstractCPUDisplayRule implements DisplayRule {
	
	public static final String RULE_NAME = "cpu_usage";
	
	public CPUUsageRule(ConfigDisplay displayCfg, SequenceSheetDisplayContext context){
		super(displayCfg, context);
	}	
	
	@Override
	public boolean apply(Action action, List<Cell> cells) {
		Cell cell;
		ThreadStack stack;
		ThreadStackCPUInfo stackCPUInfo;
		
		// cell value
		ActionStatsContext actionStatsContext = new ActionStatsContext();
		for (int j=0; j< action.size(); j++){
			cell = cells.get(j);
			stack = action.getThreadStack(j);
			stackCPUInfo = stack.getCpuInfo();
			if (stackCPUInfo == null)
				continue;
			
			displaySingleValue(cell, LABEL_CPU_USAGE, (int)Math.round(stackCPUInfo.getCpuUsage()), actionStatsContext, stack.getInstanceCount());
		}
		
		return true;
	}

	@Override
	public int displayLegend(int line, int pos) {
		return displayHighlightLegend(line, pos, DESCRIPTION_PREFIX);
	}

	@Override
	public int displayStats(int line, int pos) {
		return super.displayStats(line, pos);
	}

	@Override
	public String getName() {
		return RULE_NAME;
	}
	
	
	@Override
	public boolean hasLegend() {
		return hasHighlights();
	}
	
	@Override
	public boolean hasStats() {
		return true;
	}
}
