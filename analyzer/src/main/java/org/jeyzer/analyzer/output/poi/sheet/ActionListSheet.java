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



import static org.jeyzer.analyzer.math.FormulaHelper.*;
import static org.jeyzer.analyzer.output.poi.style.DefaultCellStyles.*;
import static org.jeyzer.analyzer.output.poi.theme.AbstractTheme.*;





import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.jeyzer.analyzer.config.report.ConfigActionSheet;
import org.jeyzer.analyzer.data.ThreadAction;
import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.analyzer.output.poi.CellRefRepository;
import org.jeyzer.analyzer.output.poi.context.DisplayContext;
import org.jeyzer.analyzer.output.poi.rule.header.ApplicativeCpuActivityRule;
import org.jeyzer.analyzer.output.poi.rule.header.ApplicativeMemoryActivityRule;
import org.jeyzer.analyzer.session.JzrSession;

public class ActionListSheet extends JeyzerSheet {
	
	private static final String RESTART_LABEL = "RESTART";

	private ConfigActionSheet sheetCfg;
	
	public ActionListSheet(ConfigActionSheet sheetCfg, JzrSession session, DisplayContext displayContext) {
		super(session, displayContext);
		this.sheetCfg = sheetCfg;
	}	
	
	@Override
	public void display() {
    	int linePos = 1;
    	int rowPos=0;
    	Sheet sheet = createSheet(this.sheetCfg);
    	CellRefRepository cellRefRepository = this.displayContext.getCellRefRepository();
    	
    	rowPos = displayHeader(sheet, cellRefRepository, linePos, rowPos);
    	linePos++;
    	
    	linePos = displayActions(sheet, cellRefRepository, linePos);
		
		int linkOffset = cellRefRepository.getActionSheetTypes().size();
		sheet.setAutoFilter(new CellRangeAddress(1, linePos-1, 1 + linkOffset, rowPos - 1));
		sheet.createFreezePane(0, 2);

		createColumnGroups(sheet, cellRefRepository);
		
    	addTopBar(sheet, rowPos);
    	addMenuLink(sheet, sheetCfg, STYLE_THEME_TOP_BAR_JEYZER_TITLE, 0, 0);
    	
    	close(this.sheetCfg);
	}

	private void createColumnGroups(Sheet sheet, CellRefRepository cellRefRepository) {
		int linkSize = cellRefRepository.getActionSheetTypes().size();
		int columnOffset = linkSize;

		// link group
		if (linkSize>1)
			createColumnGroup(sheet, 2, linkSize, true);
		
		// time group
		createColumnGroup(sheet, 2 + columnOffset, 4 + columnOffset, true);
		
		// thread group
		createColumnGroup(sheet, 6 + columnOffset, 8 + columnOffset, true);

		// duration group
		createColumnGroup(sheet, 10 + columnOffset, 10 + columnOffset, true);
		
		// cpu group
		if (session.isCPUInfoAvailable()){
			createColumnGroup(sheet, 13 + columnOffset, 15 + columnOffset, true);
			columnOffset += 3; // not 4
		}
		
		// memory group
		if (session.isMemoryInfoAvailable())
			createColumnGroup(sheet, 13 + columnOffset, 13 + columnOffset, true);
	}

	private int displayHeader(Sheet sheet, CellRefRepository cellRefRepository, int linePos, int rowPos) {
    	// Header
    	Row row = sheet.createRow(linePos++);

    	sheet.setColumnWidth(rowPos, 6*256);
    	addHeaderCell(row, rowPos++, "Id");
    	
    	// add action links to sheets
    	for (String sheetType : cellRefRepository.getActionSheetTypes()){
        	sheet.setColumnWidth(rowPos, 4*256);
        	addHeaderCell(row, rowPos++, sheetType, STYLE_THEME_HEADER_VERY_SMALL_ROTATED);
    	}

    	sheet.setColumnWidth(rowPos, 18*256);
    	addHeaderCell(row, rowPos++, "Min start date");
        
    	sheet.setColumnWidth(rowPos, 18*256);
    	addHeaderCell(row, rowPos++, "Max start date");
    	
    	sheet.setColumnWidth(rowPos, 18*256);
    	addHeaderCell(row, rowPos++, "Min end date");
    	
    	sheet.setColumnWidth(rowPos, 18*256);
    	addHeaderCell(row, rowPos++, "Max end date");
    	
    	sheet.setColumnWidth(rowPos, 30*256);
    	addHeaderCell(row, rowPos++, "Executor");
    	
    	sheet.setColumnWidth(rowPos, 15*256);
    	addHeaderCell(row, rowPos++, "Thread id");
    	
    	sheet.setColumnWidth(rowPos, 25*256);
    	addHeaderCell(row, rowPos++, "Thread name");
    	
    	sheet.setColumnWidth(rowPos, 15*256);
    	addHeaderCell(row, rowPos++, "Thread priority");
    	
    	sheet.setColumnWidth(rowPos, 45*256);
    	addHeaderCell(row, rowPos++, "Action (principal)");
    	
    	sheet.setColumnWidth(rowPos, 20*256);
    	Cell cell = addHeaderCell(row, rowPos++, "Min Duration (sec)");
    	addComment(sheet, cell, "Multiple of " + session.getThreadDumpPeriod() + " sec (thread dump period)", 1, 2);

    	sheet.setColumnWidth(rowPos, 20*256);
    	addHeaderCell(row, rowPos++, "Max Duration (sec)");
    	
    	if (session.isCPUInfoAvailable()){
        	sheet.setColumnWidth(rowPos, 20*256);
        	addHeaderCell(row, rowPos++, "CPU usage");
        	
        	sheet.setColumnWidth(rowPos, 20*256);
        	addHeaderCell(row, rowPos++, "CPU time (ms)");
        	
        	sheet.setColumnWidth(rowPos, 20*256);
        	addHeaderCell(row, rowPos++, "CPU usage max");
        	
        	sheet.setColumnWidth(rowPos, 20*256);
        	cell = addHeaderCell(row, rowPos++, "App CPU Act max");
        	addComment(sheet, cell, ApplicativeCpuActivityRule.CELL_LABEL_COMMENT, 3, 6);
    	}

    	if (session.isMemoryInfoAvailable()){
        	sheet.setColumnWidth(rowPos, 20*256);
        	addHeaderCell(row, rowPos++, "Mem used (Mb)");
    		
        	sheet.setColumnWidth(rowPos, 20*256);
        	cell = addHeaderCell(row, rowPos++, "App Mem Act max");
        	addComment(sheet, cell, ApplicativeMemoryActivityRule.CELL_LABEL_COMMENT, 2, 6);
    	}    	
    	
    	sheet.setColumnWidth(rowPos, 30*256);
    	addHeaderCell(row, rowPos++, "Operation (principal)");

    	sheet.setColumnWidth(rowPos, 15*256);
    	addHeaderCell(row, rowPos++, "# Rec snapshots");
    	
    	sheet.setColumnWidth(rowPos, 15*256);
    	addHeaderCell(row, rowPos++, "# Hiatus");
    	
    	return rowPos;
    }

	private int displayActions(Sheet sheet, CellRefRepository cellRefRepository, int linePos) {
		this.itemsCount = session.getActionsSize();

		// Content
		for (ThreadDump dump : session.getDumps()){
			
			if (dump.isRestart())
				displayRestart(sheet, cellRefRepository.getActionSheetTypes().size(), linePos++);
			
			Date timestamp = dump.getTimestamp();
			List<ThreadAction> actions = new ArrayList<>(session.getActionHistory().get(timestamp));
			
			// sort by id
			actions.sort(new Comparator<ThreadAction>() {
				@Override
	            public int compare(ThreadAction a1, ThreadAction a2) {
	                return a1.getId() < a2.getId() ? -1:
	                	a1.getId() == a2.getId()? 0: 1;
	            	}
				}
			);
			
			for(ThreadAction action : actions){
				displayAction(sheet, cellRefRepository, action, linePos++);
			}
		}
		
		return linePos;
	}

	private void displayRestart(Sheet sheet, int actionSheetTypesSize, int linePos) {
		int rowPos = 0;
		Row row = sheet.createRow(linePos);
		addEmptyCell(row, rowPos++, STYLE_RESTART_LINE);
		for (int i=0; i<actionSheetTypesSize; i++){
			addEmptyCell(row, rowPos++, STYLE_RESTART_LINE);
		}
		
    	// Time
		addCell(row, rowPos++, RESTART_LABEL, STYLE_RESTART_LINE); // set it every 2 columns
		addEmptyCell(row, rowPos++, STYLE_RESTART_LINE);
		addEmptyCell(row, rowPos++, STYLE_RESTART_LINE);
		addEmptyCell(row, rowPos++, STYLE_RESTART_LINE);
		
		// Thread
		addEmptyCell(row, rowPos++, STYLE_RESTART_LINE);
		addEmptyCell(row, rowPos++, STYLE_RESTART_LINE);
		addEmptyCell(row, rowPos++, STYLE_RESTART_LINE);
		addEmptyCell(row, rowPos++, STYLE_RESTART_LINE);
		
		addCell(row, rowPos++, RESTART_LABEL, STYLE_RESTART_LINE);
		addEmptyCell(row, rowPos++, STYLE_RESTART_LINE);
		addEmptyCell(row, rowPos++, STYLE_RESTART_LINE);
    	if (session.isCPUInfoAvailable()){
    		addCell(row, rowPos++, RESTART_LABEL, STYLE_RESTART_LINE);
    		addEmptyCell(row, rowPos++, STYLE_RESTART_LINE);
    		addEmptyCell(row, rowPos++, STYLE_RESTART_LINE);
    		addEmptyCell(row, rowPos++, STYLE_RESTART_LINE);
    	}
    	if (session.isMemoryInfoAvailable()){
    		addEmptyCell(row, rowPos++, STYLE_RESTART_LINE);
    		addEmptyCell(row, rowPos++, STYLE_RESTART_LINE);
    	}
    	addCell(row, rowPos++, RESTART_LABEL, STYLE_RESTART_LINE);
    	addEmptyCell(row, rowPos++, STYLE_RESTART_LINE);
    	addEmptyCell(row, rowPos++, STYLE_RESTART_LINE);
	}

	private void displayAction(Sheet sheet, CellRefRepository cellRefRepository, ThreadAction action, int linePos) {
		int rowPos = 0;
		Row row = sheet.createRow(linePos);
		
		addCell(row, rowPos++, (long)action.getId(), STYLE_CELL);
    	// add links to sheets
    	for (String sheetType : cellRefRepository.getActionSheetTypes()){
    		CellReference cellRef = cellRefRepository.getCellRef(sheetType, action);
    		if (cellRef != null)
    			addCell(row, rowPos++, sheetType.substring(0, 1).toUpperCase(), cellRef, STYLE_LINK);
    		else
    			addEmptyCell(row, rowPos++);
    	}
    	
    	// Time
		addCell(row, rowPos++, convertToTimeZone(action.getMinStartDate()), STYLE_CELL_DATE);
		addCell(row, rowPos++, convertToTimeZone(action.getStartDate()), STYLE_CELL_DATE);
		addCell(row, rowPos++, convertToTimeZone(action.getEndDate()), STYLE_CELL_DATE);
		addCell(row, rowPos++, convertToTimeZone(action.getMaxEndDate()), STYLE_CELL_DATE);
		
		// Thread
		addCell(row, rowPos++, action.getExecutor(), STYLE_CELL);
		addCell(row, rowPos++, action.getThreadId(), STYLE_CELL);
		addCell(row, rowPos++, action.getName(), STYLE_CELL);
		addCell(row, rowPos++, action.getPriority(), STYLE_CELL_NUMBER);
		
		Cell cell = addCell(row, rowPos++, action.getPrincipalCompositeFunction(), STYLE_CELL);
		registerActionLink(action.getId(), this.sheetCfg.getName(), cell);
		addCell(row, rowPos++, action.getMinDuration(), STYLE_CELL_NUMBER);
		addCell(row, rowPos++, action.getMaxDuration(), STYLE_CELL_NUMBER);
    	if (session.isCPUInfoAvailable()){
    		int cpuUsage = (int) (Math.round(action.getCpuUsage()));
    		addCell(row, rowPos++, cpuUsage, STYLE_CELL_NUMBER);
    		addCell(row, rowPos++, action.getCpuTime() / 1000000L, STYLE_CELL_NUMBER); // convert to ms
    		addCell(row, rowPos++, Math.round(action.getCpuUsageMax()), STYLE_CELL_NUMBER);
    		addCell(row, rowPos++, Math.round(action.getApplicativeCpuActivityUsageMax()), STYLE_CELL_NUMBER);
    	}
    	if (session.isMemoryInfoAvailable()){
    		addCell(row, rowPos++, convertToMb(action.getAllocatedMemory()), STYLE_CELL_NUMBER);
    		addCell(row, rowPos++, Math.round(action.getApplicativeMemoryActivityUsageMax()), STYLE_CELL_NUMBER);
    	}
		addCell(row, rowPos++, action.getPrincipalCompositeOperation(), STYLE_CELL);
		addCell(row, rowPos++, action.size(), STYLE_CELL_NUMBER);
		addCell(row, rowPos++, getActionMissingDumpsCount(action), STYLE_CELL_NUMBER);
	}

	private int getActionMissingDumpsCount(ThreadAction action){
    	int count = 0;
		for (int a=0; a<action.size(); a++){
			Date timestamp = action.getThreadStack(a).getTimeStamp();
			if (session.getDump(timestamp).hasHiatusBefore()){
				count++;
			}
		}
		return count;
    }

}
