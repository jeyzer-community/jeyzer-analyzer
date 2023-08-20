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



import static org.jeyzer.analyzer.output.poi.CellText.STACK_DISPLAY_MAX_SIZE;
import static org.jeyzer.analyzer.output.poi.CellText.TRUNCATED_STACK_MESSAGE;


import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.jeyzer.analyzer.config.report.ConfigDisplay;
import org.jeyzer.analyzer.data.Action;
import org.jeyzer.analyzer.data.ThreadStackGroupAction;
import org.jeyzer.analyzer.data.stack.StackText;
import org.jeyzer.analyzer.data.stack.ThreadStack;
import org.jeyzer.analyzer.data.stack.ThreadStack.STACK_CODE_STATE;
import org.jeyzer.analyzer.output.poi.CellText;
import org.jeyzer.analyzer.output.poi.context.SequenceSheetDisplayContext;

public class StackRule extends AbstractCellDisplayRule implements DisplayRule{

	public static final String RULE_NAME = "stack";
	
	private boolean optimized;
	
	public StackRule (ConfigDisplay displayCfg, SequenceSheetDisplayContext context, boolean optimized){
		super(displayCfg, context);
		this.optimized = optimized;
	}
	
	@Override
	public boolean apply(Action action, List<Cell> cells) {
		Cell cell;
		ThreadStack stack;
		boolean display;
		
		// stack
		for (int j=0; j< action.size(); j++){
			cell = cells.get(j);
			stack = action.getThreadStack(j);
			
			display = isDisplayable(stack, j);
			
			if (display) {
				boolean sample = action instanceof ThreadStackGroupAction && ((ThreadStackGroupAction)action).getGroupSize(j) > 1;
				addStackOnCell(cell, stack, sample);
			}
		}
		
		return true;
	}

	private boolean isDisplayable(ThreadStack stack, int count) {
		if (!optimized)
			return true;
		
		if (STACK_CODE_STATE.RUNNING.equals(stack.getStackCodeState())
				|| STACK_CODE_STATE.FREEZE_BEGIN.equals(stack.getStackCodeState())
				|| STACK_CODE_STATE.FREEZE_END.equals(stack.getStackCodeState()))
			return true;
		
		return ((count % 5) == 0);
	}

	private void addStackOnCell(Cell cell, ThreadStack stack, boolean sample) {
		StackText st = stack.getStackHandler().getJzrFilteredText();
		String prefix = sample ? ("Sample : " + (stack.hasHeader() ? "" : "\n"))  : "";
		
		// Prevent this Excel warning on file opening when stack text is huge:
		//   Repaired Records: Sorting from /xl/comments20.xml part (Comments)
		String comment = 
				prefix
				+ ((st.getText().length() + st.getDepth() > STACK_DISPLAY_MAX_SIZE) ?
				    st.getText().substring(0, STACK_DISPLAY_MAX_SIZE -1 -st.getDepth()) + TRUNCATED_STACK_MESSAGE
				  : st.getText());
		
		addComment(cell, comment, st.getDepth(), st.getMaxlength() / CellText.COMMENT_COLUMN_CHARS_SIZE);
	}

	@Override
	public int displayLegend(int line, int pos) {
		// do nothing
		return line;
	}

	@Override
	public int displayStats(int line, int pos) {
		// do nothing
		return line;
	}	
	
	@Override
	public String getName() {
		return RULE_NAME;
	}
	
	@Override
	public boolean hasLegend() {
		return false;
	}
	
	@Override
	public boolean hasStats() {
		return false; // non sense
	}
}
