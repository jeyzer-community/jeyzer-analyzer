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
import org.jeyzer.analyzer.output.poi.context.SheetDisplayContext;
import org.jeyzer.analyzer.output.poi.rule.AbstractDisplayRule;
import org.jeyzer.analyzer.output.poi.rule.header.function.HeaderFunction;
import org.jeyzer.analyzer.output.poi.rule.header.function.HeaderFunction.HeaderFunctionType;

public class SectionDelimiterRule extends AbstractDisplayRule implements Header {

	public static final String RULE_NAME = "section_delimiter";
	
	public static final String TITLE_FIELD = "title";
	
	private static final String DISPLAY_NAME = "";
		
	private String title;
	
	public SectionDelimiterRule(ConfigDisplay headerCfg, SheetDisplayContext context){
		super(headerCfg, context);
		this.title = (String)headerCfg.getValue(TITLE_FIELD);
	}	
	
	@Override
	public boolean apply(List<ThreadDump> dumps, List<Cell> cells, HeaderFunction function) {
		Cell cell;
		String value;
		
		// cell value
		for (int j=0; j< dumps.size(); j++){
			
			cell = cells.get(j);
			if (j%10 == 0)
				value = title;
			else
				value ="";
			
			setValue(cell, value);
			setColorForeground(cell, this.color);
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
		return RULE_NAME;
	}

	@Override
	public String getDisplayName() {
		return DISPLAY_NAME;
	}
	
	@Override
	public String getComment() {
		return null;
	}

	@Override
	public String getStyle() {
		return STYLE_CELL_VERY_SMALL_TEXT_WRAPPED;
	}
	
	@Override
	public boolean supportFunction(HeaderFunctionType function) {
		return false;
	}

}
