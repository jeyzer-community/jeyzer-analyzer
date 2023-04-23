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
import static org.jeyzer.analyzer.output.graph.picture.ExcelGraphPicture.GraphType.ACTION_GRAPH;
import static org.jeyzer.analyzer.output.graph.picture.ExcelGraphPicture.GraphType.CONTENTION_GRAPH;
import static org.jeyzer.analyzer.output.poi.CellText.*;
import static org.jeyzer.analyzer.output.poi.style.CellFonts.FONT_SYMBOL_9;
import static org.jeyzer.analyzer.output.poi.style.DefaultCellStyles.*;
import static org.jeyzer.analyzer.output.poi.theme.AbstractTheme.*;





import java.util.ArrayList;
import java.util.Collection;
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
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jeyzer.analyzer.config.report.ConfigActionProfilingSheet;
import org.jeyzer.analyzer.config.report.ConfigGraph;
import org.jeyzer.analyzer.data.ThreadAction;
import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.analyzer.data.action.ActionGraphRoot;
import org.jeyzer.analyzer.data.action.ActionGraphSection;
import org.jeyzer.analyzer.data.action.ActionGraphSectionComparator;
import org.jeyzer.analyzer.error.JzrException;
import org.jeyzer.analyzer.math.FormulaHelper;
import org.jeyzer.analyzer.output.graph.picture.ActionContentionTypeGraphGenerator;
import org.jeyzer.analyzer.output.graph.picture.ActionGraphGenerator;
import org.jeyzer.analyzer.output.graph.picture.ExcelGraphPicture;
import org.jeyzer.analyzer.output.poi.CellText;
import org.jeyzer.analyzer.output.poi.context.DisplayContext;
import org.jeyzer.analyzer.output.poi.style.CellFonts;
import org.jeyzer.analyzer.session.JzrSession;
import org.jeyzer.analyzer.util.AnalyzerHelper;
import org.slf4j.Logger;

public class ActionProfilingSheet extends JeyzerSheet {

	private static final int NOT_EXIST = -1;
	
	private static final int ALL_GROUP_START_INDEX = 12; // Group of All functions/operations/contention types
	
	private ConfigActionProfilingSheet sheetCfg;
	
	private ActionGraphRoot actionGraphRoot = null;
	private int totalActionStacks;
	private boolean javaModuleSupport;
	
	public ActionProfilingSheet(ConfigActionProfilingSheet sheetCfg, JzrSession session, DisplayContext displayContext) {
		super(session, displayContext);
		this.sheetCfg = sheetCfg;
		this.javaModuleSupport = session.isJavaModuleSupported();
	}

	@Override
	public void display() throws JzrException {
    	int linePos = 1;
    	Sheet sheet = createSheet(this.sheetCfg);
    	int rowPos=0;
    	
    	rowPos = displayHeader(sheet, linePos, rowPos);
    	linePos++;
    	sheet.createFreezePane(0, 2);
    	
    	addTopBar(sheet, rowPos);
    	addMenuLink(sheet, sheetCfg, STYLE_THEME_TOP_BAR_JEYZER_TITLE, 0, 1);

    	// key = thread name + action composite name
    	Map<String, ExcelGraphPicture> functionGraphPictures = new LinkedHashMap<String, ExcelGraphPicture>();
    	Map<String, ExcelGraphPicture> contentionGraphPictures = new LinkedHashMap<String, ExcelGraphPicture>();

    	linePos = displayActions(sheet, functionGraphPictures, contentionGraphPictures, linePos);
    	
    	createColumnGroups(sheet);
    	createFilters(sheet, linePos, rowPos);
    	
    	displayGraphPictures(sheet, functionGraphPictures, contentionGraphPictures, linePos, rowPos);
    	
    	close(this.sheetCfg);
	}
	
	private void createFilters(Sheet sheet, int linePos, int rowPos) {
		// graph offset
		int graphOffset = this.sheetCfg.isFunctionGraphDisplayed()? 1 : 0;
		graphOffset = this.sheetCfg.isContentionGraphDisplayed()? graphOffset + 1 : graphOffset;
    	sheet.setAutoFilter(new CellRangeAddress(1, linePos-1, 6+graphOffset, rowPos-1));
	}

	private int displayGraphPictures(Sheet sheet, Map<String, ExcelGraphPicture> functionGraphPictures, Map<String, ExcelGraphPicture> contentionGraphPictures, int linePos, int rowPos) {
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
			
			for (Map.Entry<String,ExcelGraphPicture> entry : functionGraphPictures.entrySet()){
				lines.put(entry.getKey(), linePos);
				linePos = addGraphPicture(sheet, this.sheetCfg.getName(), entry.getValue(), linePos, rowPos, ACTION_GRAPH);
				candidate = entry.getValue();
			}
			
			if (candidate != null){
				contentionRowPos += candidate.getExcelWidth() + 4;  // 2 for the borders, 2 for the separation
			}
			
			// clean pictures
			clearGraphPictures(functionGraphPictures.values());
		}
		
		// display on the right
		if (this.sheetCfg.isContentionGraphDisplayed()){
			for (Map.Entry<String,ExcelGraphPicture> entry : contentionGraphPictures.entrySet()){
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

	private int displayActions(Sheet sheet, Map<String, ExcelGraphPicture> functionGraphPictures, Map<String, ExcelGraphPicture> contentionGraphPictures, int linePos) throws JzrException {
    	int prevSectionLine = NOT_EXIST;
    	int nextSectionLine;
    	int parentSectionLine = NOT_EXIST;
    	int lastChildSectionLine;
		
    	actionGraphRoot = buildActionGraph(session);
    	
    	Set<Map.Entry<String, ActionGraphSection>> rootSections = actionGraphRoot.getActionGraphSectionRoots();
    	this.itemsCount = rootSections.size();

    	buildGraphPictures(functionGraphPictures, contentionGraphPictures, rootSections);
    	
    	List<Integer> sectionLineSizes = new ArrayList<>(rootSections.size());
    	for (Map.Entry<String, ActionGraphSection> rootSection : rootSections){
			int lineSize = computeSectionLineSize(rootSection.getValue());
			sectionLineSizes.add(lineSize);
    	}

    	int i=1; // ignore the first entry as we're interested by the next one
    	for (Map.Entry<String, ActionGraphSection> rootSection : rootSections){
    		// get action, key and related graph section 
    		ActionGraphSection section = rootSection.getValue();

    		section.sortChildren();
    		
    		// Next arrow
			if (i == rootSections.size())
				nextSectionLine = NOT_EXIST;
			else
				nextSectionLine = linePos + sectionLineSizes.get(i-1) + sectionLineSizes.get(i) -1;  // should be equal to  linePos after processGraph call 
			i++;

    		// Down arrow
			lastChildSectionLine = getLastChildSectionLine(section, linePos);
        	
    		linePos = processGraph(sheet, linePos, section, section.getStackCount(), 1, prevSectionLine, nextSectionLine, parentSectionLine, lastChildSectionLine, functionGraphPictures, contentionGraphPictures);
    		
			// Prev arrow
    		prevSectionLine = linePos-1; 
    	}
		return linePos;
	}

	private void buildGraphPictures(Map<String, ExcelGraphPicture> functionGraphPictures, Map<String, ExcelGraphPicture> contentionGraphPictures, Set<Entry<String, ActionGraphSection>> rootSections) throws JzrException {
		if (!this.sheetCfg.isFunctionGraphDisplayed() && !this.sheetCfg.isContentionGraphDisplayed())
			return;
		
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
		
		// sort action graph sections per number of operation/functions, from greatest to smallest
		List<ActionGraphSection> sortedActionGraphSections = new ArrayList<>();
    	for (Entry<String,ActionGraphSection> actionGraphSection : rootSections){
    		sortedActionGraphSections.add(actionGraphSection.getValue());
    	}
    	sortedActionGraphSections.sort(new ActionGraphSectionComparator.TreeFunctionAndOperationSizeComparator());		
		
		// generate the graphs, in the number of operation/functions order
		Map<ActionGraphSection, ExcelGraphPicture> generatedFunctionGraphPictures = new HashMap<>();	// order is not important
		Map<ActionGraphSection, ExcelGraphPicture> generatedContentionGraphPictures = new HashMap<>();	// order is not important
    	for (ActionGraphSection rootSection : sortedActionGraphSections){
    		// generate the function type graph
    		buildFunctionGraphPicture(rootSection, generatedFunctionGraphPictures, functionGraphGenerator);
			// generate the contention type graph
			buildContentionGraphPicture(rootSection, generatedContentionGraphPictures, contentionGraphGenerator);
    	}
    	
    	// order the generated pictures in the action display order, to get the same display order at picture level
    	for (Map.Entry<String, ActionGraphSection> entry : rootSections){
    		ActionGraphSection rootSection = entry.getValue();
    		ExcelGraphPicture functionPicturePath = generatedFunctionGraphPictures.get(rootSection);
    		if (functionPicturePath!= null)
    			functionGraphPictures.put(
    					rootSection.getExecutor() + ActionGraphRoot.EXECUTOR_ACTION_SEPARATOR + rootSection.getFunction(),  
    					functionPicturePath);
			// add the contention type graph picture
			ExcelGraphPicture contentionPicturePath = generatedContentionGraphPictures.get(rootSection);
			if (contentionPicturePath != null)
				contentionGraphPictures.put(
						rootSection.getExecutor() + ActionGraphRoot.EXECUTOR_ACTION_SEPARATOR + rootSection.getFunction(), 
						contentionPicturePath);
    	}
	}

	private void buildContentionGraphPicture(ActionGraphSection rootSection, Map<ActionGraphSection, ExcelGraphPicture> generatedContentionGraphPictures, ActionContentionTypeGraphGenerator contentionGraphGenerator) {
		if (!this.sheetCfg.isContentionGraphDisplayed() || contentionGraphGenerator == null)
			return;
		
		ExcelGraphPicture contentionPicturePath = contentionGraphGenerator.generateGraphPicture(
				rootSection.getFunction(),
				rootSection.getExecutor(),
				rootSection);
		
		if (contentionPicturePath!= null){
			generatedContentionGraphPictures.put(rootSection, contentionPicturePath);
			this.graphItemsCount++;
		}
	}

	private void buildFunctionGraphPicture(ActionGraphSection rootSection, Map<ActionGraphSection, ExcelGraphPicture> generatedGraphPictures, ActionGraphGenerator functionGraphGenerator) {
		if (!this.sheetCfg.isFunctionGraphDisplayed() || functionGraphGenerator == null)
			return;		
		
		ExcelGraphPicture picturePath = functionGraphGenerator.generateGraphPicture(
				rootSection.getFunction(),
				rootSection.getExecutor(),
				rootSection);
		
		if (picturePath!= null){
			generatedGraphPictures.put(rootSection, picturePath);
			this.graphItemsCount++;
		}
	}

	private int displayHeader(Sheet sheet, int linePos, int rowPos) {
    	// Header
    	Row row = sheet.createRow(linePos++);
    	
    	sheet.setColumnWidth(rowPos, 3*256);
    	addHeaderCell(row, rowPos++, "D");
    	
    	sheet.setColumnWidth(rowPos, 6*256);
    	addHeaderCell(row, rowPos++, "Level");

    	sheet.setColumnWidth(rowPos, 4*256);
    	addHeaderCell(row, rowPos++, "Navigation", STYLE_THEME_HEADER_ALIGN_LEFT);
    	
    	sheet.setColumnWidth(rowPos, 2*256);
    	addHeaderCell(row, rowPos++, "");

    	sheet.setColumnWidth(rowPos, 2*256);
    	addHeaderCell(row, rowPos++, "");

    	sheet.setColumnWidth(rowPos, 2*256);
    	addHeaderCell(row, rowPos++, "");
    	
    	if (this.sheetCfg.isFunctionGraphDisplayed()){
    		sheet.setColumnWidth(rowPos, 4*256);
    		addHeaderCell(row, rowPos++, "Graph", STYLE_THEME_HEADER_VERY_SMALL_ROTATED);
    	}
    	
    	if (this.sheetCfg.isContentionGraphDisplayed()){
    		sheet.setColumnWidth(rowPos, 4*256);
    		addHeaderCell(row, rowPos++, "Cont", STYLE_THEME_HEADER_VERY_SMALL_ROTATED);
    	}
        
    	sheet.setColumnWidth(rowPos, 45*256);
    	addHeaderCell(row, rowPos++, "Executor");
        
    	sheet.setColumnWidth(rowPos, 45*256);
    	addHeaderCell(row, rowPos++, "Action (principal)");
    	
    	sheet.setColumnWidth(rowPos, 12*256);
    	addHeaderCell(row, rowPos++, "Count");

    	sheet.setColumnWidth(rowPos, 12*256);
    	addHeaderCell(row, rowPos++, "Global %");
    	
    	sheet.setColumnWidth(rowPos, 12*256);
    	addHeaderCell(row, rowPos++, "Action %");
    	
    	if (session.isCPUInfoAvailable()){
        	sheet.setColumnWidth(rowPos, 12*256);
        	addHeaderCell(row, rowPos++, "CPU usage");
        	sheet.setColumnWidth(rowPos, 12*256);
        	addHeaderCell(row, rowPos++, 
        			concatTextAndSymbol((XSSFWorkbook) this.workbook, "CPU ", "s") // standard deviation symbol 
        			);
    	}
    	
    	if (session.isMemoryInfoAvailable()){
        	sheet.setColumnWidth(rowPos, 12*256);
        	addHeaderCell(row, rowPos++, "Mem used");
        	sheet.setColumnWidth(rowPos, 12*256);
        	addHeaderCell(row, rowPos++,
        			concatTextAndSymbol((XSSFWorkbook) this.workbook, "Mem ", "s") // standard deviation symbol
        			);
    	}
    	
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
    	
		return rowPos;
	}

	@Override
	protected Logger getLogger(){
		return logger;
	}	

	private void createColumnGroups(Sheet sheet) {
		int columnOffset = 0;
		
		int graphOffset = this.sheetCfg.isFunctionGraphDisplayed()? 1 : 0;
		graphOffset = this.sheetCfg.isContentionGraphDisplayed()? graphOffset + 1 : graphOffset;
		columnOffset += graphOffset;
		
		if (session.isCPUInfoAvailable()){
			// CPU info group
			createColumnGroup(sheet, ALL_GROUP_START_INDEX + columnOffset, ALL_GROUP_START_INDEX + columnOffset, true);
			columnOffset += 2;
		}
		
		if (session.isMemoryInfoAvailable()){
			// Memory info group
			createColumnGroup(sheet, ALL_GROUP_START_INDEX + columnOffset, ALL_GROUP_START_INDEX + columnOffset, true);
			columnOffset +=2;
		}
		
		// All function/operation group
		createColumnGroup(sheet, ALL_GROUP_START_INDEX + columnOffset, ALL_GROUP_START_INDEX + 2 + columnOffset, true);
	}

	private int processGraph(Sheet sheet, int linePos, ActionGraphSection section, int actionStackCount, int level, 
			int prevSectionLine, int nextSectionLine, int parentSectionLine, int lastChildSectionLine, 
			Map<String, ExcelGraphPicture> functionGraphPictures, Map<String, ExcelGraphPicture> contentionGraphPictures) {
		int localLevel = level;
		int firstLine = linePos;
		int lineSize;
		int childPrevSectionLine = NOT_EXIST;
		int childNextSectionLine = NOT_EXIST;
		int childParentSectionLine;
		int childLastChildSectionLine;
		
		// display sub sections first
		if (section.hasChildren()){
			Collection<ActionGraphSection> children = section.getChildren();
			List<Integer> sectionLineSizes = new ArrayList<>(children.size());
			level++;
			
			for (ActionGraphSection child : children){
				lineSize = computeSectionLineSize(child);
				sectionLineSizes.add(lineSize);
			}
			
			// Up arrow : as parent, calculate current position for our child
			childParentSectionLine = linePos + computeSectionLineSize(section) - 1;
			
			int i = 1; // ignore the first entry as we're interested by the next one
			for (ActionGraphSection child : children){
				// Down arrow : calculate position of last child
				childLastChildSectionLine = getLastChildSectionLine(child, linePos);
				
				// Next arrow
				if (i == children.size())
					childNextSectionLine = NOT_EXIST;
				else
					childNextSectionLine = linePos + sectionLineSizes.get(i-1) + sectionLineSizes.get(i) -1;  // should be equal to  linePos after processGraph call 
				i++;
					
				linePos = processGraph(sheet, linePos, child, actionStackCount, level, 
						childPrevSectionLine, childNextSectionLine, childParentSectionLine, childLastChildSectionLine, 
						functionGraphPictures, contentionGraphPictures);
				
				// Prev arrow
				childPrevSectionLine = linePos-1;
			}
		}

		linePos = displaySection(sheet, firstLine, linePos, section, actionStackCount, localLevel, 
				prevSectionLine, nextSectionLine, parentSectionLine, lastChildSectionLine, 
				functionGraphPictures, contentionGraphPictures);
		
		return linePos;
	}

	private int computeSectionLineSize(ActionGraphSection section) {
		int lineSize = 0;
		
		if (section.hasChildren()){
			for (ActionGraphSection child : section.getChildren()){
				lineSize += computeSectionLineSize(child);
			}
		}
		
		lineSize += section.getSize() + 1;
		
		return lineSize;
	}

	private int getLastChildSectionLine(ActionGraphSection section, int linePos){
		if (section.hasChildren()){
			List<ActionGraphSection> children = (List<ActionGraphSection>)section.getChildren();
			// refer to the first child
			int lastChildSectionSize = computeSectionLineSize(children.get(0));
			return linePos + lastChildSectionSize -1;
		}
		else
			return NOT_EXIST;
	}
	
	protected int displaySection(Sheet sheet, int firstLine, int linePos, ActionGraphSection section, int actionStackCount, int level, 
			int prevSectionLine, int nextSectionLine, int parentSectionLine, int lastChildSectionLine, 
			Map<String, ExcelGraphPicture> functionGraphPictures, Map<String, ExcelGraphPicture> contentionGraphPictures) {

		// Pre-grouping doesn't work, even if cells are already created
//		int rowEnd = getActionSectionRowEnd(section);
//		sheet.groupRow(firstLine, rowEnd-1);
//		sheet.setRowGroupCollapsed(firstLine, true);
		
		// display code lines first
		Iterator<String> functionTag = section.getSourceFunctionTags().iterator();
		Iterator<String> operationTag = section.getSourceOperationTags().iterator();
		Iterator<String> contentionTypeTag = section.getSourceContentionTypeTags().iterator();
		for (String codeLine : section.getCodeLines())
			displayCodeLines(sheet, linePos++, codeLine, functionTag.next(), operationTag.next(), contentionTypeTag.next());
		
		// display action line
		displayActionLine(sheet, linePos++, level, section, actionStackCount, 
				prevSectionLine, nextSectionLine, parentSectionLine, lastChildSectionLine, 
				functionGraphPictures, contentionGraphPictures);

		// group it (Excel support max 8 levels of grouping)
		if (level<8){
			sheet.groupRow(firstLine, linePos-2);
			sheet.setRowGroupCollapsed(firstLine, true);
		}
		
		return linePos;
	}

	protected void displayCodeLines(Sheet sheet, int pos, String rawCodeLine, String function, String operation, String contentionType) {
		int rowPos = 0;
		Row row = sheet.createRow(pos);
		String codeLine = AnalyzerHelper.stripCodeLine(rawCodeLine, javaModuleSupport && this.sheetCfg.isJavaModuleStripped());

		addEmptyCell(row, rowPos++);
		addEmptyCell(row, rowPos++);
		addEmptyCell(row, rowPos++);
		addEmptyCell(row, rowPos++);
		addEmptyCell(row, rowPos++);
		addEmptyCell(row, rowPos++);
		
		if(this.sheetCfg.isFunctionGraphDisplayed())
			addEmptyCell(row, rowPos++);  // graph picture

		if(this.sheetCfg.isContentionGraphDisplayed())
			addEmptyCell(row, rowPos++);  // graph picture
		
		if (function != null){
			addCell(row, rowPos++, codeLine, STYLE_CELL, this.sheetCfg.getFunctionColor());
			addEmptyCell(row, rowPos++, STYLE_CELL, this.sheetCfg.getFunctionColor()); // action
			addEmptyCell(row, rowPos++, STYLE_CELL, this.sheetCfg.getFunctionColor()); // count
			addEmptyCell(row, rowPos++, STYLE_CELL, this.sheetCfg.getFunctionColor()); // global %
			addEmptyCell(row, rowPos++, STYLE_CELL, this.sheetCfg.getFunctionColor()); // action%
			
	    	if (session.isCPUInfoAvailable()){
	    		addEmptyCell(row, rowPos++, STYLE_CELL, this.sheetCfg.getFunctionColor());
	    		addEmptyCell(row, rowPos++, STYLE_CELL, this.sheetCfg.getFunctionColor());
	    	}
	    	
	    	if (session.isMemoryInfoAvailable()){
	    		addEmptyCell(row, rowPos++, STYLE_CELL, this.sheetCfg.getFunctionColor());
	    		addEmptyCell(row, rowPos++, STYLE_CELL, this.sheetCfg.getFunctionColor());
	    	}
	    	
			addEmptyCell(row, rowPos++, STYLE_CELL, this.sheetCfg.getFunctionColor());
			addEmptyCell(row, rowPos++, STYLE_CELL, this.sheetCfg.getFunctionColor());
			addCell(row, rowPos++, function, STYLE_CELL, this.sheetCfg.getFunctionColor());
			addEmptyCell(row, rowPos++, STYLE_CELL, this.sheetCfg.getFunctionColor());
			addEmptyCell(row, rowPos++, STYLE_CELL, this.sheetCfg.getFunctionColor());
		}
		else if (operation != null){
			addCell(row, rowPos++, codeLine, STYLE_CELL, this.sheetCfg.getOperationColor());
			addEmptyCell(row, rowPos++, STYLE_CELL, this.sheetCfg.getOperationColor()); // action
			addEmptyCell(row, rowPos++, STYLE_CELL, this.sheetCfg.getOperationColor()); // count
			addEmptyCell(row, rowPos++, STYLE_CELL, this.sheetCfg.getOperationColor()); // global %
			addEmptyCell(row, rowPos++, STYLE_CELL, this.sheetCfg.getOperationColor()); // action%
			
	    	if (session.isCPUInfoAvailable()){
	    		addEmptyCell(row, rowPos++, STYLE_CELL, this.sheetCfg.getOperationColor());
	    		addEmptyCell(row, rowPos++, STYLE_CELL, this.sheetCfg.getOperationColor());
	    	}
	    	
	    	if (session.isMemoryInfoAvailable()){
	    		addEmptyCell(row, rowPos++, STYLE_CELL, this.sheetCfg.getOperationColor());
	    		addEmptyCell(row, rowPos++, STYLE_CELL, this.sheetCfg.getOperationColor());
	    	}
	    	
	    	addCell(row, rowPos++, operation, STYLE_CELL, this.sheetCfg.getOperationColor());
	    	if (contentionType != null)
	    		addCell(row, rowPos++, contentionType, STYLE_CELL, this.sheetCfg.getOperationColor());
	    	else
	    		addEmptyCell(row, rowPos++, STYLE_CELL, this.sheetCfg.getOperationColor());
			addEmptyCell(row, rowPos++, STYLE_CELL, this.sheetCfg.getOperationColor());
			addEmptyCell(row, rowPos++, STYLE_CELL, this.sheetCfg.getOperationColor());
			addEmptyCell(row, rowPos++, STYLE_CELL, this.sheetCfg.getOperationColor());
		}
		else{
			addCell(row, rowPos++, codeLine);
			addEmptyCell(row, rowPos++); // action
			addEmptyCell(row, rowPos++); // count
			addEmptyCell(row, rowPos++); // global %
			addEmptyCell(row, rowPos++); // action%
			
	    	if (session.isCPUInfoAvailable()){
	    		addEmptyCell(row, rowPos++);
	    		addEmptyCell(row, rowPos++);
	    	}
	    	
	    	if (session.isMemoryInfoAvailable()){
	    		addEmptyCell(row, rowPos++);
	    		addEmptyCell(row, rowPos++);
	    	}
	    	
			addEmptyCell(row, rowPos++);	    	
			addEmptyCell(row, rowPos++);
			addEmptyCell(row, rowPos++);
			addEmptyCell(row, rowPos++);
			addEmptyCell(row, rowPos++);
		}
	}

	protected void displayActionLine(Sheet sheet, int pos, int level, ActionGraphSection section, int actionStackCount, 
			int prevSectionLine, int nextSectionLine, int parentSectionLine, int lastChildSectionLine, 
			Map<String, ExcelGraphPicture> functionGraphPictures, Map<String, ExcelGraphPicture> contentionGraphPictures) {
		int rowPos = 0;
		Row row = sheet.createRow(pos);
		
		// -----------------------------
		// Navigation section
		// -----------------------------
		rowPos = displayActionNavigation(section, row, level, rowPos, prevSectionLine, nextSectionLine, parentSectionLine, lastChildSectionLine);

		// -----------------------------
		// Function graph link
		// -----------------------------
		if (this.sheetCfg.isFunctionGraphDisplayed())
			rowPos = displayGraphPictureLink(functionGraphPictures, section, row, level, rowPos);

		// -----------------------------
		// Contention graph link
		// -----------------------------
		if (this.sheetCfg.isContentionGraphDisplayed())
			rowPos = displayGraphPictureLink(contentionGraphPictures, section, row, level, rowPos);
		
		addCell(row, rowPos++, section.getExecutor(), getCellStyleShadedReference(level));
		addCell(row, rowPos++, getFunction(section), getCellStyleShadedReference(level));
		addCell(row, rowPos++, section.getStackCount(), getCellStyleShadedReference(level));
		addCell(row, rowPos++, FormulaHelper.percentNull(section.getStackCount(), totalActionStacks), getCellStyleShadedReference(level)); // global %
		if (level == 1)
			addEmptyCell(row, rowPos++, getCellStyleShadedReference(level));
		else
			addCell(row, rowPos++, FormulaHelper.percentNull(section.getStackCount(), actionStackCount), getCellStyleShadedReference(level)); // action %
		
		// -----------------------------
		// CPU
		// -----------------------------
		rowPos = displayActionCPUInfo(section, row, level, rowPos);
    	
		// -----------------------------
		// Memory section
		// -----------------------------
		rowPos = displayActionMemoryInfo(section, row, level, rowPos);
		
		addCell(row, rowPos++, getOperation(section), getCellStyleShadedReference(level));
		addCell(row, rowPos++, getContentionType(section), getCellStyleShadedReference(level));
		addCell(row, rowPos++, getAllActions(section), getCellStyleShadedReference(level));
		addCell(row, rowPos++, getAllOperations(section), getCellStyleShadedReference(level));
		addCell(row, rowPos++, getAllContentionTypes(section), getCellStyleShadedReference(level));
	}

	private int displayGraphPictureLink(Map<String, ExcelGraphPicture> graphPictures, ActionGraphSection section, Row row, int level, int rowPos) {
		// cell link will be created at later stage, so always create empty cell to start with
		Cell cell = addEmptyCell(row, rowPos++, getShadedStyle(level));
		
		if (level == 1) {
			String key = section.getExecutor() + ActionGraphRoot.EXECUTOR_ACTION_SEPARATOR + section.getFunction();
			ExcelGraphPicture picture = graphPictures.get(key);
			if (picture != null)
				// update the picture with the parent cell that will link it
				picture.setParentLinkCell(cell);
		}
		
		return rowPos;
	}

	private int displayActionMemoryInfo(ActionGraphSection section, Row row, int level, int rowPos) {
    	if (session.isMemoryInfoAvailable()){
    		// Allocated memory average    		
    		long memoryUsage = section.getAllocatedMemoryAverage();
    		if (memoryUsage != -1)
    			addCell(row, rowPos++, convertToMb(memoryUsage), getCellStyleShadedReference(level));
    		else
    			addCell(row, rowPos++, "NA", getCellStyleShadedReference(level));
    		
    		// Allocated memory standard deviation
    		double memoryUsageStdDeviation = section.getAllocatedMemoryStdDeviation();
    		if (Double.doubleToRawLongBits(memoryUsageStdDeviation) != DOUBLE_TO_LONG_NA)
    			addCell(row, rowPos++, convertToMb(memoryUsageStdDeviation), getCellStyleShadedReference(level));
    		else
    			addCell(row, rowPos++, "NA", getCellStyleShadedReference(level));
    	}

		return rowPos;
	}

	private int displayActionCPUInfo(ActionGraphSection section, Row row, int level, int rowPos) {
    	if (session.isCPUInfoAvailable()){
    		// CPU usage average
    		int cpuUsage = section.getCpuUsageAverage();
    		if (cpuUsage != -1)
    			addCell(row, rowPos++, cpuUsage, getCellStyleShadedReference(level));
    		else
    			addCell(row, rowPos++, "NA", getCellStyleShadedReference(level));

    		// CPU usage standard deviation
    		double cpuUsageStdDeviation = section.getCpuUsageStdDeviation();
    		if (Double.doubleToRawLongBits(cpuUsageStdDeviation) != DOUBLE_TO_LONG_NA)
    			addCell(row, rowPos++, cpuUsageStdDeviation, getCellStyleShadedReference(level));
    		else
    			addCell(row, rowPos++, "NA", getCellStyleShadedReference(level));
    	}
    	
		return rowPos;
	}

	private int displayActionNavigation(ActionGraphSection section, Row row, int level, int rowPos, int prevSectionLine, int nextSectionLine, int parentSectionLine, int lastChildSectionLine) {
		int maxLevel = section.getMaxDepth()+level-1;
		
		if (section.getMaxDepth() == 1)
			addEmptyCell(row, rowPos++, getCellStyleShadedReference(section.getMaxDepth()+level-1));
		else 
			addCell(row, rowPos++, maxLevel, getCellStyleShadedReference(maxLevel), getConstantFont(CellFonts.FONT_8)); // display max depth
		addCell(row, rowPos++, level, getCellStyleShadedReference(level));
		
		int actionGraphOffset = this.sheetCfg.isFunctionGraphDisplayed() ? 1 : 0;
		actionGraphOffset = this.sheetCfg.isContentionGraphDisplayed()? actionGraphOffset + 1 : actionGraphOffset;
		int columnTarget = 1 + actionGraphOffset;  // 1 = level
		
		// previous arrow
		if (prevSectionLine != NOT_EXIST){
			CellReference cellref = new CellReference(prevSectionLine, columnTarget, true, true);
			addCell(row, rowPos++, CellText.FONT_SYMBOL_LEFT_ARROW, cellref.formatAsString(), getCellStyleShadedReference(level), getConstantFont(FONT_SYMBOL_9));
		}
		else
			addEmptyCell(row, rowPos++, getCellStyleShadedReference(level));
		
		// parent arrow
		if (level != 1){
			CellReference cellref = new CellReference(parentSectionLine, columnTarget, true, true);
			addCell(row, rowPos++, CellText.FONT_SYMBOL_UP_ARROW, cellref.formatAsString(), getCellStyleShadedReference(level), getConstantFont(FONT_SYMBOL_9));
		}
		else
			addEmptyCell(row, rowPos++, getCellStyleShadedReference(level));
		
		// child arrow
		if (section.hasChildren()){
			CellReference cellref = new CellReference(lastChildSectionLine, columnTarget, true, true);
			addCell(row, rowPos++, CellText.FONT_SYMBOL_DOWN_ARROW, cellref.formatAsString(), getCellStyleShadedReference(level), getConstantFont(FONT_SYMBOL_9));
		}
		else
			addEmptyCell(row, rowPos++, getCellStyleShadedReference(level));
		
		// next arrow
		if (nextSectionLine != NOT_EXIST){
			CellReference cellref = new CellReference(nextSectionLine, columnTarget, true, true);
			addCell(row, rowPos++, CellText.FONT_SYMBOL_RIGHT_ARROW, cellref.formatAsString(), getCellStyleShadedReference(level), getConstantFont(FONT_SYMBOL_9));
		}
		else
			addEmptyCell(row, rowPos++, getCellStyleShadedReference(level));

		return rowPos;
	}

	private String getContentionType(ActionGraphSection section) {
		String contentionType = section.getContentionType();
		return (contentionType != null) ? contentionType : "";
	}
	
	private String getOperation(ActionGraphSection section) {
		String operation = section.getOperation();
		return (operation != null) ? operation : "";
	}

	private String getFunction(ActionGraphSection section) {
		String function = section.getFunction();
		return (function != null) ? function : "";
	}

	private String getAllContentionTypes(ActionGraphSection section) {
		Set<String> contentionTypes = section.getAllContentionTypes();
		return mergeLabels(contentionTypes);
	}
	
	private String getAllOperations(ActionGraphSection section) {
		Set<String> operations = section.getAllOperations();
		return mergeLabels(operations);
	}
	
	private String getAllActions(ActionGraphSection section) {
		Set<String> actions = section.getAllFunctions();
		return mergeLabels(actions);
	}	
	
	private String mergeLabels(Set<String> labels) {
		StringBuilder result = new StringBuilder();

		boolean start = true;
		for (String label : labels){
			result.append(start? "" : " - ");
			result.append(label);
			start = false;
		}
		
		return result.toString();
	}		

	private String getCellStyleShadedReference(int level) {
		return getShadedStyle(level);
	}

	private ActionGraphRoot buildActionGraph(JzrSession session) {
		if (this.actionGraphRoot != null)
			return this.actionGraphRoot;
		
		boolean includeStates = 
				this.sheetCfg.isFunctionGraphDisplayed()
				&& (this.sheetCfg.getFunctionGraphCfg().getMode() == ConfigGraph.GENERATION_MODE.TREE 
					|| this.sheetCfg.getFunctionGraphCfg().getMode() == ConfigGraph.GENERATION_MODE.TREE_MERGE)
				&& this.sheetCfg.getFunctionGraphCfg().isThreadStateDisplayed();
			
		actionGraphRoot = new ActionGraphRoot(
				this.sheetCfg.isAtbiIncluded(),
				includeStates
				);
		
		for (ThreadDump dump : session.getDumps()){
			Date timestamp = dump.getTimestamp();
			Set<ThreadAction> actions = session.getActionHistory().get(timestamp);
			
			for(ThreadAction action : actions){
				actionGraphRoot.addAction(action);
				totalActionStacks += action.size();
			}
		}
		
		return actionGraphRoot;
	}
}
