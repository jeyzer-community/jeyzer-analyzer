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
import static org.jeyzer.analyzer.output.poi.CellText.*;
import static org.jeyzer.analyzer.output.poi.style.CellFonts.FONT_12_BOLD;
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
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jeyzer.analyzer.config.report.ConfigActionDashboardSheet;
import org.jeyzer.analyzer.config.report.ConfigGraph;
import org.jeyzer.analyzer.config.report.ConfigSheet;
import org.jeyzer.analyzer.data.ThreadAction;
import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.analyzer.data.action.ActionGraphRoot;
import org.jeyzer.analyzer.data.action.ActionGraphRootSection;
import org.jeyzer.analyzer.data.action.ActionGraphSection;
import org.jeyzer.analyzer.data.action.ActionGraphSectionComparator;
import org.jeyzer.analyzer.error.JzrException;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.analyzer.error.JzrMonitorException;
import org.jeyzer.analyzer.math.FormulaHelper;
import org.jeyzer.analyzer.output.graph.picture.ActionContentionTypeGraphGenerator;
import org.jeyzer.analyzer.output.graph.picture.ActionGraphGenerator;
import org.jeyzer.analyzer.output.graph.picture.ExcelGraphPicture;
import org.jeyzer.analyzer.output.poi.CellRefRepository;
import org.jeyzer.analyzer.output.poi.CellText;
import org.jeyzer.analyzer.output.poi.context.DisplayContext;
import org.jeyzer.analyzer.output.poi.context.DisplayContext.MenuItemsContext;
import org.jeyzer.analyzer.output.poi.rule.highlight.HighLightBuilder;
import org.jeyzer.analyzer.output.poi.rule.highlight.Highlight;
import org.jeyzer.analyzer.session.JzrSession;
import org.jeyzer.monitor.engine.event.MonitorEvent;
import org.jeyzer.monitor.engine.event.MonitorTaskEvent;
import org.jeyzer.monitor.engine.event.info.Level;
import org.jeyzer.monitor.engine.rule.MonitorTaskRule;
import org.jeyzer.monitor.sticker.Sticker;
import org.jeyzer.monitor.util.MonitorHelper;
import org.slf4j.Logger;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

public class ActionDashboardSheet extends JeyzerSheet {

	private static final int NOT_EXIST = -1;
	private static final int HEADER_SIZE = 17;
	
	private ConfigActionDashboardSheet sheetCfg;
	
	private ActionGraphRoot actionGraphRoot = null;
	private int totalActionStacks;
	private int filterEndPos;
	private boolean criticalAlert = false;
	
	protected List<Highlight> rankingHighlights = new ArrayList<>();
	
	public ActionDashboardSheet(ConfigActionDashboardSheet sheetCfg, JzrSession session, DisplayContext displayContext) {
		super(session, displayContext);
		this.sheetCfg = sheetCfg;
		this.rankingHighlights = HighLightBuilder.newInstance().buildHighLights(sheetCfg.getMonitoringConfig().getRankingHighlights().getHighlights());
	}

	@Override
	public void display() throws JzrException {
		Multimap<String, MonitorTaskEvent> events;
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

		try{
			events = buildEvents();
		}
		catch(JzrException ex){
			logger.error("Failed to generate the " + sheetCfg.getName() + " sheet.", ex);
			return;
		}
    	
    	linePos = displayActions(sheet, functionGraphPictures, contentionGraphPictures, events, linePos);
    	
    	clearGraphPictures(functionGraphPictures.values());
    	clearGraphPictures(contentionGraphPictures.values());
    	
		if (criticalAlert && sheetCfg.getMonitoringConfig().getCriticalColor() != null)
    		((XSSFSheet)sheet).setTabColor(sheetCfg.getMonitoringConfig().getCriticalColor());
    	
		// graph offset
    	sheet.setAutoFilter(new CellRangeAddress(1, linePos, 2, filterEndPos-1));
    		
    	close(this.sheetCfg);
	}
	
	@Override
	protected void close(ConfigSheet cfgSheet){
		super.close(cfgSheet);
		
		if (criticalAlert && sheetCfg.getMonitoringConfig().getCriticalColor() != null) {
			// Update the menu item with the criticality
			MenuItemsContext menuItem = this.displayContext.getMenuItems().get(cfgSheet.getName());
			menuItem.setCriticalColor(sheetCfg.getMonitoringConfig().getCriticalColor());
		}
	}

	private int displayActions(Sheet sheet, Map<String, ExcelGraphPicture> functionGraphPictures, Map<String, ExcelGraphPicture> contentionGraphPictures, Multimap<String, MonitorTaskEvent> eventsPerFunction, int linePos) throws JzrException {
    	int prevSectionLine = NOT_EXIST;
    	int nextSectionLine;
		
    	actionGraphRoot = buildActionGraph(session);
    	
    	Set<Map.Entry<String, ActionGraphSection>> rootSections = actionGraphRoot.getActionGraphSectionRoots();

    	buildGraphPictures(functionGraphPictures, contentionGraphPictures, rootSections);

    	// add empty display if no picture
    	if (functionGraphPictures.isEmpty() && contentionGraphPictures.isEmpty()){
    		// display empty message
    		displayEmpty(sheet, linePos++);
    		return linePos;
    	}
    	
    	int i=0;
    	for (Map.Entry<String, ActionGraphSection> rootSection : rootSections){
    		// get action, key and related graph section 
    		ActionGraphRootSection section = (ActionGraphRootSection)rootSection.getValue();
			
			String key = section.getExecutor() + ActionGraphRoot.EXECUTOR_ACTION_SEPARATOR + section.getFunction();
			ExcelGraphPicture functionPicture = functionGraphPictures.get(key);
			ExcelGraphPicture contentionPicture = contentionGraphPictures.get(key);
			if (functionPicture == null && contentionPicture == null)
				continue;

	    	this.itemsCount++;
			Collection<MonitorTaskEvent> events = eventsPerFunction.get(section.getFunction());
			
			int pictureRowSize = (functionPicture != null) ? functionPicture.getExcelHeight() : (contentionPicture != null) ? contentionPicture.getExcelHeight() : 0;
			int eventSize = getEventsSize(events);
			Level maxLevel = getHighestEventLevel(events);
			
    		int startLine = linePos;
    		
			if (prevSectionLine != NOT_EXIST)
				displaySeparatorLine(sheet, linePos++);
			
			// next arrows
			if (i == functionGraphPictures.size()-1)
				nextSectionLine = NOT_EXIST;
			else
				nextSectionLine = linePos + pictureRowSize + eventSize + 3; // 3 = bottom action line + separator + target lines
			
			// top action line
			displayActionLine(sheet, section, linePos++, prevSectionLine, nextSectionLine, maxLevel);
    		
    		if (eventSize>0)
    			linePos = displayEvents(sheet, linePos, section, events, maxLevel);
    		
    		if (functionPicture != null){
    			int index = addPicture(
    					sheet, 
    					functionPicture.getPicturePath(),
    					functionPicture.getIndex(),
    					0,
    					linePos++,
    					functionPicture.getExcelWidth(),
    					functionPicture.getExcelHeight()
    					);
    			if (!functionPicture.hasIndex())
    				functionPicture.setIndex(index);
    		}
    		
    		if (contentionPicture != null){
    			int index = addPicture(
    					sheet, 
    					contentionPicture.getPicturePath(),
    					contentionPicture.getIndex(),
    					9,
    					linePos-1,
    					contentionPicture.getExcelWidth(),
    					contentionPicture.getExcelHeight()
    					);
    			if (!contentionPicture.hasIndex())
    				contentionPicture.setIndex(index);
    		}
			
			linePos += pictureRowSize-1;
			
			sheet.groupRow(startLine, linePos-1);
			sheet.setRowGroupCollapsed(startLine, true);
			
			// bottom action line
			displayActionLine(sheet, section, linePos++, NOT_EXIST, NOT_EXIST, maxLevel);
			prevSectionLine =  prevSectionLine != NOT_EXIST ? startLine + 1 : startLine;
		
			i++;
    	}
    	
		return linePos;
	}

	private Level getHighestEventLevel(Collection<MonitorTaskEvent> events) {
		List<MonitorEvent> list = new ArrayList<MonitorEvent>(events);
		if (events.isEmpty()){
			return null;
		}
		else if (MonitorHelper.isEventCategoryMatched(list, Level.ERROR)){
			return Level.ERROR;
		}
		else if (MonitorHelper.isEventCategoryMatched(list, Level.CRITICAL)){
			return Level.CRITICAL;
		}
		else if (MonitorHelper.isEventCategoryMatched(list, Level.WARNING)){
			return Level.WARNING;
		}
		else if (MonitorHelper.isEventCategoryMatched(list, Level.INFO)){
			return Level.INFO;
		}
		else{
			return Level.UNKNOWN;
		}
	}

	private void displaySeparatorLine(Sheet sheet, int pos) {
		Row row = sheet.createRow(pos);
		row.setHeightInPoints(6);
		
		for (int rowPos=0; rowPos<=HEADER_SIZE; rowPos++){
			addEmptyCell(row, rowPos, STYLE_LINE_SEPARATOR);  // leaf green
		}
	}

	private int getEventsSize(Collection<MonitorTaskEvent> events) {
		if (!events.isEmpty())
			return events.size() + 1; // header
		else
			return 0;
	}

	private int displayEvents(Sheet sheet, int linePos, ActionGraphRootSection section, Collection<MonitorTaskEvent> events, Level maxLevel) {
		if (events == null || events.isEmpty())
			return linePos;
		
		int startLine = linePos;
		
		displayEventHeader(sheet, linePos++, maxLevel);
		for(MonitorTaskEvent event : events){
			displayEvent(sheet, linePos++, event);
		}
		
		sheet.groupRow(startLine, linePos-1);
		sheet.setRowGroupCollapsed(startLine, true);
		
		return linePos;
	}

	private void displayEventHeader(Sheet sheet, int pos, Level maxLevel) {
		int rowPos = 0;
		Row row = sheet.createRow(pos);
		
		if (maxLevel != null){
			displayLevelColor(maxLevel, row, rowPos++);
			displayLevelColor(maxLevel, row, rowPos++);
		}
		else{
			addEmptyCell(row, rowPos++, STYLE_CELL_LEVEL_1); // grey part first
			addEmptyCell(row, rowPos++, STYLE_CELL_LEVEL_1);
		}
		
		addCell(row, rowPos++, "Event", STYLE_CELL_SMALL_TEXT_WRAPPED);
		addCell(row, rowPos++, "Recommendation", STYLE_CELL_SMALL_TEXT_WRAPPED);
		addCell(row, rowPos++, "Level", STYLE_CELL_SMALL_TEXT_WRAPPED);
		addCell(row, rowPos++, "Rank", STYLE_CELL_SMALL_TEXT_WRAPPED);
		
		addEmptyCell(row, rowPos++);
		
		addCell(row, rowPos++, "Start time", STYLE_CELL_SMALL_TEXT_WRAPPED);
		addCell(row, rowPos++, "Duration", STYLE_CELL_SMALL_TEXT_WRAPPED);
		addCell(row, rowPos++, "Ref", STYLE_CELL_SMALL_TEXT_WRAPPED);
		
    	// complete the right part
    	while (rowPos <=  HEADER_SIZE)
    		addEmptyCell(row, rowPos++);
	}

	private void displayEvent(Sheet sheet, int pos, MonitorTaskEvent event) {
		int rowPos = 0;
		Row row = sheet.createRow(pos);
		
		displayLevelColor(event.getLevel(), row, rowPos++);  // up link
		displayLevelColor(event.getLevel(), row, rowPos++);  // down link

		String rank = event.getRank();
		
		String displayValue = event.getName();
		Cell cell = addCell(row, rowPos++, displayValue, STYLE_CELL_CENTERED_WRAPPED);
		addActionLink(cell, event);
		setColorHighlight(cell, rank, this.rankingHighlights);
		
		cell = addCell(row, rowPos++, event.getMessage(), STYLE_CELL_SMALL_TEXT_WRAPPED);
		setColorHighlight(cell, rank, this.rankingHighlights);

		addCell(row, rowPos++, event.getLevel().toString(), STYLE_CELL_CENTERED_WRAPPED);
		addCell(row, rowPos++, rank, STYLE_CELL_CENTERED_WRAPPED);
		addEmptyCell(row, rowPos++);
		addCell(row, rowPos++, convertToTimeZone(event.getStartDate()), STYLE_CELL_CENTERED_WRAPPED);
		
		addCell(row, rowPos++, event.getPrintableDuration(event.getDuration()*1000), STYLE_CELL_CENTERED_WRAPPED);

		addCell(row, rowPos++, event.getRef(), STYLE_CELL_CENTERED_WRAPPED);
		
    	// complete the right part
    	while (rowPos <=  HEADER_SIZE)
    		addEmptyCell(row, rowPos++);
	}

	private void addActionLink(Cell cell, MonitorTaskEvent event) {
		CellRefRepository cellRefRepository = this.displayContext.getCellRefRepository();
		Iterator<String> sheetTypeIter = cellRefRepository.getSheetTypes().iterator();
		
		if (!sheetTypeIter.hasNext())
			return;
		
		String sheetType = sheetTypeIter.next();
   		CellReference cellRef = cellRefRepository.getCellRef(
   				sheetType, 
   				event.getThreadName(), 
   				event.getStartDate());
   		if (cellRef!=null)
   				addDocumentHyperLink(cell, cellRef.formatAsString());
	}

	private void displayEmpty(Sheet sheet, int pos) {
		Row row = sheet.createRow(pos);
		addCell(row, 3, "No actions of interest found here. Please check the other panels.", STYLE_CELL_CENTERED_WRAPPED);
	}

	private void displayActionLine(Sheet sheet, ActionGraphRootSection section, int pos, int prevSectionLine, int nextSectionLine, Level maxLevel) {
		int rowPos = 0;
		Row row = sheet.createRow(pos);
		
		displayLink(row, rowPos++, prevSectionLine, true, maxLevel);  // up link
		displayLink(row, rowPos++, nextSectionLine, false, maxLevel); // down link
		
		addCell(row, rowPos++, section.getExecutor(), STYLE_CELL_WRAPPED_LEVEL_1);
		addCell(row, rowPos++, getFunction(section), STYLE_CELL_WRAPPED_LEVEL_1, getConstantFont(FONT_12_BOLD));
		
		addCell(row, rowPos++, section.getActionCount(), STYLE_CELL_LEVEL_1); // action %
		addCell(row, rowPos++, FormulaHelper.percentNull(section.getActionCount(), session.getActionsSize()), STYLE_CELL_LEVEL_1); // action %
		
		addCell(row, rowPos++, section.getStackCount(), STYLE_CELL_LEVEL_1);
		addCell(row, rowPos++, FormulaHelper.percentNull(section.getStackCount(), totalActionStacks), STYLE_CELL_LEVEL_1); // global %
		
		addCell(row, rowPos++, getOperation(section), STYLE_CELL_LEVEL_1);
		addCell(row, rowPos++, getContentionType(section), STYLE_CELL_LEVEL_1);
		
		// -----------------------------
		// CPU
		// -----------------------------
		rowPos = displayActionCPUInfo(section, row, rowPos);
    	
		// -----------------------------
		// Memory section
		// -----------------------------
		rowPos = displayActionMemoryInfo(section, row, rowPos);
		
    	// complete the right part
    	while (rowPos <=  HEADER_SIZE)
    		addEmptyCell(row, rowPos++, STYLE_CELL_LEVEL_1);
	}
	
	private void displayLevelColor(Level level, Row row, int rowPos) {
		if (Level.CRITICAL.equals(level)){
			addEmptyCell(row, rowPos++, STYLE_CELL_LEVEL_CRITICAL);
			criticalAlert = true; 
		}
		else if (Level.ERROR.equals(level))
			addEmptyCell(row, rowPos++, STYLE_CELL_LEVEL_ERROR);
		else if (Level.WARNING.equals(level))
			addEmptyCell(row, rowPos++, STYLE_CELL_LEVEL_WARNING);
		else 
			addEmptyCell(row, rowPos++, STYLE_CELL_LEVEL_INFO);
	}

	private void displayLink(Row row, int pos, int targetLine, boolean up, Level maxLevel) {
		if (targetLine != NOT_EXIST){
			CellReference cellref = new CellReference(targetLine, pos, true, true);
			String style;
			if (maxLevel != null){
				if (Level.CRITICAL.equals(maxLevel))
					style = STYLE_CELL_LEVEL_CRITICAL;
				else if (Level.ERROR.equals(maxLevel))
					style = STYLE_CELL_LEVEL_ERROR;
				else if (Level.WARNING.equals(maxLevel))
					style = STYLE_CELL_LEVEL_WARNING;
				else 
					style = STYLE_CELL_LEVEL_INFO;
			}
			else
				style = STYLE_CELL_LINK_LEVEL_1;
			addCell(row, pos, up? CellText.FONT_SYMBOL_UP_ARROW : CellText.FONT_SYMBOL_DOWN_ARROW, cellref.formatAsString(), style, getConstantFont(FONT_SYMBOL_9));
		}else{
			if (maxLevel != null)
				displayLevelColor(maxLevel, row, pos);
			else
				addEmptyCell(row, pos, STYLE_CELL_LEVEL_1);
		}
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

	private int displayHeader(Sheet sheet, int linePos, int rowPos) {
    	// Header
    	Row row = sheet.createRow(linePos++);
    	
    	sheet.setColumnWidth(rowPos, 2*256);
    	addHeaderCell(row, rowPos++, "");

    	sheet.setColumnWidth(rowPos, 2*256);
    	addHeaderCell(row, rowPos++, "");
        
    	sheet.setColumnWidth(rowPos, 45*256);
    	addHeaderCell(row, rowPos++, "Executor");
        
    	sheet.setColumnWidth(rowPos, 45*256);
    	addHeaderCell(row, rowPos++, "Action (principal)");
    	
    	sheet.setColumnWidth(rowPos, 12*256);
    	addHeaderCell(row, rowPos++, "Action Count");
    	
    	sheet.setColumnWidth(rowPos, 12*256);
    	addHeaderCell(row, rowPos++, "Action %");
    	
    	sheet.setColumnWidth(rowPos, 12*256);
    	addHeaderCell(row, rowPos++, "Stack count");

    	sheet.setColumnWidth(rowPos, 12*256);
    	addHeaderCell(row, rowPos++, "Stack %");
    	
    	sheet.setColumnWidth(rowPos, 32*256);
    	addHeaderCell(row, rowPos++, "Operation (principal)");
    	
    	sheet.setColumnWidth(rowPos, 32*256);
    	addHeaderCell(row, rowPos++, "Contention type (principal)");
    	
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
    	
    	filterEndPos = rowPos;
    	
    	// complete the right part
    	while (rowPos <=  HEADER_SIZE)
    		addHeaderCell(row, rowPos++, "");
    	
		return rowPos;
	}

	@Override
	protected Logger getLogger(){
		return logger;
	}
	
	private int displayActionMemoryInfo(ActionGraphSection section, Row row, int rowPos) {
    	if (session.isMemoryInfoAvailable()){
    		// Allocated memory average    		
    		long memoryUsage = section.getAllocatedMemoryAverage();
    		if (memoryUsage != -1)
    			addCell(row, rowPos++, convertToMb(memoryUsage), STYLE_CELL_LEVEL_1);
    		else
    			addCell(row, rowPos++, "NA", STYLE_CELL_LEVEL_1);
    		
    		// Allocated memory standard deviation
    		double memoryUsageStdDeviation = section.getAllocatedMemoryStdDeviation();
    		if (Double.doubleToRawLongBits(memoryUsageStdDeviation) != DOUBLE_TO_LONG_NA)
    			addCell(row, rowPos++, convertToMb(memoryUsageStdDeviation), STYLE_CELL_LEVEL_1);
    		else
    			addCell(row, rowPos++, "NA", STYLE_CELL_LEVEL_1);
    	}

		return rowPos;
	}

	private int displayActionCPUInfo(ActionGraphSection section, Row row, int rowPos) {
    	if (session.isCPUInfoAvailable()){
    		// CPU usage average
    		int cpuUsage = section.getCpuUsageAverage();
    		if (cpuUsage != -1)
    			addCell(row, rowPos++, cpuUsage, STYLE_CELL_LEVEL_1);
    		else
    			addCell(row, rowPos++, "NA", STYLE_CELL_LEVEL_1);

    		// CPU usage standard deviation
    		double cpuUsageStdDeviation = section.getCpuUsageStdDeviation();
    		if (Double.doubleToRawLongBits(cpuUsageStdDeviation) != DOUBLE_TO_LONG_NA)
    			addCell(row, rowPos++, cpuUsageStdDeviation, STYLE_CELL_LEVEL_1);
    		else
    			addCell(row, rowPos++, "NA", STYLE_CELL_LEVEL_1);
    	}
    	
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

	private ActionGraphRoot buildActionGraph(JzrSession session) {
		if (this.actionGraphRoot != null)
			return this.actionGraphRoot;
		
		boolean includeStates = 
				(this.sheetCfg.getFunctionGraphCfg().getMode() == ConfigGraph.GENERATION_MODE.TREE 
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

	private Multimap<String, MonitorTaskEvent> buildEvents() throws JzrInitializationException, JzrMonitorException {
		Multimap<String, MonitorTaskEvent> taskEvents = this.displayContext.getMonitoringRepository().getTaskEvents(this.sheetCfg.getMonitoringConfig());
		if (taskEvents == null){
			List<MonitorTaskRule> taskRules = this.displayContext.getMonitoringRepository().getTaskRules(this.sheetCfg.getMonitoringConfig(), this.session);
			Map<String, Sticker> stickers = this.displayContext.getMonitoringRepository().getStickers(
					this.sheetCfg.getMonitoringConfig().getConfigStickers(), 
					this.session,
					this.sheetCfg.getMonitoringConfig().getJzrLocationResolver(),
					this.displayContext.getSetupManager().getMonitorSetupManager());
			
			taskEvents = LinkedListMultimap.create();
			session.applyMonitorStickers(taskRules, stickers);
			session.applyMonitorTaskRules(taskRules, taskEvents, this.sheetCfg.getMonitoringConfig().getConfigMonitorRules().getApplicativeRuleManager().getApplicativeTaskRuleFilter());
			this.displayContext.getMonitoringRepository().addTaskEvents(this.sheetCfg.getMonitoringConfig(), taskEvents);
		}
		
		if (this.sheetCfg.getMonitoringConfig().hasDuplicateEventCleanup())
			MonitorHelper.filterDuplicateTaskEvents(taskEvents);
		
		// sort
		Multimap<String, MonitorEvent> events = LinkedListMultimap.create();
		for (Entry<String, MonitorTaskEvent> entry : taskEvents.entries()){
			events.put(entry.getKey(), entry.getValue());
		}

		List<MonitorEvent> sortedEvents = MonitorHelper.buildElectedEventSortedList(events, this.sheetCfg.getMonitoringConfig().isGroupSorting());
		taskEvents.clear();
		for (MonitorEvent sortedEvent : sortedEvents){
			taskEvents.put(((MonitorTaskEvent)sortedEvent).getPrincipalFunction(), (MonitorTaskEvent)sortedEvent);
		}
		
		return taskEvents;
	}
	
}
