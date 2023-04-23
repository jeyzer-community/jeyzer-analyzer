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



import static org.jeyzer.analyzer.math.FormulaHelper.DOUBLE_TO_LONG_NA;
import static org.jeyzer.analyzer.math.FormulaHelper.convertToMb;
import static org.jeyzer.analyzer.output.graph.picture.ExcelGraphPicture.GraphType.ACTION_GRAPH;
import static org.jeyzer.analyzer.output.graph.picture.ExcelGraphPicture.GraphType.CONTENTION_GRAPH;
import static org.jeyzer.analyzer.output.poi.CellText.concatTextAndSymbol;
import static org.jeyzer.analyzer.output.poi.style.CellFonts.FONT_SYMBOL_9;
import static org.jeyzer.analyzer.output.poi.style.DefaultCellStyles.*;
import static org.jeyzer.analyzer.output.poi.theme.AbstractTheme.STYLE_THEME_HEADER_ALIGN_LEFT;
import static org.jeyzer.analyzer.output.poi.theme.AbstractTheme.STYLE_THEME_HEADER_VERY_SMALL_ROTATED;
import static org.jeyzer.analyzer.output.poi.theme.AbstractTheme.STYLE_THEME_TOP_BAR_JEYZER_TITLE;





import java.util.AbstractMap;
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
import org.jeyzer.analyzer.config.report.ConfigActionDistinctProfilingSheet;
import org.jeyzer.analyzer.config.report.ConfigGraph;
import org.jeyzer.analyzer.data.ThreadAction;
import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.analyzer.data.action.ActionGraphRootSection;
import org.jeyzer.analyzer.data.action.ActionGraphSection;
import org.jeyzer.analyzer.data.action.ActionGraphSectionComparator;
import org.jeyzer.analyzer.data.stack.ThreadStack;
import org.jeyzer.analyzer.error.JzrException;
import org.jeyzer.analyzer.math.FormulaHelper;
import org.jeyzer.analyzer.output.graph.picture.ActionContentionTypeGraphGenerator;
import org.jeyzer.analyzer.output.graph.picture.ActionGraphGenerator;
import org.jeyzer.analyzer.output.graph.picture.ExcelGraphPicture;
import org.jeyzer.analyzer.output.poi.CellRefRepository;
import org.jeyzer.analyzer.output.poi.CellText;
import org.jeyzer.analyzer.output.poi.context.DisplayContext;
import org.jeyzer.analyzer.output.poi.style.CellFonts;
import org.jeyzer.analyzer.session.JzrSession;
import org.jeyzer.analyzer.util.AnalyzerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActionDistinctProfilingSheet extends JeyzerSheet {
	
	private static final Logger logger = LoggerFactory.getLogger(ActionDistinctProfilingSheet.class);
	
	private static final int NOT_EXIST = -1;
	
	private ConfigActionDistinctProfilingSheet sheetCfg;
	private boolean javaModuleSupport;
	
	public ActionDistinctProfilingSheet(ConfigActionDistinctProfilingSheet config, JzrSession session, DisplayContext displayContext) {
		super(session, displayContext);
		this.sheetCfg = config;
		this.javaModuleSupport = session.isJavaModuleSupported();
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
    	
    	linePos = displayDistinctActions(sheet, cellRefRepository, functionGraphPictures, contentionGraphPictures, linePos);
		
    	createFilters(sheet, cellRefRepository, linePos, rowPos);
		sheet.createFreezePane(0, 2);

		linePos = displayGraphPictures(sheet, functionGraphPictures, contentionGraphPictures, linePos, rowPos);
		
		createColumnGroups(sheet, cellRefRepository);
		
    	addTopBar(sheet, rowPos);
    	addMenuLink(sheet, sheetCfg, STYLE_THEME_TOP_BAR_JEYZER_TITLE, 0, 0);
    	
    	close(this.sheetCfg);
	}
	
	private void createFilters(Sheet sheet, CellRefRepository cellRefRepository, int linePos, int rowPos) {
		int linkOffset = cellRefRepository.getActionSheetTypes().size() + 6;
		
		// graph offset
		int graphOffset = this.sheetCfg.isFunctionGraphDisplayed()? 1 : 0;
		graphOffset = this.sheetCfg.isContentionGraphDisplayed()? graphOffset + 1 : graphOffset;
		linkOffset += graphOffset;
		
		sheet.setAutoFilter(new CellRangeAddress(1, linePos-1, 1 + linkOffset, rowPos - 1));
	}

	@Override
	protected Logger getLogger(){
		return logger;
	}	

	private int displayGraphPictures(Sheet sheet, Map<ThreadAction, ExcelGraphPicture> functionGraphPictures, Map<ThreadAction, ExcelGraphPicture> contentionGraphPictures, int linePos, int rowPos) {
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
			lines = new HashMap<ThreadAction, Integer>(functionGraphPictures.size());
		
			// add each graph picture
			for (Map.Entry<ThreadAction,ExcelGraphPicture> entry : functionGraphPictures.entrySet()){
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

	private void createColumnGroups(Sheet sheet, CellRefRepository cellRefRepository) {
		int linkSize = cellRefRepository.getActionSheetTypes().size();
		int columnOffset = linkSize;
		int graphOffset = this.sheetCfg.isFunctionGraphDisplayed()? 1 : 0;
		graphOffset = this.sheetCfg.isContentionGraphDisplayed()? graphOffset + 1 : graphOffset;
		columnOffset += graphOffset;
		
		// links group
		if (linkSize>1)
			createColumnGroup(sheet, 2 + graphOffset, linkSize + graphOffset, true); // first link column is always visible
		
		// time group (time start is always displayed)
		createColumnGroup(sheet, 8 + columnOffset, 13 + columnOffset, true);

		// thread group
		createColumnGroup(sheet, 15 + columnOffset, 17 + columnOffset, true);

		if (session.isCPUInfoAvailable()){
			// CPU info group
			createColumnGroup(sheet, 22 + columnOffset, 23 + columnOffset, true);
			columnOffset += 3;
		}
		
		if (session.isMemoryInfoAvailable()){
			// Memory info group
			createColumnGroup(sheet, 22 + columnOffset, 22 + columnOffset, true);
			columnOffset += 2;
		}
		
		// All function/operation group
		createColumnGroup(sheet, 23 + columnOffset, 25 + columnOffset, true);
	}

	private int displayDistinctActions(Sheet sheet, CellRefRepository cellRefRepository, Map<ThreadAction, ExcelGraphPicture> functionGraphPictures, Map<ThreadAction, ExcelGraphPicture> contentionGraphPictures, int linePos) throws JzrException {
    	int prevSectionLine = NOT_EXIST;
    	int nextSectionLine;
    	int parentSectionLine = NOT_EXIST;
    	int lastChildSectionLine;
		
		List<Map.Entry<ThreadAction, ActionGraphSection>> actionGraphSections = buildActionGraphSectionList();
		this.itemsCount = actionGraphSections.size();
		
    	List<Integer> sectionLineSizes = new ArrayList<>(actionGraphSections.size());
    	for (Map.Entry<ThreadAction, ActionGraphSection> rootSection : actionGraphSections){
			int lineSize = computeSectionLineSize(rootSection.getValue());
			sectionLineSizes.add(lineSize);
    	}

		buildGraphPictures(functionGraphPictures, contentionGraphPictures, actionGraphSections);
    	
    	int i=1; // ignore the first entry as we're interested by the next one
    	for (Map.Entry<ThreadAction, ActionGraphSection> rootSection : actionGraphSections){
    		// get action, key and related graph section 
    		ActionGraphSection section = rootSection.getValue();
    		
    		section.sortChildren();
    		
    		// Next arrow
			if (i == actionGraphSections.size())
				nextSectionLine = NOT_EXIST;
			else
				nextSectionLine = linePos + sectionLineSizes.get(i-1) + sectionLineSizes.get(i) -1;  // should be equal to  linePos after processGraph call 
			i++;

    		// Down arrow
			lastChildSectionLine = getLastChildSectionLine(section, linePos);
        	
    		linePos = processGraph(sheet, cellRefRepository, linePos, rootSection.getKey(), section, section.getStackCount(), 1, prevSectionLine, nextSectionLine, parentSectionLine, lastChildSectionLine, functionGraphPictures, contentionGraphPictures);
    		
			// Prev arrow
    		prevSectionLine = linePos-1; 
    	}
		
		return linePos;
	}
	
	private void buildGraphPictures(Map<ThreadAction, ExcelGraphPicture> graphPictures, Map<ThreadAction, ExcelGraphPicture> contentionGraphPictures, List<Entry<ThreadAction,ActionGraphSection>> actionGraphSections) throws JzrException {
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
		Map<ActionGraphSection, ExcelGraphPicture> generatedContentionGraphPictures = new HashMap<>();	// order is not
		
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
    			graphPictures.put(rootSection.getKey(), picturePath);
    		
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

	private int processGraph(Sheet sheet, CellRefRepository cellRefRepository, int linePos, ThreadAction threadAction, ActionGraphSection section, int actionStackCount, int level, int prevSectionLine, int nextSectionLine, int parentSectionLine, int lastChildSectionLine, Map<ThreadAction, ExcelGraphPicture> functionGraphPictures, Map<ThreadAction, ExcelGraphPicture> contentionGraphPictures) {
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
					
				linePos = processGraph(sheet, cellRefRepository, linePos, threadAction, child, actionStackCount, level, childPrevSectionLine, childNextSectionLine, childParentSectionLine, childLastChildSectionLine, functionGraphPictures, contentionGraphPictures);
				
				// Prev arrow
				childPrevSectionLine = linePos-1;
			}
		}

		linePos = displaySection(sheet, cellRefRepository, firstLine, linePos, threadAction, section, actionStackCount, localLevel, prevSectionLine, nextSectionLine, parentSectionLine, lastChildSectionLine, functionGraphPictures, contentionGraphPictures);
		
		return linePos;
	}
	
	private int displaySection(Sheet sheet, CellRefRepository cellRefRepository, int firstLine, int linePos, ThreadAction threadAction, ActionGraphSection section, int actionStackCount, int level, int prevSectionLine, int nextSectionLine, int parentSectionLine, int lastChildSectionLine, Map<ThreadAction, ExcelGraphPicture> functionGraphPictures, Map<ThreadAction, ExcelGraphPicture> contentionGraphPictures) {

		// Pre-grouping doesn't work, even if cells are already created
//		int rowEnd = getActionSectionRowEnd(section);
//		sheet.groupRow(firstLine, rowEnd-1);
//		sheet.setRowGroupCollapsed(firstLine, true);
		
		// display code lines first
		Iterator<String> functionTag = section.getSourceFunctionTags().iterator();
		Iterator<String> operationTag = section.getSourceOperationTags().iterator();
		Iterator<String> ContentionTypeTag = section.getSourceContentionTypeTags().iterator();
		for (String codeLine : section.getCodeLines()){
			displayCodeLines(sheet, cellRefRepository, linePos++, codeLine, functionTag.next(), operationTag.next(), ContentionTypeTag.next());
		}
		
		// display action line
		displayActionLine(sheet, cellRefRepository, linePos++, level, threadAction, section, actionStackCount, prevSectionLine, nextSectionLine, parentSectionLine, lastChildSectionLine, functionGraphPictures, contentionGraphPictures);

		// group it (Excel support max 8 levels of grouping)
		if (level<8){
			sheet.groupRow(firstLine, linePos-2);
			sheet.setRowGroupCollapsed(firstLine, true);
		}
		
		return linePos;
	}
	
	private void displayActionLine(Sheet sheet, CellRefRepository cellRefRepository, int pos, int level, ThreadAction action, ActionGraphSection section, int actionStackCount, int prevSectionLine, int nextSectionLine, int parentSectionLine, int lastChildSectionLine, Map<ThreadAction, ExcelGraphPicture> functionGraphPictures, Map<ThreadAction, ExcelGraphPicture> contentionGraphPictures) {
		int rowPos = 0;
		Row row = sheet.createRow(pos);
		
		// Action id
		addCell(row, rowPos++, (long)action.getId(), getShadedStyle(level));
    	
		// -----------------------------
		// Function graph link
		// -----------------------------
		if (this.sheetCfg.isFunctionGraphDisplayed())
			rowPos = displayGraphPictureLink(functionGraphPictures, action, row, level, rowPos);
		
		// -----------------------------
		// Contention type graph link
		// -----------------------------
		if (this.sheetCfg.isContentionGraphDisplayed())
			rowPos = displayGraphPictureLink(contentionGraphPictures, action, row, level, rowPos);
		
		// -----------------------------
		// Action links
		// -----------------------------
		rowPos = displayActionLinks(cellRefRepository, action, row, level, rowPos);
		
		// -----------------------------
		// Navigation section
		// -----------------------------
		rowPos = displayActionNavigation(cellRefRepository, section, row, level, rowPos, prevSectionLine, nextSectionLine, parentSectionLine, lastChildSectionLine);
		
		// -----------------------------
		// Time section
		// -----------------------------
		rowPos = displayActionTime(action, row, level, rowPos);

		// -----------------------------
		// Thread section
		// -----------------------------
		rowPos = displayActionThreadInfo(action, section, row, level, rowPos);
		
		// -----------------------------
		// Action section
		// -----------------------------
		
		if (level == 1){
			Cell cell = addCell(row, rowPos++, getFunction(action, section, level), getShadedStyle(level));
			registerActionLink(action.getId(), this.sheetCfg.getName(), cell);
		}
		else
			addEmptyCell(row, rowPos++, getShadedStyle(level));

		addCell(row, rowPos++, section.getStackCount(), getShadedStyle(level));
		if (level == 1)
			addEmptyCell(row, rowPos++, getShadedStyle(level));
		else
			addCell(row, rowPos++, FormulaHelper.percentNull(section.getStackCount(), actionStackCount), getShadedStyle(level)); // action %

		// -----------------------------
		// CPU
		// -----------------------------
		rowPos = displayActionCPUInfo(action, section, row, level, rowPos);
    	
		// -----------------------------
		// Memory section
		// -----------------------------
		rowPos = displayActionMemoryInfo(section, row, level, rowPos);

		// -----------------------------
		// Operation / function / contention type
		// -----------------------------
		addCell(row, rowPos++, getOperation(action, section, level), getShadedStyle(level));
		addCell(row, rowPos++, getContentionType(action, section, level), getShadedStyle(level));
		addCell(row, rowPos++, getAllActions(action, section, level), getShadedStyle(level));
		addCell(row, rowPos++, getAllOperations(action, section, level), getShadedStyle(level));
		addCell(row, rowPos++, getAllContentionTypes(action, section, level), getShadedStyle(level));
	}

	private int displayGraphPictureLink(Map<ThreadAction, ExcelGraphPicture> graphPictures, ThreadAction action, Row row, int level, int rowPos) {
		// cell link will be created at later stage, so always create empty cell to start with
		Cell cell = addEmptyCell(row, rowPos++, getShadedStyle(level));
		if (level == 1) {
			ExcelGraphPicture picture = graphPictures.get(action);
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
    			addCell(row, rowPos++, convertToMb(memoryUsage), getShadedStyle(level));
    		else
    			addCell(row, rowPos++, "NA", getShadedStyle(level));
    		
    		// Allocated memory standard deviation
    		double memoryUsageStdDeviation = section.getAllocatedMemoryStdDeviation();
    		if (Double.doubleToRawLongBits(memoryUsageStdDeviation) != DOUBLE_TO_LONG_NA)
    			addCell(row, rowPos++, convertToMb(memoryUsageStdDeviation), getShadedStyle(level));
    		else
    			addCell(row, rowPos++, "NA", getShadedStyle(level));
    	}

		return rowPos;
	}

	private int displayActionCPUInfo(ThreadAction action, ActionGraphSection section, Row row, int level, int rowPos) {
    	if (session.isCPUInfoAvailable()){
    		// CPU usage average
    		int cpuUsage = section.getCpuUsageAverage();
    		if (cpuUsage != -1)
    			addCell(row, rowPos++, cpuUsage, getShadedStyle(level));
    		else
    			addCell(row, rowPos++, "NA", getShadedStyle(level));

    		// CPU usage standard deviation
    		double cpuUsageStdDeviation = section.getCpuUsageStdDeviation();
    		if (Double.doubleToRawLongBits(cpuUsageStdDeviation) != DOUBLE_TO_LONG_NA)
    			addCell(row, rowPos++, cpuUsageStdDeviation, getShadedStyle(level));
    		else
    			addCell(row, rowPos++, "NA", getShadedStyle(level));
    		
    		// CPU time
    		long cpuTime = (level == 1)? action.getCpuTime() : section.getCpuTime();
    		if (cpuTime != -1)
    			addCell(row, rowPos++, cpuTime / 1000000L, getShadedStyle(level)); // convert to ms
    		else
    			addCell(row, rowPos++, "NA", getShadedStyle(level));
    	}

		return rowPos;
	}

	private int displayActionThreadInfo(ThreadAction action, ActionGraphSection section, Row row, int level, int rowPos) {
		if (level == 1){
			addCell(row, rowPos++, section.getExecutor(), getShadedStyle(level));
			addCell(row, rowPos++, action.getThreadId(), getShadedStyle(level));
			addCell(row, rowPos++, action.getName(), getShadedStyle(level));
			addCell(row, rowPos++, action.getPriority(), getShadedStyle(level));
		}
		else{
			addEmptyCell(row, rowPos++, getShadedStyle(level));
			addEmptyCell(row, rowPos++, getShadedStyle(level));
			addEmptyCell(row, rowPos++, getShadedStyle(level));
			addEmptyCell(row, rowPos++, getShadedStyle(level));
		}
		return rowPos;
	}

	private int displayActionTime(ThreadAction action, Row row, int level, int rowPos) {
		if (level == 1){
			addCell(row, rowPos++, convertToTimeZone(action.getMinStartDate()), STYLE_CELL_DATE_LEVEL_1);
			addCell(row, rowPos++, convertToTimeZone(action.getStartDate()), STYLE_CELL_DATE_LEVEL_1);
			addCell(row, rowPos++, convertToTimeZone(action.getEndDate()), STYLE_CELL_DATE_LEVEL_1);
			addCell(row, rowPos++, convertToTimeZone(action.getMaxEndDate()), STYLE_CELL_DATE_LEVEL_1);
			addCell(row, rowPos++, action.getMinDuration(), getShadedStyle(level));
			addCell(row, rowPos++, action.getMaxDuration(), getShadedStyle(level));
			addCell(row, rowPos++, getActionMissingDumpsCount(action), getShadedStyle(level));
		}
		else{
			addEmptyCell(row, rowPos++, getShadedStyle(level));
			addEmptyCell(row, rowPos++, getShadedStyle(level));
			addEmptyCell(row, rowPos++, getShadedStyle(level));
			addEmptyCell(row, rowPos++, getShadedStyle(level));
			addEmptyCell(row, rowPos++, getShadedStyle(level));
			addEmptyCell(row, rowPos++, getShadedStyle(level));
			addEmptyCell(row, rowPos++, getShadedStyle(level));
		}

		return rowPos;
	}

	private int displayActionNavigation(CellRefRepository cellRefRepository, ActionGraphSection section, Row row, int level, int rowPos, int prevSectionLine, int nextSectionLine, int parentSectionLine, int lastChildSectionLine) {
		int maxLevel = section.getMaxDepth()+level-1;
		
		if (section.getMaxDepth() == 1)
			addEmptyCell(row, rowPos++, getShadedStyle(section.getMaxDepth()+level-1));
		else 
			addCell(row, rowPos++, maxLevel, getShadedStyle(maxLevel), getConstantFont(CellFonts.FONT_8)); // display max depth
		addCell(row, rowPos++, level, getShadedStyle(level));
		
		int actionGraphOffset = this.sheetCfg.isFunctionGraphDisplayed() ? 1 : 0;
		actionGraphOffset = this.sheetCfg.isContentionGraphDisplayed()? actionGraphOffset + 1 : actionGraphOffset;
		int columnTarget = cellRefRepository.getActionSheetTypes().size() + 2 + actionGraphOffset;  // 2 = ID + level 
		
		// previous arrow
		if (prevSectionLine != NOT_EXIST){
			CellReference cellref = new CellReference(prevSectionLine, columnTarget, true, true);
			addCell(row, rowPos++, CellText.FONT_SYMBOL_LEFT_ARROW, cellref.formatAsString(), getShadedStyle(level), getConstantFont(FONT_SYMBOL_9));
		}
		else
			addEmptyCell(row, rowPos++, getShadedStyle(level));
		
		// parent arrow
		if (level != 1){
			CellReference cellref = new CellReference(parentSectionLine, columnTarget, true, true);
			addCell(row, rowPos++, CellText.FONT_SYMBOL_UP_ARROW, cellref.formatAsString(), getShadedStyle(level), getConstantFont(FONT_SYMBOL_9));
		}
		else
			addEmptyCell(row, rowPos++, getShadedStyle(level));
		
		// child arrow
		if (section.hasChildren()){
			CellReference cellref = new CellReference(lastChildSectionLine, columnTarget, true, true);
			addCell(row, rowPos++, CellText.FONT_SYMBOL_DOWN_ARROW, cellref.formatAsString(), getShadedStyle(level), getConstantFont(FONT_SYMBOL_9));
		}
		else
			addEmptyCell(row, rowPos++, getShadedStyle(level));
		
		// next arrow
		if (nextSectionLine != NOT_EXIST){
			CellReference cellref = new CellReference(nextSectionLine, columnTarget, true, true);
			addCell(row, rowPos++, CellText.FONT_SYMBOL_RIGHT_ARROW, cellref.formatAsString(), getShadedStyle(level), getConstantFont(FONT_SYMBOL_9));
		}
		else
			addEmptyCell(row, rowPos++, getShadedStyle(level));
		
		return rowPos;
	}

	private int displayActionLinks(CellRefRepository cellRefRepository, ThreadAction action, Row row, int level, int rowPos) {
    	for (String sheetType : cellRefRepository.getActionSheetTypes()){
    		CellReference cellRef = cellRefRepository.getCellRef(sheetType, action);
    		if (level == 1 && cellRef != null)
    			addCell(row, rowPos++, sheetType.substring(0, 1).toUpperCase(), cellRef, STYLE_CELL_LINK_LEVEL_1);
    		else
    			addEmptyCell(row, rowPos++, getShadedStyle(level));
    	}
    	
    	return rowPos;
	}

	private void displayCodeLines(Sheet sheet, CellRefRepository cellRefRepository, int pos, String rawCodeLine, String function, String operation, String contentionType) {
		int rowPos = 0;
		Row row = sheet.createRow(pos);
		String codeLine = AnalyzerHelper.stripCodeLine(rawCodeLine, javaModuleSupport && this.sheetCfg.isJavaModuleStripped());

		addEmptyCell(row, rowPos++);  // id
		
		if(this.sheetCfg.isFunctionGraphDisplayed())
			addEmptyCell(row, rowPos++);  // function graph picture
		
		if(this.sheetCfg.isContentionGraphDisplayed())
			addEmptyCell(row, rowPos++);  // contention type graph picture
		
    	// action links
		Iterator<String> iter = cellRefRepository.getActionSheetTypes().iterator();
    	while (iter.hasNext()){
        	addEmptyCell(row, rowPos++);
        	iter.next();
    	}
		
		addEmptyCell(row, rowPos++); // D
		addEmptyCell(row, rowPos++); // Level
		addEmptyCell(row, rowPos++); // Navigation
		addEmptyCell(row, rowPos++);
		addEmptyCell(row, rowPos++);
		addEmptyCell(row, rowPos++);
		
		addEmptyCell(row, rowPos++); // min start date
		addEmptyCell(row, rowPos++);
		addEmptyCell(row, rowPos++); // min end date
		addEmptyCell(row, rowPos++);
		addEmptyCell(row, rowPos++); // min duration
		addEmptyCell(row, rowPos++); // max duration
		addEmptyCell(row, rowPos++); // # hiatus
		
		if (function != null){
			addCell(row, rowPos++, codeLine, STYLE_CELL, this.sheetCfg.getFunctionColor()); // executor
			addEmptyCell(row, rowPos++, STYLE_CELL, this.sheetCfg.getFunctionColor()); // thread id
			addEmptyCell(row, rowPos++, STYLE_CELL, this.sheetCfg.getFunctionColor()); // thread name
			addEmptyCell(row, rowPos++, STYLE_CELL, this.sheetCfg.getFunctionColor()); // thread priority
			addEmptyCell(row, rowPos++, STYLE_CELL, this.sheetCfg.getFunctionColor()); // action
			addEmptyCell(row, rowPos++, STYLE_CELL, this.sheetCfg.getFunctionColor()); // count
			addEmptyCell(row, rowPos++, STYLE_CELL, this.sheetCfg.getFunctionColor()); // action %
			
	    	if (session.isCPUInfoAvailable()){
	    		addEmptyCell(row, rowPos++, STYLE_CELL, this.sheetCfg.getFunctionColor());  // CPU usage
	    		addEmptyCell(row, rowPos++, STYLE_CELL, this.sheetCfg.getFunctionColor());  // CPU standard deviation
	    		addEmptyCell(row, rowPos++, STYLE_CELL, this.sheetCfg.getFunctionColor());  // CPU time
	    	}
	    	if (session.isMemoryInfoAvailable()){
	    		addEmptyCell(row, rowPos++, STYLE_CELL, this.sheetCfg.getFunctionColor()); // Mem used 
	    		addEmptyCell(row, rowPos++, STYLE_CELL, this.sheetCfg.getFunctionColor()); // Mem standard deviation
	    	}
	    	
			addEmptyCell(row, rowPos++, STYLE_CELL, this.sheetCfg.getFunctionColor()); // Operation (principal)
			addEmptyCell(row, rowPos++, STYLE_CELL, this.sheetCfg.getFunctionColor()); // Contention type (principal)
			addCell(row, rowPos++, function, STYLE_CELL, this.sheetCfg.getFunctionColor()); // Action (all)
			addEmptyCell(row, rowPos++, STYLE_CELL, this.sheetCfg.getFunctionColor()); // Operation (all)
			addEmptyCell(row, rowPos++, STYLE_CELL, this.sheetCfg.getFunctionColor()); // Contention type (all)
		}
		else if (operation != null){
			addCell(row, rowPos++, codeLine, STYLE_CELL, this.sheetCfg.getOperationColor()); // executor
			addEmptyCell(row, rowPos++, STYLE_CELL, this.sheetCfg.getOperationColor()); // thread id
			addEmptyCell(row, rowPos++, STYLE_CELL, this.sheetCfg.getOperationColor()); // thread name
			addEmptyCell(row, rowPos++, STYLE_CELL, this.sheetCfg.getOperationColor()); // thread priority
			addEmptyCell(row, rowPos++, STYLE_CELL, this.sheetCfg.getOperationColor()); // action
			addEmptyCell(row, rowPos++, STYLE_CELL, this.sheetCfg.getOperationColor()); // count
			addEmptyCell(row, rowPos++, STYLE_CELL, this.sheetCfg.getOperationColor()); // action %
			
	    	if (session.isCPUInfoAvailable()){
	    		addEmptyCell(row, rowPos++, STYLE_CELL, this.sheetCfg.getOperationColor());  // CPU usage
	    		addEmptyCell(row, rowPos++, STYLE_CELL, this.sheetCfg.getOperationColor());  // CPU standard deviation
	    		addEmptyCell(row, rowPos++, STYLE_CELL, this.sheetCfg.getOperationColor());  // CPU time
	    	}
	    	if (session.isMemoryInfoAvailable()){
	    		addEmptyCell(row, rowPos++, STYLE_CELL, this.sheetCfg.getOperationColor()); // Mem used 
	    		addEmptyCell(row, rowPos++, STYLE_CELL, this.sheetCfg.getOperationColor()); // Mem standard deviation
	    	}
	    	
	    	addCell(row, rowPos++, operation, STYLE_CELL, this.sheetCfg.getOperationColor()); // Operation (principal)
	    	if (contentionType != null)
	    		addCell(row, rowPos++, contentionType, STYLE_CELL, this.sheetCfg.getOperationColor()); // Contention type (principal)
	    	else
	    		addEmptyCell(row, rowPos++, STYLE_CELL, this.sheetCfg.getOperationColor()); // Empty contention type (principal)
	    	addEmptyCell(row, rowPos++, STYLE_CELL, this.sheetCfg.getOperationColor()); // Contention type (principal)
			addEmptyCell(row, rowPos++, STYLE_CELL, this.sheetCfg.getOperationColor()); // Action (all)
			addEmptyCell(row, rowPos++, STYLE_CELL, this.sheetCfg.getOperationColor()); // Operation (all)
			addEmptyCell(row, rowPos++, STYLE_CELL, this.sheetCfg.getOperationColor()); // Contention type (all)
		}
		else{
			addCell(row, rowPos++, codeLine); // executor
			addEmptyCell(row, rowPos++); // thread id
			addEmptyCell(row, rowPos++); // thread name
			addEmptyCell(row, rowPos++); // thread priority
			addEmptyCell(row, rowPos++); // action
			addEmptyCell(row, rowPos++); // count
			addEmptyCell(row, rowPos++); // action %
			
	    	if (session.isCPUInfoAvailable()){
	    		addEmptyCell(row, rowPos++);  // CPU usage
	    		addEmptyCell(row, rowPos++);  // CPU standard deviation
	    		addEmptyCell(row, rowPos++);  // CPU time
	    	}
	    	if (session.isMemoryInfoAvailable()){
	    		addEmptyCell(row, rowPos++); // Mem used 
	    		addEmptyCell(row, rowPos++); // Mem standard deviation
	    	}
	    	
			addEmptyCell(row, rowPos++); // Operation (principal)
			addEmptyCell(row, rowPos++); // Contention type (principal)
			addEmptyCell(row, rowPos++); // Action (all)
			addEmptyCell(row, rowPos++); // Operation (all)
			addEmptyCell(row, rowPos++); // Contention type (all)
		}
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
				
				boolean includeStates =
						this.sheetCfg.isFunctionGraphDisplayed()
						&& (this.sheetCfg.getFunctionGraphCfg().getMode() == ConfigGraph.GENERATION_MODE.TREE 
							|| this.sheetCfg.getFunctionGraphCfg().getMode() == ConfigGraph.GENERATION_MODE.TREE_MERGE)
						&& this.sheetCfg.getFunctionGraphCfg().isThreadStateDisplayed();
				ActionGraphSection section = new ActionGraphRootSection(stack, action, includeStates);
				
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

	private int displayHeader(Sheet sheet, CellRefRepository cellRefRepository, int linePos, int rowPos) {
    	// Header
    	Row row = sheet.createRow(linePos++);

    	sheet.setColumnWidth(rowPos, 6*256);
    	addHeaderCell(row, rowPos++, "Id");

		// -----------------------------
    	// Function graph section
		// -----------------------------
    	if (this.sheetCfg.isFunctionGraphDisplayed()){
    		sheet.setColumnWidth(rowPos, 4*256);
    		addHeaderCell(row, rowPos++, "Graph", STYLE_THEME_HEADER_VERY_SMALL_ROTATED);
    	}
    	
		// -----------------------------
    	// Contention type graph section
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
    	// Navigation section
		// -----------------------------
    	
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
    	// Action section    	
		// -----------------------------
    	
    	sheet.setColumnWidth(rowPos, 45*256);
    	addHeaderCell(row, rowPos++, "Action (principal)");
    	
    	sheet.setColumnWidth(rowPos, 15*256);
    	addHeaderCell(row, rowPos++, "Count");
    	
    	sheet.setColumnWidth(rowPos, 12*256);
    	addHeaderCell(row, rowPos++, "Action %");
    	
		// -----------------------------
    	// CPU section
		// -----------------------------
    	
    	if (session.isCPUInfoAvailable()){
        	sheet.setColumnWidth(rowPos, 12*256);
        	addHeaderCell(row, rowPos++, "CPU usage");
        	
        	sheet.setColumnWidth(rowPos, 12*256);
        	addHeaderCell(row, rowPos++, 
        			concatTextAndSymbol((XSSFWorkbook) this.workbook, "CPU ", "s") // standard deviation symbol 
        			);
        	
        	sheet.setColumnWidth(rowPos, 20*256);
        	addHeaderCell(row, rowPos++, "CPU time (ms)");
    	}

		// -----------------------------
    	// Memory section
		// -----------------------------
    	
    	if (session.isMemoryInfoAvailable()){
        	sheet.setColumnWidth(rowPos, 20*256);
        	addHeaderCell(row, rowPos++, "Mem used (Mb)");
        	
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
	
	private String getFunction(ThreadAction action, ActionGraphSection section, int level) {
		if (level == 1)
			return action.getPrincipalCompositeFunction();
		
		String function = section.getFunction();
		return (function != null) ? function : "";
	}
	
	private String getOperation(ThreadAction action, ActionGraphSection section, int level) {
		if (level == 1)
			return action.getPrincipalCompositeOperation();
		
		String operation = section.getOperation();
		return (operation != null) ? operation : "";
	}
	
	private String getAllOperations(ThreadAction action, ActionGraphSection section, int level) {
		if (level == 1)
			return action.getCompositeOperation();
		
		Set<String> operations = section.getAllOperations();
		return mergeLabels(operations);
	}
	
	private String getContentionType(ThreadAction action, ActionGraphSection section, int level) {
		if (level == 1)
			return action.getPrincipalCompositeContentionType();
		
		String contentionType = section.getContentionType();
		return (contentionType != null) ? contentionType : "";
	}
	
	private String getAllContentionTypes(ThreadAction action, ActionGraphSection section, int level) {
		if (level == 1)
			return action.getCompositeContentionType();
		
		Set<String> contentionTypes = section.getAllContentionTypes();
		return mergeLabels(contentionTypes);
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
	
	private String getAllActions(ThreadAction action, ActionGraphSection section, int level) {
		if (level == 1)
			return action.getCompositeFunction();
		
		Set<String> actions = section.getAllFunctions();
		return mergeLabels(actions);
	}
	
}
