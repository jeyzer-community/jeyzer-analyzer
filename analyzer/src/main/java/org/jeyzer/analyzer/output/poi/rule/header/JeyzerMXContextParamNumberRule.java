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





import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.jeyzer.analyzer.config.report.headers.ConfigSheetHeader;
import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.analyzer.output.poi.context.SheetDisplayContext;
import org.jeyzer.analyzer.output.poi.rule.AbstractDisplayRule;
import org.jeyzer.analyzer.output.poi.rule.header.function.HeaderFunction;
import org.jeyzer.analyzer.output.poi.rule.header.function.HeaderFunction.HeaderFunctionType;

import com.google.common.primitives.Longs;

public class JeyzerMXContextParamNumberRule extends AbstractNumericDisplayHeaderRule implements Header{

	public static final String RULE_NAME = "jzr_mx_context_param_number";

	private static final String DISPLAY_NAME = "display";
	private static final String DISPLAY_COMMENT_NAME = "display_comment";
	private static final String PARAM_NAME = "name";
	
	private static final Set<HeaderFunctionType> SUPPORTED_FUNCTIONS = EnumSet.of(
			HeaderFunctionType.AVERAGE, 
			HeaderFunctionType.MIN,
			HeaderFunctionType.MAX,
			HeaderFunctionType.CUMULATIVE,
			HeaderFunctionType.VARIANCE,
			HeaderFunctionType.STANDARD_DEVIATION);
	
	private String displayName;
	private String displayCommentName;
	private String paramName;
	
	public JeyzerMXContextParamNumberRule(ConfigSheetHeader headerCfg, SheetDisplayContext context) {
		super(headerCfg, context);
		this.displayName = (String)headerCfg.getValue(DISPLAY_NAME);
		this.displayCommentName = (String)headerCfg.getValue(DISPLAY_COMMENT_NAME);
		this.paramName = (String)headerCfg.getValue(PARAM_NAME);
	}

	@Override
	public boolean apply(List<ThreadDump> dumps, List<Cell> cells, HeaderFunction function) {
		Cell cell;
		Long value; // NA
		String paramValue;
		
		// cell value
		for (int j=0; j< dumps.size(); j++){
			cell = cells.get(j);
			value = null;
			
			paramValue = dumps.get(j).getJeyzerMXContextParams().get(paramName);
			if(paramValue != null)
				value = Longs.tryParse(paramValue); // value can be null
			
			if (value != null){
				setValue(cell, value);
				function.apply(value, cell);
			}
			else
				setValue(cell, AbstractDisplayRule.NOT_AVAILABLE);
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
	public String getName() {
		return RULE_NAME + "-" + paramName;
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}
	
	@Override
	public String getComment() {
		return displayCommentName;
	}
	
	@Override
	public String getStyle(){
		return STYLE_CELL_ALIGN_RIGHT;
	}

	@Override
	public boolean isPercentageBased() {
		return false;
	}
	
	@Override
	public boolean supportFunction(HeaderFunctionType function) {
		return SUPPORTED_FUNCTIONS.contains(function);
	}
	
}
