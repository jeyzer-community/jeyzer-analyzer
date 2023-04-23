package org.jeyzer.analyzer.output.poi.sheet;

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



import static org.jeyzer.analyzer.output.poi.theme.AbstractTheme.*;





import java.util.Collection;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.jeyzer.analyzer.config.report.ConfigExecutorHistogramSheet;
import org.jeyzer.analyzer.data.ThreadAction;
import org.jeyzer.analyzer.error.JzrException;
import org.jeyzer.analyzer.math.FormulaHelper;
import org.jeyzer.analyzer.output.poi.context.DisplayContext;
import org.jeyzer.analyzer.session.JzrSession;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multisets;

public class ExecutorHistogramSheet extends JeyzerSheet{

	private ConfigExecutorHistogramSheet sheetCfg;
	
	public ExecutorHistogramSheet(ConfigExecutorHistogramSheet config, JzrSession session, DisplayContext displayContext) {
		super(session, displayContext);
		this.sheetCfg = config;
	}

	@Override
	public void display() throws JzrException {
		int linePos = 1;
		
		Sheet sheet = createSheet(this.sheetCfg);

    	// Headers
		int rowPos = displayHeaders(sheet, linePos);
    	sheet.createFreezePane(0, 2);
    	
    	// Executors
    	linePos = displayExecutors(sheet, linePos);
    	sheet.setAutoFilter(new CellRangeAddress(1, linePos, 0, rowPos-1));
    	
    	addTopBar(sheet, rowPos);
    	addMenuLink(sheet, sheetCfg, STYLE_THEME_TOP_BAR_JEYZER_TITLE, 0, 0);
    	
    	close(this.sheetCfg);
	}

	private int displayExecutors(Sheet sheet, int linePos) throws JzrException {
    	// total number of active stacks
    	int globalActionsStackSize = session.getActionsStackSize();
    	int globalActionsSize = session.getActionsSize();
    	
    	Multimap<String, ThreadAction> actionsPerExecutor = session.getActionSetPerExecutor();
    	this.itemsCount = actionsPerExecutor.keySet().size();
    	
    	int groupCount = 0;
    	for (String executor : Multisets.copyHighestCountFirst(actionsPerExecutor.keys()).elementSet()) {
    		Collection<ThreadAction> actions = actionsPerExecutor.get(executor);
    		
    		int actionStackCount = 0;
    		for (ThreadAction action : actions)
    			actionStackCount += action.size();
    		
   			linePos++;
   			displayExecutor(executor, actions.size(), globalActionsSize, actionStackCount, globalActionsStackSize, sheet, groupCount, linePos);

			groupCount++;
    	}
    	
		return linePos;
	}

	private int displayHeaders(Sheet sheet, int linePos) {
    	Row row = sheet.createRow(linePos);
    	int rowPos=0;
    	Cell cell;
        
    	sheet.setColumnWidth(rowPos, 50*256);
    	cell = addHeaderCell(row, rowPos++, "Executor");
        wrapText(cell);
        
    	sheet.setColumnWidth(rowPos, 27*256);
    	cell = addHeaderCell(row, rowPos++, "Action count");
    	wrapText(cell);
        
    	sheet.setColumnWidth(rowPos, 27*256);
    	cell = addHeaderCell(row, rowPos++, "Action %");
    	wrapText(cell);
    	
    	sheet.setColumnWidth(rowPos, 27*256);
    	cell = addHeaderCell(row, rowPos++, "Stack count");
    	wrapText(cell);
    	
    	sheet.setColumnWidth(rowPos, 27*256);
    	cell = addHeaderCell(row, rowPos++, "Stack %");
    	wrapText(cell);
    	
		return rowPos;
	}

	private void displayExecutor(String executor, int actionCount, int globalActionCount, int actionStackCount, int globalActionsStackSize, Sheet sheet, int groupCount, int linePos) {
    	Row row = sheet.createRow(linePos);
    	int rowPos=0;
    	
        addCell(row, rowPos++, executor, getCellStyleReference(groupCount));
    	addCell(row, rowPos++, actionCount, getCellStyleNumberReference(groupCount));
    	addCell(row, rowPos++, FormulaHelper.percentRound(actionCount, globalActionCount), getCellStyleNumberReference(groupCount));
    	addCell(row, rowPos++, actionStackCount, getCellStyleNumberReference(groupCount));
    	addCell(row, rowPos++, FormulaHelper.percentRound(actionStackCount, globalActionsStackSize), getCellStyleNumberReference(groupCount));
	}
	
}
