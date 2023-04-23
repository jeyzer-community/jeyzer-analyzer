package org.jeyzer.analyzer.output.poi.rule.header;

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



import static org.jeyzer.analyzer.output.poi.style.DefaultCellStyles.*;



import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.jeyzer.analyzer.config.report.ConfigDisplay;
import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.analyzer.data.stack.ThreadStack;
import org.jeyzer.analyzer.output.poi.context.SheetDisplayContext;
import org.jeyzer.analyzer.output.poi.rule.AbstractDisplayRule;
import org.jeyzer.analyzer.output.poi.rule.header.function.HeaderFunction;
import org.jeyzer.analyzer.output.poi.rule.header.function.HeaderFunction.HeaderFunctionType;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;

public abstract class AbstractTendencyRule extends AbstractDisplayRule implements Header {
	
	public AbstractTendencyRule(ConfigDisplay headerCfg, SheetDisplayContext context){
		super(headerCfg, context);
	}	
	
	@Override
	public boolean apply(List<ThreadDump> dumps, List<Cell> cells, HeaderFunction function) {
		Cell cell;
		String value;
		
		// cell value
		for (int j=0; j< dumps.size(); j++){
			cell = cells.get(j);
			value = extractTendency(dumps.get(j).getWorkingThreads());
			if (value != null)
				setValue(cell, value);
		}
		
		return true;
	}

	private String extractTendency(List<ThreadStack> stacks) {
		if (stacks.size() < 3)
			return null;

		Multiset<String> valueMultiSet = HashMultiset.create();
		for (ThreadStack stack : stacks){
			valueMultiSet.add(getElementValue(stack));
		}
		
		String tendencyValue = Multisets.copyHighestCountFirst(valueMultiSet).elementSet().iterator().next();
		
		int count = valueMultiSet.count(tendencyValue);
		if (count >= 3)
			return tendencyValue+ " (" + count+ ")";
		else
			return null;
	}
	
	protected abstract String getElementValue(ThreadStack stack);

	@Override
	public int displayLegend(int line, int pos) {
		return line;
	}

	@Override
	public int displayStats(int line, int pos) {
		return line;
	}

	@Override
	public String getStyle() {
		return STYLE_CELL_SMALL_TEXT;
	}
	
	@Override
	public boolean supportFunction(HeaderFunctionType function) {
		return false;
	}

}
