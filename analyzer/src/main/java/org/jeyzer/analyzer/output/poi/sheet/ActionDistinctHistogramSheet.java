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
import static org.jeyzer.analyzer.output.poi.style.DefaultCellStyles.*;
import static org.jeyzer.analyzer.output.poi.theme.AbstractTheme.STYLE_THEME_HEADER_VERY_SMALL_ROTATED;
import static org.jeyzer.analyzer.output.poi.theme.AbstractTheme.STYLE_THEME_TOP_BAR_JEYZER_TITLE;





import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.jeyzer.analyzer.config.report.ConfigActionDistinctHistogramSheet;
import org.jeyzer.analyzer.data.ThreadAction;
import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.analyzer.data.action.ActionGraphRootSection;
import org.jeyzer.analyzer.data.action.ActionGraphSection;
import org.jeyzer.analyzer.data.action.ActionGraphSectionComparator;
import org.jeyzer.analyzer.data.stack.ThreadStack;
import org.jeyzer.analyzer.data.tag.ContentionTypeTag;
import org.jeyzer.analyzer.data.tag.FunctionTag;
import org.jeyzer.analyzer.data.tag.OperationTag;
import org.jeyzer.analyzer.data.tag.Tag;
import org.jeyzer.analyzer.error.JzrException;
import org.jeyzer.analyzer.math.FormulaHelper;
import org.jeyzer.analyzer.output.graph.picture.ActionContentionTypeGraphGenerator;
import org.jeyzer.analyzer.output.graph.picture.ActionGraphGenerator;
import org.jeyzer.analyzer.output.graph.picture.ExcelGraphPicture;
import org.jeyzer.analyzer.output.poi.CellRefRepository;
import org.jeyzer.analyzer.output.poi.context.DisplayContext;
import org.jeyzer.analyzer.session.JzrSession;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;

public class ActionDistinctHistogramSheet extends JeyzerSheet {

	private ConfigActionDistinctHistogramSheet sheetCfg;
	
	public ActionDistinctHistogramSheet(ConfigActionDistinctHistogramSheet config, JzrSession session, DisplayContext displayContext) {
		super(session, displayContext);
		this.sheetCfg = config;
	}

	@Override
	public void display() throws JzrException {
		int linePos = 1;
		int rowPos=0;
		
		Sheet sheet = createSheet(this.sheetCfg);
		CellRefRepository cellRefRepository = this.displayContext.getCellRefRepository();
    	
    	rowPos = displayHeader(sheet, cellRefRepository, linePos, rowPos);
    	linePos++;

		Map<ThreadAction, ExcelGraphPicture> functionGraphPictures = new LinkedHashMap<ThreadAction, ExcelGraphPicture>();
		Map<ThreadAction, ExcelGraphPicture> contentionGraphPictures = new LinkedHashMap<ThreadAction, ExcelGraphPicture>();
    	
    	linePos = displayDistinctActions(sheet, session, cellRefRepository, linePos, functionGraphPictures, contentionGraphPictures);

    	createFilters(sheet, cellRefRepository, linePos, rowPos);
		sheet.createFreezePane(0, 2);
    	
    	createColumnGroups(sheet, cellRefRepository);

    	linePos = displayGraphPictures(sheet, functionGraphPictures, contentionGraphPictures, linePos, rowPos);
    	
    	addTopBar(sheet, rowPos);
    	addMenuLink(sheet, sheetCfg, STYLE_THEME_TOP_BAR_JEYZER_TITLE, 0, 0);
    	
    	close(this.sheetCfg);
	}
	
	private int displayGraphPictures(Sheet sheet, Map<ThreadAction, ExcelGraphPicture> graphPictures, Map<ThreadAction, ExcelGraphPicture> contentionGraphPictures, int linePos, int rowPos) {
		if (!this.sheetCfg.isFunctionGraphDisplayed() && !this.sheetCfg.isContentionGraphDisplayed())
			return linePos;
		
		linePos += 10;
		rowPos += 10;
		int contentionRowPos = rowPos;
		int contentionLinePos = linePos;
		Map<ThreadAction, Integer> lines = null;
		
		// add each graph picture
		if (this.sheetCfg.isFunctionGraphDisplayed()){
			ExcelGraphPicture candidate = null;
			lines = new HashMap<ThreadAction, Integer>(graphPictures.size());
			
			for (Map.Entry<ThreadAction,ExcelGraphPicture> entry : graphPictures.entrySet()){
				lines.put(entry.getKey(), linePos);
				linePos = addGraphPicture(sheet, this.sheetCfg.getName(), entry.getValue(), linePos, rowPos, ACTION_GRAPH);
				candidate = entry.getValue();
			}
			
			if (candidate != null){
				contentionRowPos += candidate.getExcelWidth() + 4;  // 2 for the borders, 2 for the separation
			}
			
			// clean pictures
			clearGraphPictures(graphPictures.values());
		}

		// display on the right
		if (this.sheetCfg.isContentionGraphDisplayed()){
			for (Map.Entry<ThreadAction,ExcelGraphPicture> entry : contentionGraphPictures.entrySet()){
				if (lines!= null)
					contentionLinePos = lines.get(entry.getKey()); // align with function picture 
				contentionLinePos = addGraphPicture(sheet, this.sheetCfg.getName(), entry.getValue(), contentionLinePos, contentionRowPos, CONTENTION_GRAPH);
			}
			
			// clean pictures
			clearGraphPictures(contentionGraphPictures.values());
		}
		
		if (this.sheetCfg.isContentionGraphDisplayed() && !this.sheetCfg.isFunctionGraphDisplayed())
			linePos = contentionLinePos;  // in case only contention is displayed
		
		return linePos;
	}

	private void createFilters(Sheet sheet, CellRefRepository cellRefRepository, int linePos, int rowPos) {
		int linkOffset = cellRefRepository.getActionSheetTypes().size();
		
		// graph offset
		int graphOffset = this.sheetCfg.isFunctionGraphDisplayed()? 1 : 0;
		graphOffset = this.sheetCfg.isContentionGraphDisplayed()? graphOffset + 1 : graphOffset;
		linkOffset += graphOffset;
		
		sheet.setAutoFilter(new CellRangeAddress(1, linePos-1, 1 + linkOffset, rowPos-1));
	}

	private void createColumnGroups(Sheet sheet, CellRefRepository cellRefRepository) {
		int linkSize = cellRefRepository.getActionSheetTypes().size();
		int columnOffset = linkSize;
		int graphOffset = this.sheetCfg.isFunctionGraphDisplayed()? 1 : 0;
		graphOffset = this.sheetCfg.isContentionGraphDisplayed()? graphOffset + 1 : graphOffset;
		columnOffset += graphOffset;
		
		// links group
		if (linkSize>1)
			createColumnGroup(sheet, 2, linkSize, true); // first link column is always visible
		
		// time group (time start is always displayed)
		createColumnGroup(sheet, 2 + columnOffset, 7 + columnOffset, true);

		// thread group
		createColumnGroup(sheet, 9 + columnOffset, 11 + columnOffset, true);
	}	

	private int displayDistinctActions(Sheet sheet, JzrSession session, CellRefRepository cellRefRepository, int linePos, 
			Map<ThreadAction, ExcelGraphPicture> graphPictures,
			Map<ThreadAction, ExcelGraphPicture> contentionGraphPictures) throws JzrException {
		List<ThreadAction> actions = getActions(session);
		this.itemsCount = actions.size();
		
		buildGraphPictures(graphPictures, contentionGraphPictures);
		
		for(ThreadAction action : actions){
			Multiset<Tag> actionTags = getActionTags(action);
			linePos = displayAction(sheet, cellRefRepository, action, actionTags, session.getActionsStackSize(), linePos, graphPictures, contentionGraphPictures);
		}
		
		return linePos;
	}
	
	private void buildGraphPictures(Map<ThreadAction, ExcelGraphPicture> functionGraphPictures, Map<ThreadAction, ExcelGraphPicture> contentionGraphPictures) throws JzrException {
		if (!this.sheetCfg.isFunctionGraphDisplayed() && !this.sheetCfg.isContentionGraphDisplayed())
			return;
		
		List<Map.Entry<ThreadAction, ActionGraphSection>> actionGraphSections = buildActionGraphSectionList();

		ActionGraphGenerator functionGraphGenerator = null;
		if (this.sheetCfg.isFunctionGraphDisplayed()){
			functionGraphGenerator =  new ActionGraphGenerator(
					this.displayContext.getSetupManager().getGraphSetupMgr(),
					this.sheetCfg.getFunctionGraphCfg(),
					this.sheetCfg.getName(),
					this.displayContext.getGraphPictureRepository());
		}
		
		ActionContentionTypeGraphGenerator contentionGraphGenerator = null;
		if (this.sheetCfg.isContentionGraphDisplayed()){
			contentionGraphGenerator =  new ActionContentionTypeGraphGenerator(
					this.displayContext.getSetupManager().getGraphSetupMgr(),
					this.sheetCfg.getContentionGraphCfg(),
					this.sheetCfg.getName(),
					this.displayContext.getGraphPictureRepository());			
		}
		
		// before
		if (logger.isDebugEnabled()){
			logger.debug("Action graph sections : order before sorting");
			for (Map.Entry<ThreadAction, ActionGraphSection> rootSection : actionGraphSections){
				logger.debug("Action name : {} / distinct nodes : {}", rootSection.getValue().getFunction(), rootSection.getValue().getTreeFunctions().size() + rootSection.getValue().getTreeOperations().size());
			}
		}
		
		// Required to keep action dates on generated pictures and therefore benefit from picture caching
		Map<ActionGraphSection, ThreadAction> actions = new HashMap<>();
		
		// sort action graph sections per number of operation/functions, from greatest to smallest
		List<ActionGraphSection> sortedActionGraphSections = new ArrayList<>();
    	for (Entry<ThreadAction,ActionGraphSection> actionGraphSection : actionGraphSections){
    		sortedActionGraphSections.add(actionGraphSection.getValue());
    		actions.put(actionGraphSection.getValue(), actionGraphSection.getKey());
    	}
    	sortedActionGraphSections.sort(new ActionGraphSectionComparator.TreeFunctionAndOperationSizeComparator());
		
		// after
		if (logger.isDebugEnabled()){
			logger.debug("Action graph sections : order after sorting");
			for (ActionGraphSection rootSection : sortedActionGraphSections){
				logger.debug("Action name : {} / distinct nodes : {}", rootSection.getFunction(), rootSection.getTreeFunctions().size() + rootSection.getTreeOperations().size());
			}
		}
		
		// generate the graphs, in the number of operation/functions order
		Map<ActionGraphSection, ExcelGraphPicture> generatedGraphPictures = new HashMap<>();	// order is not important
		Map<ActionGraphSection, ExcelGraphPicture> generatedContentionGraphPictures = new HashMap<>();	// order is not important
    	for (ActionGraphSection rootSection : sortedActionGraphSections){
    		// generate the function type graph
    		buildFunctionGraphPicture(rootSection, actions, generatedGraphPictures, functionGraphGenerator);
			// generate the contention type graph
			buildContentionGraphPicture(rootSection, actions, generatedContentionGraphPictures, contentionGraphGenerator);
    	}
    	
    	// order the generated pictures in the action display order, to get the same display order at picture level
    	for (Map.Entry<ThreadAction, ActionGraphSection> rootSection : actionGraphSections){
    		ExcelGraphPicture picturePath = generatedGraphPictures.get(rootSection.getValue());
    		if (picturePath!= null)
    			functionGraphPictures.put(rootSection.getKey(), picturePath);
    		
			// add the contention type graph picture
			ExcelGraphPicture contentionPicturePath = generatedContentionGraphPictures.get(rootSection.getValue());
			if (contentionPicturePath != null)
				contentionGraphPictures.put(rootSection.getKey(), contentionPicturePath);
    	}
	}

	private void buildFunctionGraphPicture(ActionGraphSection rootSection, Map<ActionGraphSection, ThreadAction> actions,
			Map<ActionGraphSection, ExcelGraphPicture> generatedGraphPictures, ActionGraphGenerator graphGenerator) {
		if (!this.sheetCfg.isFunctionGraphDisplayed() || graphGenerator == null)
			return;
		
		ExcelGraphPicture picturePath = graphGenerator.generateGraphPicture(
				rootSection.getFunction(),
				rootSection.getExecutor(),
				rootSection,
				actions.get(rootSection).getStrId());
		if (picturePath!= null){
			generatedGraphPictures.put(rootSection, picturePath);
			this.graphItemsCount++;
		}
	}

	private void buildContentionGraphPicture(ActionGraphSection rootSection, Map<ActionGraphSection, ThreadAction> actions, 
			Map<ActionGraphSection, ExcelGraphPicture> generatedContentionGraphPictures, ActionContentionTypeGraphGenerator contentionGraphGenerator) throws JzrException {
		if (!this.sheetCfg.isContentionGraphDisplayed() || contentionGraphGenerator == null)
			return;
		
		ExcelGraphPicture contentionPicturePath = contentionGraphGenerator.generateGraphPicture(
				rootSection.getFunction(),
				rootSection.getExecutor(),
				rootSection,
				actions.get(rootSection).getStrId());
		
		if (contentionPicturePath!= null){
			generatedContentionGraphPictures.put(rootSection, contentionPicturePath);
			this.graphItemsCount++;
		}
	}

	private Multiset<Tag> getActionTags(ThreadAction action) {
		// use multi set to get it ordered by appearance
		Multiset<Tag> tagMultiSet = HashMultiset.create();

		for (int i = 0 ; i<action.size(); i++){
			for(String functionTag : action.getThreadStack(i).getFunctionTags()){
				tagMultiSet.add(new FunctionTag(functionTag));
			}
				
			for(String operationTag : action.getThreadStack(i).getOperationTags()){
				tagMultiSet.add(new OperationTag(operationTag));
			}

			for(String contentionTypeTag : action.getThreadStack(i).getContentionTypeTags()){
				tagMultiSet.add(new ContentionTypeTag(contentionTypeTag));
			}
		}
		
		return tagMultiSet;
	}

	private List<ThreadAction> getActions(JzrSession session) {
		List<ThreadAction> actions = new ArrayList<>();
		
		for (ThreadDump dump : session.getDumps()){
			Date timestamp = dump.getTimestamp();
			List<ThreadAction> actionCandidates = new ArrayList<>(session.getActionHistory().get(timestamp));
			
			// filter the actions : keep only the ones above the threshold 
			for (ThreadAction actionCandidate : actionCandidates){
				if (actionCandidate.size() >= this.sheetCfg.getThreshold())
					actions.add(actionCandidate);
			}
			
			// sort by id
			actions.sort(new Comparator<ThreadAction>() {
				@Override
	            public int compare(ThreadAction a1, ThreadAction a2) {
	                return a1.getId() < a2.getId() ? -1:
	                	a1.getId() == a2.getId()? 0: 1;
	            	}
				}
			);
		}

		return actions;
	}

	private int displayAction(Sheet sheet, CellRefRepository cellRefRepository, ThreadAction action, Multiset<Tag> actionTags, int globalActionsStackSize, int linePos, 
			Map<ThreadAction, ExcelGraphPicture> graphPictures, Map<ThreadAction, ExcelGraphPicture> contentionGraphPictures) {
		int firstLine = linePos;
		
		// display action tags first
		for (Tag tag : Multisets.copyHighestCountFirst(actionTags).elementSet()){
			displayTag(cellRefRepository, action, tag, actionTags.count(tag), globalActionsStackSize, sheet, linePos);
			linePos++;
		}
		
		// display action line
		displayActionLine(sheet, cellRefRepository, action, linePos++, graphPictures, contentionGraphPictures);
		
		// group it
		sheet.groupRow(firstLine, linePos-2);
		sheet.setRowGroupCollapsed(firstLine, true);
		
		return linePos;
	}

	private void displayActionLine(Sheet sheet, CellRefRepository cellRefRepository, ThreadAction action, int linePos, Map<ThreadAction, ExcelGraphPicture> graphPictures, Map<ThreadAction, ExcelGraphPicture> contentGraphPictures) {
		int rowPos = 0;
		Row row = sheet.createRow(linePos);
		
		// Action id
		addCell(row, rowPos++, (long)action.getId(), STYLE_CELL_LEVEL_1);
    	
		// -----------------------------
		// Graph link
		// -----------------------------
		if (this.sheetCfg.isFunctionGraphDisplayed())
			rowPos = displayGraphPictureLink(graphPictures, action, row, rowPos);

		// -----------------------------
		// Contention graph link
		// -----------------------------
		if (this.sheetCfg.isContentionGraphDisplayed())
			rowPos = displayGraphPictureLink(contentGraphPictures, action, row, rowPos);
		
		// -----------------------------
		// Action links
		// -----------------------------
		rowPos = displayActionLinks(cellRefRepository, action, row, rowPos);
		
		// -----------------------------
		// Time section
		// -----------------------------
		addCell(row, rowPos++, convertToTimeZone(action.getMinStartDate()), STYLE_CELL_DATE_LEVEL_1);
		addCell(row, rowPos++, convertToTimeZone(action.getStartDate()), STYLE_CELL_DATE_LEVEL_1);
		addCell(row, rowPos++, convertToTimeZone(action.getEndDate()), STYLE_CELL_DATE_LEVEL_1);
		addCell(row, rowPos++, convertToTimeZone(action.getMaxEndDate()), STYLE_CELL_DATE_LEVEL_1);
		addCell(row, rowPos++, action.getMinDuration(), STYLE_CELL_LEVEL_1);
		addCell(row, rowPos++, action.getMaxDuration(), STYLE_CELL_LEVEL_1);
		addCell(row, rowPos++, getActionMissingDumpsCount(action), STYLE_CELL_LEVEL_1);

		// -----------------------------
		// Thread section
		// -----------------------------
		addCell(row, rowPos++, action.getExecutor(), STYLE_CELL_LEVEL_1);
		addCell(row, rowPos++, action.getThreadId(), STYLE_CELL_LEVEL_1);
		addCell(row, rowPos++, action.getName(), STYLE_CELL_LEVEL_1);
		addCell(row, rowPos++, action.getPriority(), STYLE_CELL_LEVEL_1);		
		
		// -----------------------------
		// Action section
		// -----------------------------
		Cell cell = addCell(row, rowPos++, action.getPrincipalCompositeFunction(), STYLE_CELL_LEVEL_1);
		registerActionLink(action.getId(), this.sheetCfg.getName(), cell);
		addEmptyCell(row, rowPos++, STYLE_CELL_LEVEL_1); // function or operation appearance count
		addEmptyCell(row, rowPos++, STYLE_CELL_LEVEL_1); // function or operation appearance %
		addEmptyCell(row, rowPos++, STYLE_CELL_LEVEL_1); // function or operation global appearance %
		addEmptyCell(row, rowPos++, STYLE_CELL_LEVEL_1); // function or operation type
		addCell(row, rowPos++, action.size(), STYLE_CELL_LEVEL_1);
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

	private int displayHeader(Sheet sheet, CellRefRepository cellRefRepository, int linePos, int rowPos) {
    	// Headers
    	Row row = sheet.createRow(linePos);
        
    	sheet.setColumnWidth(rowPos, 6*256);
    	addHeaderCell(row, rowPos++, "Id");
    	
		// -----------------------------
    	// Graph section
		// -----------------------------
    	if (this.sheetCfg.isFunctionGraphDisplayed()){
    		sheet.setColumnWidth(rowPos, 4*256);
    		addHeaderCell(row, rowPos++, "Graph", STYLE_THEME_HEADER_VERY_SMALL_ROTATED);
    	}

		// -----------------------------
    	// Graph section
		// -----------------------------
    	if (this.sheetCfg.isContentionGraphDisplayed()){
    		sheet.setColumnWidth(rowPos, 4*256);
    		addHeaderCell(row, rowPos++, "Cont", STYLE_THEME_HEADER_VERY_SMALL_ROTATED);
    	}
    	
		// -----------------------------
    	// Links section
		// -----------------------------
    	
    	for (String sheetType : cellRefRepository.getActionSheetTypes()){
        	sheet.setColumnWidth(rowPos, 4*256);
        	addHeaderCell(row, rowPos++, sheetType, STYLE_THEME_HEADER_VERY_SMALL_ROTATED);    	
    	}
    	
		// -----------------------------
    	// Time section    	
		// -----------------------------
    	
    	sheet.setColumnWidth(rowPos, 18*256);
    	addHeaderCell(row, rowPos++, "Min start date");
        
    	sheet.setColumnWidth(rowPos, 18*256);
    	addHeaderCell(row, rowPos++, "Max start date");
    	
    	sheet.setColumnWidth(rowPos, 18*256);
    	addHeaderCell(row, rowPos++, "Min end date");
    	
    	sheet.setColumnWidth(rowPos, 18*256);
    	addHeaderCell(row, rowPos++, "Max end date");
    	
    	sheet.setColumnWidth(rowPos, 20*256);
    	Cell cell = addHeaderCell(row, rowPos++, "Min Duration (sec)");
    	addComment(sheet, cell, "Multiple of " + session.getThreadDumpPeriod() + " sec (thread dump period)", 1, 2);    	

    	sheet.setColumnWidth(rowPos, 20*256);
    	cell = addHeaderCell(row, rowPos++, "Max Duration (sec)");
    	addComment(sheet, cell, "Multiple of " + session.getThreadDumpPeriod() + " sec (thread dump period)", 1, 2);
    	
    	sheet.setColumnWidth(rowPos, 15*256);
    	addHeaderCell(row, rowPos++, "# hiatus");

		// -----------------------------
    	// Thread section
		// -----------------------------
    	
    	sheet.setColumnWidth(rowPos, 30*256);
    	addHeaderCell(row, rowPos++, "Executor");

    	sheet.setColumnWidth(rowPos, 15*256);
    	addHeaderCell(row, rowPos++, "Thread id");
    	
    	sheet.setColumnWidth(rowPos, 25*256);
    	addHeaderCell(row, rowPos++, "Thread name");
    	
    	sheet.setColumnWidth(rowPos, 15*256);
    	addHeaderCell(row, rowPos++, "Thread priority");

		// -----------------------------
    	// Action / Histogram section    	
		// -----------------------------
    	
    	sheet.setColumnWidth(rowPos, 50*256);
    	cell = addHeaderCell(row, rowPos++, "Action / Principal function\r\n (Function / Operation)");
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
    	
    	sheet.setColumnWidth(rowPos, 15*256);
    	addHeaderCell(row, rowPos++, "# Recording snapshots");
    	
		return rowPos;
	}

	private void displayTag(CellRefRepository cellRefRepository, ThreadAction action, Tag tag, int tagActionStackCount, int globalActionsStackSize, Sheet sheet, int linePos) {
    	Row row = sheet.createRow(linePos);
    	int rowPos=0;
    	
		addEmptyCell(row, rowPos++);  // id
		
		if(this.sheetCfg.isFunctionGraphDisplayed())
			addEmptyCell(row, rowPos++);  // graph picture

		if(this.sheetCfg.isContentionGraphDisplayed())
			addEmptyCell(row, rowPos++);  // contention picture
		
    	// action links
		Iterator<String> iter = cellRefRepository.getActionSheetTypes().iterator();
    	while (iter.hasNext()){
        	addEmptyCell(row, rowPos++);
        	iter.next();
    	}
    	
		addEmptyCell(row, rowPos++); // min start date
		addEmptyCell(row, rowPos++);
		addEmptyCell(row, rowPos++); // min end date
		addEmptyCell(row, rowPos++);
		addEmptyCell(row, rowPos++); // min duration
		addEmptyCell(row, rowPos++); // max duration
		addEmptyCell(row, rowPos++); // # hiatus

		addEmptyCell(row, rowPos++); // executor
		addEmptyCell(row, rowPos++); // thread id
		addEmptyCell(row, rowPos++); // thread name
		addEmptyCell(row, rowPos++); // thread priority
		
    	addCell(row, rowPos++, tag.getName(), STYLE_CELL);
    	addCell(row, rowPos++, tagActionStackCount, STYLE_CELL);
    	addCell(row, rowPos++, FormulaHelper.percentRound(tagActionStackCount, action.size()), STYLE_CELL);
    	addCell(row, rowPos++, FormulaHelper.percentRound(tagActionStackCount, globalActionsStackSize), STYLE_CELL);
    	addCell(row, rowPos++, tag.getTypeName(), STYLE_CELL);
    	
    	addEmptyCell(row, rowPos++); // thread count
	}
	
	private int displayActionLinks(CellRefRepository cellRefRepository, ThreadAction action, Row row, int rowPos) {
    	for (String sheetType : cellRefRepository.getActionSheetTypes()){
    		CellReference cellRef = cellRefRepository.getCellRef(sheetType, action);
    		if (cellRef != null)
    			addCell(row, rowPos++, sheetType.substring(0, 1).toUpperCase(), cellRef, STYLE_CELL_LINK_LEVEL_1);
    		else
    			addEmptyCell(row, rowPos++, STYLE_CELL_LEVEL_1);
    	}
    	
    	return rowPos;
	}
	
	private List<Map.Entry<ThreadAction, ActionGraphSection>> buildActionGraphSectionList() {
		List<Map.Entry<ThreadAction, ActionGraphSection>> actionGraphSections = new ArrayList<>();
		
		for (ThreadDump dump : session.getDumps()){
			Date timestamp = dump.getTimestamp();
			Set<ThreadAction> actions = session.getActionHistory().get(timestamp);
		
			for(ThreadAction action : actions){
				if (action.size() < this.sheetCfg.getThreshold())
					continue; // ignore
			
				ThreadStack stack = action.getThreadStack(0);
				ActionGraphSection section = new ActionGraphRootSection(stack, action, false);
				
				Map.Entry<ThreadAction, ActionGraphSection> entry = new AbstractMap.SimpleEntry<ThreadAction, ActionGraphSection>(action, section); 
				actionGraphSections.add(entry);
			
				for (int i=1; i<action.size(); i++){
					stack = action.getThreadStack(i);
					if (!section.acceptNewStack(stack))
						// should never happen. there is always a common root code line
						logger.error("Failed to integrate stack into new root action graph. Stack is : \n{}", stack.toString());
				}
			}
		}
		
		return actionGraphSections;
	}
	
	private int displayGraphPictureLink(Map<ThreadAction, ExcelGraphPicture> graphPictures, ThreadAction action, Row row, int rowPos) {
		// cell link will be created at later stage, so always create empty cell to start with
		Cell cell = addEmptyCell(row, rowPos++, STYLE_CELL_LEVEL_1);

		ExcelGraphPicture picture = graphPictures.get(action);
		if (picture != null)
			// update the picture with the parent cell that will link it
			picture.setParentLinkCell(cell);
		
		return rowPos;
	}
}
