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



import static org.jeyzer.analyzer.output.graph.picture.ExcelGraphPicture.GraphType.ACTION_GRAPH_SINGLE;
import static org.jeyzer.analyzer.output.graph.picture.ExcelGraphPicture.GraphType.CONTENTION_GRAPH_SINGLE;
import static org.jeyzer.analyzer.output.poi.style.DefaultCellStyles.*;
import static org.jeyzer.analyzer.output.poi.theme.AbstractTheme.*;





import java.awt.Color;
import java.util.List;
import java.util.SortedMap;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.jeyzer.analyzer.config.report.ConfigTopStackSheet;
import org.jeyzer.analyzer.data.stack.ThreadStack.ThreadStackHandler;
import org.jeyzer.analyzer.data.tag.Tag;
import org.jeyzer.analyzer.error.JzrException;
import org.jeyzer.analyzer.output.graph.picture.ExcelGraphPicture;
import org.jeyzer.analyzer.output.graph.picture.GlobalContentionTypeGraphGenerator;
import org.jeyzer.analyzer.output.graph.picture.GlobalStackGraphGenerator;
import org.jeyzer.analyzer.output.poi.CellColor;
import org.jeyzer.analyzer.output.poi.context.DisplayContext;
import org.jeyzer.analyzer.session.JzrSession;
import org.jeyzer.analyzer.util.AnalyzerHelper;

import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;

public class TopStackSheet extends JeyzerSheet {
	
	private static final int ROW_SIZE = 11;
	private static final String LOCKED = "L";
	private static final String WAITING_TO_LOCK = "           waiting to lock ";
	private static final Color LOCKED_COLOR = new Color(255, 220, 109);
	
	private ConfigTopStackSheet sheetCfg;
	private boolean javaModuleSupport;

	public TopStackSheet(ConfigTopStackSheet sheetCfg, JzrSession session, DisplayContext displayContext) {
		super(session, displayContext);
		this.sheetCfg = sheetCfg;
		this.javaModuleSupport = session.isJavaModuleSupported();
	}

	@Override
	public void display() throws JzrException {
    	int linePos = 1;
    	int rowPos = 0;
    	Sheet sheet = createSheet(this.sheetCfg);
    	
    	displayHeader(sheet, linePos++);
    	
    	sheet.createFreezePane(0, 2);
    	
    	// Display the stack description as described by the header
    	Multiset<ThreadStackHandler> stacks = session.getStackSet();
  
    	int stackCount = 1;
    	int firstLine = linePos;
    	for (ThreadStackHandler stack : Multisets.copyHighestCountFirst(stacks).elementSet()) {
			
			if (stacks.count(stack) < this.sheetCfg.getThreshold())
				break;
			
	    	// Display the stack above the stack description, within a collapse section
			linePos = displayCodeLines(sheet, stack, linePos, stackCount);
			
    		rowPos = 0;
    		Row row = sheet.createRow(linePos++);

			addIdCell(row, rowPos++, stackCount++, stack);
			
			addCell(row, rowPos++, stack.getThreadStack().getPrincipalTag(), getCellStyleReference(stackCount-1));

			addCell(row, rowPos++, stacks.count(stack), getCellStyleReference(stackCount-1));

			addCell(row, rowPos++, stack.getThreadStack().getPrincipalOperation(), getCellStyleReference(stackCount-1));
			
			addCell(row, rowPos++, stack.getThreadStack().getPrincipalContentionType(), getCellStyleReference(stackCount-1));

			addCell(row, rowPos++, mergeList(stack.getThreadStack().getFunctionTags()), getCellStyleReference(stackCount-1));

			addCell(row, rowPos++, mergeList(stack.getThreadStack().getOperationTags()), getCellStyleReference(stackCount-1));
			
			addCell(row, rowPos++, mergeList(stack.getThreadStack().getContentionTypeTags()), getCellStyleReference(stackCount-1));
			
			addCell(row, rowPos++, stack.getThreadStack().getState().getDislayName(), getCellStyleReference(stackCount-1));
			
			addCell(row, rowPos++, stack.getThreadStack().getStackHandler().getCodeLines().size(), getCellStyleReference(stackCount-1));
			
			addCell(row, rowPos++, stack.getThreadStack().getExecutor(), getCellStyleReference(stackCount-1));
			
			sheet.groupRow(firstLine, linePos-2);
			sheet.setRowGroupCollapsed(firstLine, true);
			firstLine = linePos;
			this.itemsCount++;
    	}
    	
    	sheet.setAutoFilter(new CellRangeAddress(1, linePos-1, 0, ROW_SIZE-1));
    	
    	addTopBar(sheet, ROW_SIZE);
    	addMenuLink(sheet, sheetCfg, STYLE_THEME_TOP_BAR_JEYZER_TITLE, 0, 1);

    	// add global graph
    	displayGraphs(sheet, stacks, linePos, ROW_SIZE);
    	
    	close(this.sheetCfg);
	}

	private void displayHeader(Sheet sheet, int linePos) {
    	// Header
    	Row row = sheet.createRow(linePos);
    	int rowPos=0;
       
    	sheet.setColumnWidth(rowPos, 7*256);
    	addHeaderCell(row, rowPos++, "Id");
    	
    	sheet.setColumnWidth(rowPos, 45*256);
    	addHeaderCell(row, rowPos++, "Action (principal)");
    	
    	sheet.setColumnWidth(rowPos, 15*256);
    	addHeaderCell(row, rowPos++, "Count");
    	
    	sheet.setColumnWidth(rowPos, 30*256);
    	addHeaderCell(row, rowPos++, "Operation (principal)");
    	
    	sheet.setColumnWidth(rowPos, 30*256);
    	addHeaderCell(row, rowPos++, "Contention type (principal)");
    	
    	sheet.setColumnWidth(rowPos, 60*256);
    	addHeaderCell(row, rowPos++, "Action (all)");
    	
    	sheet.setColumnWidth(rowPos, 60*256);
    	addHeaderCell(row, rowPos++, "Operation (all)");
    	
    	sheet.setColumnWidth(rowPos, 60*256);
    	addHeaderCell(row, rowPos++, "Contention type (all)");

    	sheet.setColumnWidth(rowPos, 15*256);
    	addHeaderCell(row, rowPos++, "State");
    	
    	sheet.setColumnWidth(rowPos, 15*256);
    	addHeaderCell(row, rowPos++, "Stack size");
    	
    	sheet.setColumnWidth(rowPos, 30*256);
    	addHeaderCell(row, rowPos++, "Executor");
	}

	private void addIdCell(Row row, int pos, int stackCount, ThreadStackHandler stack) {
		if (this.sheetCfg.hasLockedColor() && stack.getThreadStack().isLocked()){
			Cell cell = addCell(row, pos, stackCount, STYLE_FRAME_SIMPLE);
			setColorForeground(cell, CellColor.buildColor(this.sheetCfg.getLockedColor()));
		}
		else if(this.sheetCfg.hasWaitingColor() && stack.getThreadStack().isWaiting()){
			Cell cell = addCell(row, pos, stackCount, STYLE_FRAME_SIMPLE);
			setColorForeground(cell, CellColor.buildColor(this.sheetCfg.getWaitingColor()));
		}
		else if(this.sheetCfg.hasTimedWaitingColor() && stack.getThreadStack().isTimedWaiting()){
			Cell cell = addCell(row, pos, stackCount, STYLE_FRAME_SIMPLE);
			setColorForeground(cell, CellColor.buildColor(this.sheetCfg.getTimedWaitingColor()));
		}
		else if(this.sheetCfg.hasRunningColor() && stack.getThreadStack().isRunning()){
			Cell cell = addCell(row, pos, stackCount, STYLE_FRAME_SIMPLE);
			setColorForeground(cell, CellColor.buildColor(this.sheetCfg.getRunningColor()));
		}
		else{
			addCell(row, pos, stackCount, getCellStyleReference(stackCount));
		}
	}

	private void displayGraphs(Sheet sheet, Multiset<ThreadStackHandler> stacks, int linePos, int rowPos) throws JzrException {
		if (!this.sheetCfg.isFunctionGraphDisplayed() && !this.sheetCfg.isContentionGraphDisplayed())
			return;
		
		linePos += 10;
		rowPos += 10;
		int contentionRowPos = rowPos;
		int contentionLinePos = linePos;
		
		// add function graph
		if (this.sheetCfg.isFunctionGraphDisplayed()){
			GlobalStackGraphGenerator graphGenerator =  new GlobalStackGraphGenerator(
					this.displayContext.getSetupManager().getGraphSetupMgr(),
					this.sheetCfg.getFunctionGraphCfg(),
					this.sheetCfg.getName(),
					this.displayContext.getGraphPictureRepository());
		
			for (ThreadStackHandler stack : stacks.elementSet()){
				graphGenerator.addStack(stack, stacks.count(stack));
			}
		
			ExcelGraphPicture picture = graphGenerator.generateGraphPicture();
		
			if (picture != null){
				picture.setParentLinkCell(getValidCell(getValidRow(sheet, 0), 2));
				addGraphPicture(sheet, this.sheetCfg.getName(), picture, linePos, rowPos, ACTION_GRAPH_SINGLE);
				this.graphItemsCount++;
				
				clearGraphPicture(picture);
				
				contentionRowPos += this.sheetCfg.getFunctionGraphCfg().getExcelResolution().getWidth() + 4;  // 2 for the borders, 2 for the separation
			}
		}

		// add contention type graph, display on the right
		if (this.sheetCfg.isContentionGraphDisplayed()){
			GlobalContentionTypeGraphGenerator contentionGraphGenerator =  new GlobalContentionTypeGraphGenerator(
					this.displayContext.getSetupManager().getGraphSetupMgr(),
					this.sheetCfg.getContentionGraphCfg(),
					this.sheetCfg.getName(),
					this.displayContext.getGraphPictureRepository());
			
			Multiset<Tag> principalTags = session.getPrincipalContentionTypeSet();
			for (Tag tag : principalTags.elementSet()){
				contentionGraphGenerator.addContentionType(tag, principalTags.count(tag), principalTags.size());
			}
			
			ExcelGraphPicture picture = contentionGraphGenerator.generateGraphPicture();
			
			if (picture != null){
				int linkPos = this.sheetCfg.isFunctionGraphDisplayed() ? 3 : 2;
				picture.setParentLinkCell(getValidCell(getValidRow(sheet, 0), linkPos));
				addGraphPicture(sheet, this.sheetCfg.getName(), picture, contentionLinePos, contentionRowPos, CONTENTION_GRAPH_SINGLE);
				this.graphItemsCount++;
				
				clearGraphPicture(picture);
			}
		}
	}

	private int displayCodeLines(Sheet sheet, ThreadStackHandler stack, int linePos, int stackCount) {
		SortedMap<Integer, String> functions = stack.getThreadStack().getSourceLocalizedFunctionTags();
		SortedMap<Integer, String> operations = stack.getThreadStack().getSourceLocalizedOperationTags();
		SortedMap<Integer, String> contentionTypes = stack.getThreadStack().getSourceLocalizedContentionTypeTags();

		if (stack.getThreadStack().isLocked())
			linePos= displayLockClassLine(sheet, stack, linePos, stackCount);
		
		List<String> codeLines =  stack.getCodeLines();
		for(int i=0 ; i<codeLines.size(); i++){
			boolean locked = stack.getThreadStack().isLocked() && i==0;
			
			String contentionType = null;
			String operation = null;
			if (this.sheetCfg.isOperationDisplayed()){
				operation = operations.get(codeLines.size()-i-1);  // can be null
				contentionType = contentionTypes.get(codeLines.size()-i-1);  // can be null
			}
			
			String function = null;
			if (this.sheetCfg.isFunctionDisplayed())
				function = functions.get(codeLines.size()-i-1); // can be null
			
			Row row = sheet.createRow(linePos++);
			String codeLine = AnalyzerHelper.stripCodeLine(codeLines.get(i), javaModuleSupport && this.sheetCfg.isJavaModuleStripped());
			if (operation!= null){
				// if both function and operation are available, operation takes priority
				displayCodeLineWithOperation(row, codeLine, operation, contentionType, locked);
			}else if (function != null){
				displayCodeLineWithFunction(row, codeLine, function, locked);
			}else{
				displayCodeLine(row, codeLine, stackCount, locked);
			}
		}
		
		return linePos;
	}

	private int displayLockClassLine(Sheet sheet, ThreadStackHandler stack, int linePos, int stackCount) {
		String lockClassName = null;
		if (stack.getThreadStack().isBlocked())
			lockClassName = stack.getThreadStack().getLockClassName();
		else if (stack.getThreadStack().isCodeLocked())
			lockClassName = stack.getThreadStack().getCodeLockName();
		
		if (lockClassName == null || lockClassName.isEmpty())
			return linePos;  // not available : nothing to display
		
		lockClassName = WAITING_TO_LOCK + lockClassName;
		String italicReferenceStyle = getCellStyleItalicReference(stackCount);
		
		Row row = sheet.createRow(linePos++);
		Cell cell = addCell(row, 0, LOCKED, STYLE_CELL_ITALIC_ALIGN_RIGHT);
		setColorForeground(cell, LOCKED_COLOR);
		addCell(row, 1, lockClassName, italicReferenceStyle);
		addEmptyCell(row, 2, italicReferenceStyle);
		addEmptyCell(row, 3, italicReferenceStyle);
		addEmptyCell(row, 4, italicReferenceStyle);
		addEmptyCell(row, 5, italicReferenceStyle);
		addEmptyCell(row, 6, italicReferenceStyle);
		addEmptyCell(row, 7, italicReferenceStyle);
		addEmptyCell(row, 8, italicReferenceStyle);
		addEmptyCell(row, 9, italicReferenceStyle);
		addEmptyCell(row, 10, italicReferenceStyle);
		
		return linePos;
	}

	private void displayCodeLineWithFunction(Row row, String line, String function, boolean blocked) {
		String color = this.sheetCfg.getFunctionColor();
		if (blocked){
			Cell cell = addCell(row, 0, LOCKED, STYLE_CELL_ITALIC_ALIGN_RIGHT);
			setColorForeground(cell, LOCKED_COLOR);
		}
		else{
			addEmptyCell(row, 0, STYLE_CELL_ITALIC, color);
		}
		addCell(row, 1, line, STYLE_CELL_ITALIC,color);
		addEmptyCell(row, 2, STYLE_CELL_ITALIC, color);
		addEmptyCell(row, 3, STYLE_CELL_ITALIC, color);
		addEmptyCell(row, 4, STYLE_CELL_ITALIC, color);
		addCell(row, 5, function, STYLE_CELL_CENTERED_SMALL_ITALIC, color);
		addEmptyCell(row, 6, STYLE_CELL_ITALIC, color);
		addEmptyCell(row, 7, STYLE_CELL_ITALIC, color);
		addEmptyCell(row, 8, STYLE_CELL_ITALIC, color);
		addEmptyCell(row, 9, STYLE_CELL_ITALIC, color);
		addEmptyCell(row, 10, STYLE_CELL_ITALIC, color);
	}

	private void displayCodeLineWithOperation(Row row, String line, String operation, String contentionType, boolean blocked) {
		String color = this.sheetCfg.getOperationColor();
		if (blocked){
			Cell cell = addCell(row, 0, LOCKED, STYLE_CELL_ITALIC_ALIGN_RIGHT);
			setColorForeground(cell, LOCKED_COLOR);
		}
		else{
			addEmptyCell(row, 0, STYLE_CELL_ITALIC, color);
		}
		addCell(row, 1, line, STYLE_CELL_ITALIC,color);
		addEmptyCell(row, 2, STYLE_CELL_ITALIC, color);
		addEmptyCell(row, 3, STYLE_CELL_ITALIC, color);
		addEmptyCell(row, 4, STYLE_CELL_ITALIC, color);
		addEmptyCell(row, 5, STYLE_CELL_ITALIC, color);
		addCell(row, 6, operation, STYLE_CELL_CENTERED_SMALL_ITALIC, color);
		if (contentionType != null)
			addCell(row, 6, contentionType, STYLE_CELL_CENTERED_SMALL_ITALIC, color);
		else
			addEmptyCell(row, 7, STYLE_CELL_ITALIC, color);
		addEmptyCell(row, 7, STYLE_CELL_ITALIC, color);
		addEmptyCell(row, 8, STYLE_CELL_ITALIC, color);
		addEmptyCell(row, 9, STYLE_CELL_ITALIC, color);
		addEmptyCell(row, 10, STYLE_CELL_ITALIC, color);
	}

	private void displayCodeLine(Row row, String line, int stackCount, boolean blocked) {
		String italicReferenceStyle = getCellStyleItalicReference(stackCount);
		
		if (blocked){
			Cell cell = addCell(row, 0, LOCKED, STYLE_CELL_ITALIC_ALIGN_RIGHT);
			setColorForeground(cell, LOCKED_COLOR);
		}
		else{
			addEmptyCell(row, 0, italicReferenceStyle);
		}
		addCell(row, 1, line, italicReferenceStyle);
		addEmptyCell(row, 2, italicReferenceStyle);
		addEmptyCell(row, 3, italicReferenceStyle);
		addEmptyCell(row, 4, italicReferenceStyle);
		addEmptyCell(row, 5, italicReferenceStyle);
		addEmptyCell(row, 6, italicReferenceStyle);
		addEmptyCell(row, 7, italicReferenceStyle);
		addEmptyCell(row, 8, italicReferenceStyle);
		addEmptyCell(row, 9, italicReferenceStyle);
		addEmptyCell(row, 10, italicReferenceStyle);
	}

	private String mergeList(List<String> list){
		StringBuilder builder = new StringBuilder();
		int count = 1;
		for (String item : list){
			builder.append(item);
			if (count < list.size())
				builder.append(" / ");
			count++;
		}
		return builder.toString();
	}

}
