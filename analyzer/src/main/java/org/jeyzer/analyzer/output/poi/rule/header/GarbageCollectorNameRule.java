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





import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.jeyzer.analyzer.config.report.ConfigDisplay;
import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.analyzer.data.gc.GarbageCollection;
import org.jeyzer.analyzer.data.gc.GarbageCollectorInfo;
import org.jeyzer.analyzer.output.poi.context.SheetDisplayContext;
import org.jeyzer.analyzer.output.poi.rule.AbstractDisplayRule;
import org.jeyzer.analyzer.output.poi.rule.header.function.HeaderFunction;
import org.jeyzer.analyzer.output.poi.rule.header.function.HeaderFunction.HeaderFunctionType;

public class GarbageCollectorNameRule extends AbstractDisplayRule implements Header {
	public static final String RULE_NAME = "garbage_collection_executed_garbage_collector";
	
	public static final String CELL_LABEL_COMMENT_PREFIX_ALL = "Executed garbage collector(s)";
	public static final String CELL_LABEL_COMMENT_PREFIX_LAST = "Last executed garbage collector";
	
	public static final String CELL_LABEL_COMMENT_SUFFIX = " since the last JZR recording snapshot\n"
			+ "First value is NA : collector(s) may have been executed or not, right before this measurement";
	
	private static final String DISPLAY_NAME = "display";
	
	// possibles values : last or all
	private static final String DISPLAY_TYPE = "type";  
	private static final String DISPLAY_TYPE_LAST = "last";
	
	protected String displayName;
	protected boolean allGCs = true;  // default display all
	
	public GarbageCollectorNameRule(ConfigDisplay headerCfg, SheetDisplayContext context){
		super(headerCfg, context);
		this.displayName = (String)headerCfg.getValue(DISPLAY_NAME);
		String type = (String)headerCfg.getValue(DISPLAY_TYPE);
		if (type != null && DISPLAY_TYPE_LAST.equals(type))
			this.allGCs = false;
	}
	
	@Override
	public boolean apply(List<ThreadDump> dumps, List<Cell> cells, HeaderFunction function) {
		Cell cell;
		String value;
		List<String> collectorNames = new ArrayList<>();
		
		// cell value
		for (int j=0; j< dumps.size(); j++){
			cell = cells.get(j);
			
			GarbageCollection garbageCollection = dumps.get(j).getGarbageCollection();
			if (allGCs)
				value = buildAllGCNames(garbageCollection, collectorNames, j);
			else
				value = buildLastGCName(garbageCollection, j);
			
			setValue(cell, value);
			if (NOT_AVAILABLE.equals(value))
				setColorForeground(cell,COLOR_NOT_AVAILABLE);
			
			if (allGCs)
				collectorNames.clear();
		}
		
		return true;
	}

	private String buildLastGCName(GarbageCollection garbageCollection, int j) {
		String value = "";
		GarbageCollectorInfo gcInfo;
		
		gcInfo = garbageCollection.getMostRecentGarbageCollectorInfo();
		if (gcInfo != null && gcInfo.isFresh())
			value = gcInfo.getName();
		else
			value = buildNoneValue(j);
		
		return value;
	}

	private String buildAllGCNames(GarbageCollection garbageCollection, List<String> collectorNames, int j) {
		String value = "";
		
		for (GarbageCollectorInfo gcInfo : garbageCollection.getGarbageCollectorInfos()){
			if (gcInfo.isFresh())
				collectorNames.add(gcInfo.getName());
		}
	
		if (!collectorNames.isEmpty()){
			int i = 0;
			for (String name : collectorNames){
				String endLine = collectorNames.size()-1 == i ? "": "\n";
				value = value + name + endLine;  
				i++;
			}
		}else{
			value = buildNoneValue(j);
		}

		return value;
	}

	private String buildNoneValue(int j) {
		if (j!=0)
			return "None";
		else 
			return NOT_AVAILABLE;  // First value is NA : collectors may have been executed or not right before this stage
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
		return displayName;
	}
	
	@Override
	public String getComment() {
		return (allGCs? CELL_LABEL_COMMENT_PREFIX_ALL : CELL_LABEL_COMMENT_PREFIX_LAST)
				+ CELL_LABEL_COMMENT_SUFFIX;
	}

	@Override
	public String getStyle() {
		return STYLE_CELL_SMALL_TEXT_WRAPPED;
	}
	
	@Override
	public boolean supportFunction(HeaderFunctionType function) {
		return false;
	}
}
