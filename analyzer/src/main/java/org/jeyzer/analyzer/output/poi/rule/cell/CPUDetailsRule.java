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

public class CPUDetailsRule extends AbstractCPUDisplayRule implements DisplayRule {
	
	public static final String RULE_NAME = "cpu_details";
	
	public CPUDetailsRule(ConfigDisplay displayCfg, SequenceSheetDisplayContext context){
		super(displayCfg, context);
	}	
	
	@Override
	public boolean apply(Action action, List<Cell> cells) {
		Cell cell;
		ThreadStack stack;
		ThreadStackCPUInfo stackCPUInfo;
		StringBuilder cpuTimes;
		
		// cell value
		for (int j=0; j< action.size(); j++){
			cell = cells.get(j);
			stack = action.getThreadStack(j);
			stackCPUInfo = stack.getCpuInfo();
			
			if (stackCPUInfo == null)
				continue;
			
			cpuTimes = new StringBuilder();
			if (!valueActivity || (stackCPUInfo.getCpuUsage()>0)){
				cpuTimes.append(LABEL_CPU_USAGE);
				cpuTimes.append(Math.round(stackCPUInfo.getCpuUsage()));
				cpuTimes.append(LABEL_PERCENT_CARRIAGE_RETURN);				
			}

			if (!valueActivity || (stackCPUInfo.getSystemUsage()>0)){
				cpuTimes.append(LABEL_SYS_USAGE);
				cpuTimes.append(Math.round(stackCPUInfo.getSystemUsage()));
				cpuTimes.append(LABEL_PERCENT_CARRIAGE_RETURN);
			}
			
			if (!valueActivity || (stackCPUInfo.getUserUsage()>0)){
				cpuTimes.append(LABEL_USR_USAGE);
				cpuTimes.append(Math.round(stackCPUInfo.getUserUsage()));
				cpuTimes.append(LABEL_PERCENT_CARRIAGE_RETURN);
			}
			
			if (!valueActivity || (stackCPUInfo.getApplicativeActivityUsage()>0)){
				cpuTimes.append(LABEL_ACT_USAGE);
				cpuTimes.append(Math.round(stackCPUInfo.getApplicativeActivityUsage()));
				cpuTimes.append(LABEL_PERCENT);
			}

			if (!valueActivity || cpuTimes.length()>0)
				setValue(cell, cpuTimes.toString(), false);
			
			int cpuPercent = (int)Math.round(stackCPUInfo.getCpuUsage());
			if (cpuPercent > 0)
				setColorHighlight(cell, getRange(cpuPercent));
		}
		
		return true;
	}

	@Override
	public int displayLegend(int line, int pos) {
		return displayHighlightLegend(line, pos);
	}

	@Override
	public int displayStats(int line, int pos) {
		return line;
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
		return false ;   // multi value cell : non sense
	}
}
