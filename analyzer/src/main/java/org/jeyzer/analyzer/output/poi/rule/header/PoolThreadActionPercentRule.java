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







import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.jeyzer.analyzer.config.report.headers.ConfigSheetHeader;
import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.analyzer.data.stack.ThreadStack;
import org.jeyzer.analyzer.math.FormulaHelper;
import org.jeyzer.analyzer.output.poi.context.SheetDisplayContext;
import org.jeyzer.analyzer.output.poi.rule.header.function.HeaderFunction;
import org.jeyzer.analyzer.output.poi.rule.header.function.HeaderFunction.HeaderFunctionType;

public class PoolThreadActionPercentRule extends AbstractNumericDisplayHeaderRule implements Header {

	public static final String RULE_NAME = "pool_thread_action_percent";
	
	public static final String POOL_NAME = "name";
	public static final String POOL_REGEX = "regex";
	
	public static final String CELL_LABEL_COMMENT_SUFFIX = " size";

	private static final Set<HeaderFunctionType> SUPPORTED_FUNCTIONS = EnumSet.of(
			HeaderFunctionType.AVERAGE, 
			HeaderFunctionType.MIN,
			HeaderFunctionType.MAX,
			HeaderFunctionType.VARIANCE,
			HeaderFunctionType.STANDARD_DEVIATION);
	
	private String ruleName;
	
	private String poolName;
	private java.util.regex.Pattern pattern;
	
	public PoolThreadActionPercentRule(ConfigSheetHeader headerCfg, SheetDisplayContext context) {
		super(headerCfg, context);
		
		this.poolName = (String)headerCfg.getValue(POOL_NAME);
		this.pattern = Pattern.compile((String)headerCfg.getValue(POOL_REGEX));
		
		this.ruleName = RULE_NAME + "-" + poolName.toLowerCase().replace(' ', '-');
	}

	@Override
	public boolean apply(List<ThreadDump> dumps, List<Cell> cells, HeaderFunction function) {
		Cell cell;
		double countValue = 0;
		double prevCountValue = (long) -1;
		Double value = null; // NA
		
		int poolThreadCounter;
		int poolThreadsActiveCounter;
		
		// cell value
		for (int j=0; j< dumps.size(); j++){
			poolThreadCounter = 0;
			poolThreadsActiveCounter = 0;
			countValue = 0;
			
			cell = cells.get(j);

			for (ThreadStack stack : dumps.get(j).getThreads()){
				Matcher matcher = pattern.matcher(stack.getName());
				if (matcher.find()){
					poolThreadCounter++;
					if (stack.isOfInterest())
						poolThreadsActiveCounter++;
				}
			}
			
			if (poolThreadCounter != 0)
				countValue = FormulaHelper.percent(poolThreadsActiveCounter, poolThreadCounter);

			value = countValue;				
			setValue(cell, value);
			function.apply(value, cell);

			setValueBasedColorForeground(
					cell,
					countValue,
					prevCountValue,
					false
					);			
			
			prevCountValue = countValue;
		}
		
		return true;
	}

	@Override
	public int displayLegend(int line, int pos) {
		return line;
	}

	@Override
	public int displayStats(int line, int pos) {
		return line;
	}

	@Override
	public String getDisplayName() {
		return poolName;
	}

	@Override
	public String getComment() {
		return poolName + CELL_LABEL_COMMENT_SUFFIX;
	}

	@Override
	public boolean isPercentageBased() {
		return true;
	}

	@Override
	public String getName() {
		return ruleName;
	}
	
	@Override
	public String getStyle(){
		return getDecimalStyle();
	}
	
	@Override
	public boolean supportFunction(HeaderFunctionType function) {
		return SUPPORTED_FUNCTIONS.contains(function);
	}

}
