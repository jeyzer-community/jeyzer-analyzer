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
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.jeyzer.analyzer.config.report.headers.ConfigSheetHeader;
import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.analyzer.output.poi.context.SheetDisplayContext;
import org.jeyzer.analyzer.output.poi.rule.AbstractDisplayRule;
import org.jeyzer.analyzer.output.poi.rule.header.function.HeaderFunction;
import org.jeyzer.analyzer.output.poi.rule.header.function.HeaderFunction.HeaderFunctionType;

import com.google.common.primitives.Longs;

public class MXBeanParamNumberRule extends AbstractNumericDisplayHeaderRule implements Header{

	public static final String RULE_NAME = "mx_bean_param_number";

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
	private Pattern paramNamePattern;
	
	public MXBeanParamNumberRule(ConfigSheetHeader headerCfg, SheetDisplayContext context) {
		super(headerCfg, context);
		this.displayName = (String)headerCfg.getValue(DISPLAY_NAME);
		this.displayCommentName = (String)headerCfg.getValue(DISPLAY_COMMENT_NAME);
		this.paramNamePattern = Pattern.compile((String)headerCfg.getValue(PARAM_NAME));
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
			
			// return the first that matches
			String paramKey = getParamKey(dumps.get(j).getJMXBeanParams().keySet());
			
			paramValue = dumps.get(j).getJMXBeanParams().get(paramKey);
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
		return RULE_NAME + "-" + (paramNamePattern.pattern() != null? paramNamePattern.pattern():"MX pattern name not yet set");
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}
	
	@Override
	public String getComment() {
		return displayCommentName + "\nMX parameter : " + this.paramNamePattern.toString();
	}
	
	@Override
	public String getStyle(){
		return STYLE_CELL_ALIGN_RIGHT;
	}

	@Override
	public boolean isPercentageBased() {
		return false;
	}	

	private String getParamKey(Set<String> paramKeys) {
		for (String key : paramKeys){
			if (this.paramNamePattern.matcher(key).find())
				return key;
		}
		return null;
	}
	
	@Override
	public boolean supportFunction(HeaderFunctionType function) {
		return SUPPORTED_FUNCTIONS.contains(function);
	}
}
