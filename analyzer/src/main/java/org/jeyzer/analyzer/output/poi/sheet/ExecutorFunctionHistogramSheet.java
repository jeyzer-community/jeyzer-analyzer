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



import static org.jeyzer.analyzer.output.graph.picture.ExcelGraphPicture.GraphType.ACTION_GRAPH;
import static org.jeyzer.analyzer.output.graph.picture.ExcelGraphPicture.GraphType.CONTENTION_GRAPH;
import static org.jeyzer.analyzer.output.poi.theme.AbstractTheme.*;





import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.jeyzer.analyzer.config.report.ConfigExecutorFunctionHistogramSheet;
import org.jeyzer.analyzer.data.ThreadAction;
import org.jeyzer.analyzer.data.action.ActionGraphRootSection;
import org.jeyzer.analyzer.data.action.ActionGraphSection;
import org.jeyzer.analyzer.data.stack.ThreadStack;
import org.jeyzer.analyzer.data.tag.Tag;
import org.jeyzer.analyzer.error.JzrException;
import org.jeyzer.analyzer.math.FormulaHelper;
import org.jeyzer.analyzer.output.graph.picture.ActionContentionTypeGraphGenerator;
import org.jeyzer.analyzer.output.graph.picture.ActionGraphGenerator;
import org.jeyzer.analyzer.output.graph.picture.ExcelGraphPicture;
import org.jeyzer.analyzer.output.poi.context.DisplayContext;
import org.jeyzer.analyzer.session.JzrSession;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;

public class ExecutorFunctionHistogramSheet extends JeyzerSheet{

	private ConfigExecutorFunctionHistogramSheet sheetCfg;
	
	public ExecutorFunctionHistogramSheet(ConfigExecutorFunctionHistogramSheet config, JzrSession session, DisplayContext displayContext) {
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
    	
    	// key = action composite name
    	Map<String, ExcelGraphPicture> functionGraphPictures = new LinkedHashMap<String, ExcelGraphPicture>();
    	Map<String, ExcelGraphPicture> contentionGraphPictures = new LinkedHashMap<String, ExcelGraphPicture>();
    	
    	linePos = displayTags(sheet, functionGraphPictures, contentionGraphPictures, linePos);
    	
    	createFilters(sheet, linePos, rowPos);

    	linePos = displayGraphPictures(sheet, functionGraphPictures, contentionGraphPictures, linePos, rowPos);
    	
    	addTopBar(sheet, rowPos);
    	addMenuLink(sheet, sheetCfg, STYLE_THEME_TOP_BAR_JEYZER_TITLE, 0, 0);
    	
    	close(this.sheetCfg);
	}

	private void createFilters(Sheet sheet, int linePos, int rowPos) {
    	int graphOffset = this.sheetCfg.isFunctionGraphDisplayed() ? 1 : 0;
    	graphOffset = this.sheetCfg.isContentionGraphDisplayed()? graphOffset + 1 : graphOffset;
    	sheet.setAutoFilter(new CellRangeAddress(1, linePos, 0 + graphOffset, rowPos-1));
	}

	private int displayGraphPictures(Sheet sheet, Map<String, ExcelGraphPicture> functionGraphPictures, Map<String, ExcelGraphPicture> contentionGraphPictures, int linePos, int rowPos) throws JzrException {
		if (!this.sheetCfg.isFunctionGraphDisplayed() && !this.sheetCfg.isContentionGraphDisplayed())
			return linePos;

		linePos += 10;
		rowPos += 10;
		int contentionRowPos = rowPos;
		int contentionLinePos = linePos;
		Map<String, Integer> lines = null;
		
		// add each graph picture
		if (this.sheetCfg.isFunctionGraphDisplayed()){
			ExcelGraphPicture candidate = null;
			lines = new HashMap<String, Integer>(functionGraphPictures.size());
			
			// add each graph picture
			for (Map.Entry<String,ExcelGraphPicture> entry : functionGraphPictures.entrySet()){
				lines.put(entry.getKey(), linePos);
				linePos = addGraphPicture(sheet, this.sheetCfg.getName(), entry.getValue(), linePos, rowPos, ACTION_GRAPH);
				candidate = entry.getValue();
				this.graphItemsCount++;
			}
			
			if (candidate != null)
				contentionRowPos += candidate.getExcelWidth() + 4;  // 2 for the borders, 2 for the separation
			
			// clean pictures
			clearGraphPictures(functionGraphPictures.values());
		}
		
		// display on the right
		if (this.sheetCfg.isContentionGraphDisplayed()){
			for (Map.Entry<String,ExcelGraphPicture> entry : contentionGraphPictures.entrySet()){
				if (lines!= null)
					contentionLinePos = lines.get(entry.getKey()); // align with function picture
				contentionLinePos = addGraphPicture(sheet, this.sheetCfg.getName(), entry.getValue(), contentionLinePos, contentionRowPos, CONTENTION_GRAPH);
				this.graphItemsCount++;
			}
			
			// clean pictures
			clearGraphPictures(contentionGraphPictures.values());
		}
		
		if (this.sheetCfg.isContentionGraphDisplayed() && !this.sheetCfg.isFunctionGraphDisplayed())
			linePos = contentionLinePos;  // in case only contention is displayed
		
		return linePos;
	}

	private int displayTags(Sheet sheet, Map<String, ExcelGraphPicture> functionGraphPictures, Map<String, ExcelGraphPicture> contentionGraphPictures, int linePos) throws JzrException {
    	Multimap<String, Tag> tagsPerExecutor = session.getFunctionSetPerExecutor();
    	tagsPerExecutor.putAll(session.getOperationSetPerExecutor());
    	tagsPerExecutor.putAll(session.getContentionTypeSetPerExecutor());
    	this.itemsCount = tagsPerExecutor.keySet().size();
    	
    	// total number of active stacks
    	int globalActionsStackSize = session.getActionsStackSize();
    	
    	Multimap<String, ThreadAction> actionsPerExecutor = session.getActionSetPerExecutor();
    	
    	int groupCount = 0;
    	for (String executor : Multisets.copyHighestCountFirst(tagsPerExecutor.keys()).elementSet()) {
    		Collection<Tag> tags = tagsPerExecutor.get(executor);
    		Collection<ThreadAction> actions = actionsPerExecutor.get(executor);
    		
    		// build graph
   			buildGraphPictures(executor, actions, functionGraphPictures, contentionGraphPictures);
    		
    		int actionStackCount = 0;
    		for (ThreadAction action : actions)
    			actionStackCount += action.getStackSize();

    		// use multi set to get it ordered by appearance
    		Multiset<Tag> tagMultiSet = HashMultiset.create();
    		for (Tag tag : tags){
    			tagMultiSet.add(tag);
    		}
    		
    		boolean first = true;
    		for (Tag tag : Multisets.copyHighestCountFirst(tagMultiSet).elementSet()){
    			linePos++;
    			displayTag(executor, tag, tagMultiSet.count(tag), actionStackCount, globalActionsStackSize, sheet, 
    					groupCount, linePos, functionGraphPictures, contentionGraphPictures, first);
    			first = false;
    		}
			groupCount++;
    	}
    	
		return linePos;
	}

	private void buildGraphPictures(String executor, Collection<ThreadAction> actions, Map<String, ExcelGraphPicture> functionGraphPictures, Map<String, ExcelGraphPicture> contentionGraphPictures) throws JzrException {
		if(!this.sheetCfg.isFunctionGraphDisplayed() && !this.sheetCfg.isContentionGraphDisplayed())
			return;
		
		// dummy action required to create the root section
		ThreadAction firstAction = actions.iterator().next();
		ActionGraphSection section = new ActionGraphRootSection(firstAction.getThreadStack(0), firstAction, false);
		
		for (ThreadAction action : actions){
			for (int i=0; i<action.size(); i++){
				ThreadStack stack = action.getThreadStack(i);
				if (!section.acceptNewStack(stack))
					// should never happen. there is always a common root code line
					logger.error("Failed to integrate stack into new root action graph. Stack is : \n{}", stack.toString());
			}
		}
		
		buildFunctionGraphPicture(section, executor, functionGraphPictures);
		buildContentionGraphPicture(section, executor, contentionGraphPictures);
	}
	
	private void buildContentionGraphPicture(ActionGraphSection section, String executor, Map<String, ExcelGraphPicture> contentionGraphPictures) throws JzrException {
		if (!this.sheetCfg.isContentionGraphDisplayed())
			return;
		
		ActionContentionTypeGraphGenerator contentionGraphGenerator =  new ActionContentionTypeGraphGenerator(
					this.displayContext.getSetupManager().getGraphSetupMgr(),
					this.sheetCfg.getContentionGraphCfg(),
					this.sheetCfg.getName(),
					this.displayContext.getGraphPictureRepository());
		
		ExcelGraphPicture picture = contentionGraphGenerator.generateGraphPicture(executor, executor, section);
		if (picture != null)
			contentionGraphPictures.put(executor, picture);
	}

	private void buildFunctionGraphPicture(ActionGraphSection section, String executor, Map<String, ExcelGraphPicture> graphPictures) throws JzrException {
		if(!this.sheetCfg.isFunctionGraphDisplayed())
			return;
		
		ActionGraphGenerator functionGraphGenerator = new ActionGraphGenerator(
					this.displayContext.getSetupManager().getGraphSetupMgr(),
					this.sheetCfg.getFunctionGraphCfg(),
					this.sheetCfg.getName(),
					this.displayContext.getGraphPictureRepository());			
		
		ExcelGraphPicture picture = functionGraphGenerator.generateGraphPicture(executor, executor, section);
		if (picture != null)
			graphPictures.put(executor, picture);
	}

	private int displayHeaders(Sheet sheet, int linePos) {
    	Row row = sheet.createRow(linePos);
    	int rowPos=0;
    	
    	Cell cell;
		if(this.sheetCfg.isFunctionGraphDisplayed()){
	    	sheet.setColumnWidth(rowPos, 5*256);
	    	cell = addHeaderCell(row, rowPos++, ""); // function graph picture
		}
		
		if(this.sheetCfg.isContentionGraphDisplayed()){
	    	sheet.setColumnWidth(rowPos, 5*256);
	    	cell = addHeaderCell(row, rowPos++, ""); // contention type graph picture
		}
        
    	sheet.setColumnWidth(rowPos, 50*256);
    	cell = addHeaderCell(row, rowPos++, "Executor");
        wrapText(cell);
        
    	sheet.setColumnWidth(rowPos, 50*256);
    	cell = addHeaderCell(row, rowPos++, "Sub function/operation");
    	wrapText(cell);
        
    	sheet.setColumnWidth(rowPos, 27*256);
    	cell = addHeaderCell(row, rowPos++, "Action\r\nappearance count");
    	wrapText(cell);
    	
    	sheet.setColumnWidth(rowPos, 27*256);
    	cell = addHeaderCell(row, rowPos++, "Action stack\r\nappearance %");
    	wrapText(cell);
    	
    	sheet.setColumnWidth(rowPos, 27*256);
    	cell = addHeaderCell(row, rowPos++, "Active stack\r\nglobal appearance %");
    	wrapText(cell);

    	sheet.setColumnWidth(rowPos, 16*256);
    	cell = addHeaderCell(row, rowPos++, "Type");
    	wrapText(cell);
    	
		return rowPos;
	}

	private void displayTag(String executor, Tag tag, int tagActionStackCount, int actionStackCount, int globalActionsStackSize, Sheet sheet, 
			int groupCount, int linePos, Map<String, ExcelGraphPicture> functionGraphPictures, Map<String, ExcelGraphPicture> contentionGraphPictures, boolean first) {
    	Row row = sheet.createRow(linePos);
    	int rowPos=0;

		if(this.sheetCfg.isFunctionGraphDisplayed()){
			if (first)
				rowPos = displayGraphPictureLink(functionGraphPictures, executor, row, rowPos, groupCount);
			else
				addEmptyCell(row, rowPos++, getCellStyleNumberReference(groupCount));
		}
		
		if(this.sheetCfg.isContentionGraphDisplayed()){
			if (first)
				rowPos = displayGraphPictureLink(contentionGraphPictures, executor, row, rowPos, groupCount);
			else
				addEmptyCell(row, rowPos++, getCellStyleNumberReference(groupCount));
		}
    	
        addCell(row, rowPos++, executor, getCellStyleReference(groupCount));
    	addCell(row, rowPos++, tag.getName(), getCellStyleNumberReference(groupCount));
    	addCell(row, rowPos++, tagActionStackCount, getCellStyleNumberReference(groupCount));
    	addCell(row, rowPos++, FormulaHelper.percentRound(tagActionStackCount, actionStackCount), getCellStyleNumberReference(groupCount));
    	addCell(row, rowPos++, FormulaHelper.percentRound(tagActionStackCount, globalActionsStackSize), getCellStyleNumberReference(groupCount));
    	addCell(row, rowPos++, tag.getTypeName(), getCellStyleReference(groupCount));
	}
	
	private int displayGraphPictureLink(Map<String, ExcelGraphPicture> graphPictures, String executor, Row row, int rowPos, int groupCount) {
		// cell link will be created at later stage, so always create empty cell to start with
		Cell cell = addEmptyCell(row, rowPos++, getCellStyleReference(groupCount));

		ExcelGraphPicture picture = graphPictures.get(executor);
		if (picture != null)
			// update the picture with the parent cell that will link it
			picture.setParentLinkCell(cell);
		
		return rowPos;
	}
	
}
