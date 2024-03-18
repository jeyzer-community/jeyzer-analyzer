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


import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.jeyzer.analyzer.config.report.ConfigGraph;
import org.jeyzer.analyzer.config.report.ConfigMonitoringSheet;
import org.jeyzer.analyzer.config.report.ConfigSheet;
import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.analyzer.data.stack.StackText;
import org.jeyzer.analyzer.error.JzrException;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.analyzer.error.JzrMonitorException;
import org.jeyzer.analyzer.output.graph.motion.ContentionGraphPlayer;
import org.jeyzer.analyzer.output.graph.motion.FunctionGraphPlayer;
import org.jeyzer.analyzer.output.graph.motion.GraphPlayer;
import org.jeyzer.analyzer.output.graph.motion.GraphSnapshot;
import org.jeyzer.analyzer.output.graph.picture.ExcelGraphPicture;
import org.jeyzer.analyzer.output.poi.CellRefRepository;
import org.jeyzer.analyzer.output.poi.CellText;
import org.jeyzer.analyzer.output.poi.context.DisplayContext;
import org.jeyzer.analyzer.output.poi.context.DisplayContext.MenuItemsContext;
import org.jeyzer.analyzer.output.poi.rule.highlight.HighLightBuilder;
import org.jeyzer.analyzer.output.poi.rule.highlight.Highlight;
import org.jeyzer.analyzer.session.JzrSession;
import org.jeyzer.analyzer.util.SystemHelper;
import org.jeyzer.monitor.engine.event.MonitorEvent;
import org.jeyzer.monitor.engine.event.MonitorSessionEvent;
import org.jeyzer.monitor.engine.event.MonitorSystemEvent;
import org.jeyzer.monitor.engine.event.MonitorTaskEvent;
import org.jeyzer.monitor.engine.event.info.Level;
import org.jeyzer.monitor.engine.rule.MonitorSessionRule;
import org.jeyzer.monitor.engine.rule.MonitorSystemRule;
import org.jeyzer.monitor.engine.rule.MonitorTaskRule;
import org.jeyzer.monitor.sticker.Sticker;
import org.jeyzer.monitor.util.MonitorHelper;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

import static org.jeyzer.analyzer.output.graph.picture.ExcelGraphPicture.GraphType.ACTION_GRAPH;
import static org.jeyzer.analyzer.output.graph.picture.ExcelGraphPicture.GraphType.CONTENTION_GRAPH;
import static org.jeyzer.analyzer.output.poi.CellText.STACK_DISPLAY_MAX_SIZE;
import static org.jeyzer.analyzer.output.poi.CellText.TRUNCATED_STACK_MESSAGE;
import static org.jeyzer.analyzer.output.poi.style.DefaultCellStyles.*;
import static org.jeyzer.analyzer.output.poi.theme.AbstractTheme.*;
import static org.jeyzer.analyzer.util.SystemHelper.JPG_EXTENSION;
import static org.jeyzer.monitor.engine.event.MonitorEvent.*;

public class MonitoringSheet extends JeyzerSheet {
	
	protected static final String NA_VALUE = "Not applicable";
	protected static final String IN_PROGRESS_VALUE = "In progress";
	private static final String ALL_SHEETS = "all";
	
	private ConfigMonitoringSheet sheetCfg;
	private boolean criticalAlert = false;
	
	protected List<Highlight> rankingHighlights = new ArrayList<>();
	
	// Used for correct comment height display
	private int lastEventLine = -1; 
	
	public MonitoringSheet(ConfigMonitoringSheet config, JzrSession session, DisplayContext displayContext) {
		super(session, displayContext);
		this.sheetCfg = config;
		this.rankingHighlights = HighLightBuilder.newInstance().buildHighLights(config.getRankingHighlights().getHighlights());
	}

	@Override
	public void display() {
		List<MonitorEvent> events;
		CellRefRepository cellRefRepository = this.displayContext.getCellRefRepository();
		List<String> sheetTypes = buildSheetTypes();
	
    	Sheet sheet = createSheet(this.sheetCfg);

		try{
			events = buildEvents();
		}
		catch(JzrException ex){
			logger.error("Failed to generate the " + sheetCfg.getName() + " sheet.", ex);
			return;
		}
		
    	// key = event
    	Map<MonitorEvent, ExcelGraphPicture> functionGraphPictures = new LinkedHashMap<MonitorEvent, ExcelGraphPicture>();
    	Map<MonitorEvent, ExcelGraphPicture> contentionGraphPictures = new LinkedHashMap<MonitorEvent, ExcelGraphPicture>();

    	buildGraphPictures(events, functionGraphPictures, contentionGraphPictures);
		
		this.itemsCount = events.size();
    	
		int linePos = 1;
    	int rowPos = displayHeaders(sheet, linePos, sheetTypes);
    	linePos++;
    	this.lastEventLine = events.size() + 2;

		for (MonitorEvent event : events){
			linePos = displayEvent(event, sheet, cellRefRepository, linePos, sheetTypes, functionGraphPictures, contentionGraphPictures);
		}
		
		if (sheetCfg.isGrouping())
			groupEvents(events, sheet);
		
		createFilters(sheet, cellRefRepository, linePos, rowPos, sheetTypes);
		
		linePos = displayGraphPictures(sheet, functionGraphPictures, contentionGraphPictures, linePos, rowPos);

		if (criticalAlert && sheetCfg.getCriticalColor() != null)
    		((XSSFSheet)sheet).setTabColor(sheetCfg.getCriticalColor());
		
    	addTopBar(sheet, 26);
    	addMenuLink(sheet, sheetCfg, STYLE_THEME_TOP_BAR_JEYZER_TITLE, 0, 1);
    	
    	close(this.sheetCfg);
	}
	
	@Override
	protected void close(ConfigSheet cfgSheet){
		super.close(cfgSheet);
		
		if (criticalAlert && sheetCfg.getCriticalColor() != null) {
			// Update the menu item with the criticality
			MenuItemsContext menuItem = this.displayContext.getMenuItems().get(cfgSheet.getName());
			menuItem.setCriticalColor(sheetCfg.getCriticalColor());
		}
	}

	private List<String> buildSheetTypes() {
		List<String> targetTypes = new ArrayList<>();
		List<String> sheetLinks = this.sheetCfg.getSequenceSheetLinks();
		Set<String> sheetTypes = this.displayContext.getCellRefRepository().getSheetTypes();
		
		if (sheetLinks.isEmpty())
			return targetTypes;
		
    	if (sheetLinks.size() == 1 
    			&& ALL_SHEETS.equalsIgnoreCase(sheetLinks.get(0))){
    		targetTypes.addAll(sheetTypes);
    		return targetTypes;
    	}
    	
    	for (String link : sheetLinks){
    		if (sheetTypes.contains(link))
    			targetTypes.add(link);
    		else
    			logger.warn("Target sheet link unknown : " + link + ". Please review the monitoring sheet configuration.");
    	}
		
		return targetTypes;
	}

	private void buildGraphPictures(List<MonitorEvent> events, Map<MonitorEvent, ExcelGraphPicture> functionGraphPictures, Map<MonitorEvent, ExcelGraphPicture> contentionGraphPictures) {
		if (this.sheetCfg.getConfigTDFunctionGraphPlayer() == null 
				&& this.sheetCfg.getConfigTDContentionGraphPlayer() == null)
			return;
		
		// generate pictures only for critical events
		if (!Level.CRITICAL.equals(this.session.getHighestEventCategory()))
			return;

		// Filter the critical events
		List<MonitorEvent> criticalEvents = new ArrayList<>();
		MonitorEvent prevEvent = null;
		for (MonitorEvent event : events){
			if (Level.CRITICAL.equals(event.getLevel())){
				if (prevEvent == null || !prevEvent.getStartDate().equals(event.getStartDate()))
					criticalEvents.add(event); // keep only 1 event per timestamp
				prevEvent = event;
			}
		}
		
		String outputDir = this.displayContext.getSetupManager().getGraphSetupMgr().getOutputRootDirectory() + "/" + this.sheetCfg.getName();
		
		if (!criticalEvents.isEmpty()){
			buildFunctionGraphPictures(criticalEvents, functionGraphPictures, outputDir);
			buildContentionGraphPictures(criticalEvents, contentionGraphPictures, outputDir);
		}
	}

	private void buildContentionGraphPictures(List<MonitorEvent> criticalEvents, Map<MonitorEvent, ExcelGraphPicture> contentionGraphPictures, String outputDir) {
		if (!this.sheetCfg.isContentionGraphDisplayed())
			return;
		
		this.sheetCfg.getConfigTDContentionGraphPlayer().getConfigPicture().setOutputDirectory(outputDir);
		GraphPlayer contentionPlayer;
		try {
			contentionPlayer = new ContentionGraphPlayer(
					this.sheetCfg.getConfigTDContentionGraphPlayer(), 
					this.sheetCfg.getName(),
					session.getThreadDumpPeriod());
		} catch (JzrInitializationException ex) {
			logger.error("Failed to generate the event contention graph pictures for the sheet : " + this.sheetCfg.getName(), ex);
			return;
		}
		
		generateGraphPictures(contentionPlayer, criticalEvents, contentionGraphPictures, outputDir, this.sheetCfg.getConfigContentionGraph());
	}

	private void buildFunctionGraphPictures(List<MonitorEvent> criticalEvents, Map<MonitorEvent, ExcelGraphPicture> functionGraphPictures, String outputDir) {
		if (!this.sheetCfg.isFunctionGraphDisplayed())
			return;
		
		this.sheetCfg.getConfigTDFunctionGraphPlayer().getConfigPicture().setOutputDirectory(outputDir);
		FunctionGraphPlayer functionPlayer;
		try {
			functionPlayer = new FunctionGraphPlayer(
						this.sheetCfg.getConfigTDFunctionGraphPlayer(),
						this.sheetCfg.getName(),
						session.getThreadDumpPeriod());
		} catch (JzrInitializationException ex) {
			logger.error("Failed to generate the event function graph pictures for the sheet : " + this.sheetCfg.getName(), ex);
			return;
		}
		
		generateGraphPictures(functionPlayer, criticalEvents, functionGraphPictures, outputDir, this.sheetCfg.getConfigFunctionGraph());
	}
	
	private void generateGraphPictures(GraphPlayer player, List<MonitorEvent> criticalEvents, Map<MonitorEvent, ExcelGraphPicture> graphPictures, String outputDir, ConfigGraph configGraph) {
		Iterator<MonitorEvent> eventIter = criticalEvents.iterator();
		MonitorEvent event = eventIter.next(); // at least 1 element
		
		int count = 0;
		for (ThreadDump dump : session.getDumps()){
			player.play(dump);
			if (event.getStartDate().equals(dump.getTimestamp())){
				
				// generate the picture
				GraphSnapshot snapshot = player.snapshot(session, false);

				if (snapshot != null && snapshot.getPicturePath() != null){
					ExcelGraphPicture picture = preparePicture(snapshot, event, count, outputDir, configGraph, player.getName()); 
					graphPictures.put(event, picture);
					this.graphItemsCount++;
					count++;
				}
				
				if (count > configGraph.getGenerationMaximum())
					return; // max reach
				
				if (eventIter.hasNext())
					event = eventIter.next();
				else
					return; // all events covered
			}
		}
	}

	private ExcelGraphPicture preparePicture(GraphSnapshot snapshot, MonitorEvent event, int count, String outputDir, ConfigGraph configGraph, String playerName) {
		// rename the snapshot picture before generating the next one
		File picture = new File(snapshot.getPicturePath());
		String targetPath = outputDir + "/" + playerName + "-" + event.getName().replace(':', '-') + "-" + count + JPG_EXTENSION;
		targetPath = SystemHelper.sanitizePathSeparators(targetPath);
		logger.info("Renaming the graph picture as : " + targetPath);
		File target = new File(targetPath);
		if (target.exists()) {
			if (!target.delete()) // remove any previous graph picture with same name
				logger.warn("Failed to delete the previous graph picture : " + targetPath);
		}
		if (!picture.renameTo(target))
			logger.warn("Failed to rename the graph picture as : " + targetPath);
		
		return new ExcelGraphPicture(targetPath, configGraph.getExcelResolution());
	}

	private void groupEvents(List<MonitorEvent> events, Sheet sheet) {
		MonitorEvent previous = null;
		int start = 2;
		int end = 2;
		boolean group = false;
		for (MonitorEvent event : events){
			if (previous != null){
				if (event.getLevel().equals(previous.getLevel())
						&& event.getName().equals(previous.getName())){
					if (!group)
						start = end - 1;
					group = true;
				}
				else{
					if (group){
						// time to create group
						group = false;
						sheet.groupRow(start, end-2);
						sheet.setRowGroupCollapsed(start, true);
					}
				}
			}
			previous = event;
			end++;
		}
		
		// if group at the end
		if (group){
			sheet.groupRow(start, end-2);
			sheet.setRowGroupCollapsed(start, true);
		}
	}

	private int displayEvent(MonitorEvent event, Sheet sheet, CellRefRepository cellRefRepository, int linePos, List<String> sheetTypes, Map<MonitorEvent, ExcelGraphPicture> functionGraphPictures, Map<MonitorEvent, ExcelGraphPicture> contentionGraphPictures) {
		int rowPos = 0;
		Row row = sheet.createRow(linePos++);
		
		List <String> params = event.getPrintableParameters();
		
		Level level = Level.getLevel(params.get(PARAM_LEVEL_VALUE_INDEX));
		
		// level color
		rowPos = displayLevelColor(level, row, rowPos);

		if (this.sheetCfg.isFunctionGraphDisplayed())
			rowPos = displayGraphPictureLink(functionGraphPictures, event, row, rowPos);

		if (this.sheetCfg.isContentionGraphDisplayed())
			rowPos = displayGraphPictureLink(contentionGraphPictures, event, row, rowPos);		
		
		// links
    	// add links to sheets
		rowPos = displaySheetLinks(event, row, rowPos, sheetTypes);

		// Displayed and used for color highlight
		String rank = event.getRank();
		
		// start date, display can be overridden
		rowPos = displayStartDate(row, rowPos, event, rank);
		
		// event id
		String eventId = params.get(PARAM_EVENT_VALUE_INDEX);
		Cell cell = addCell(row, rowPos++, eventId, STYLE_CELL_CENTERED_WRAPPED);
		setColorHighlight(cell, rank, this.rankingHighlights);

		// event ext id
		String extId = params.get(PARAM_EXT_ID_VALUE_INDEX);
		cell = addCell(row, rowPos++, Long.parseLong(extId), STYLE_CELL_CENTERED, HorizontalAlignment.CENTER);
		
		// recommendation
		String recommendation =  params.get(PARAM_RECOMMENDATION_VALUE_INDEX);
		cell = addCell(row, rowPos++, recommendation, STYLE_CELL_SMALL_TEXT_WRAPPED);
		setColorHighlight(cell, rank, this.rankingHighlights);
		
		// extra info
		rowPos = displayExtraInfo(sheet, event, params, row, rowPos, linePos);

		// rank (level + sub level)
		addCell(row, rowPos++, rank, STYLE_CELL_CENTERED);
		
		// end date, display can be overridden
		rowPos = displayEndDate(event, params, row, rowPos);
				
		// level
		addCell(row, rowPos++, level.toString(), STYLE_CELL_CENTERED);

		// scope
		String scope =  params.get(PARAM_SCOPE_VALUE_INDEX);
		addCell(row, rowPos++, scope, STYLE_CELL_CENTERED);
				
		// count
		String count = params.get(PARAM_COUNT_VALUE_INDEX);
		addCell(row, rowPos++, Long.parseLong(count), STYLE_CELL_CENTERED, HorizontalAlignment.CENTER);
		
		// action
		rowPos = displayAction(event, params, row, rowPos);

		// duration, display can be overridden
		rowPos = displayDuration(event, params, row, rowPos);
		
		// thread id
		rowPos = displayThreadId(event, params, row, rowPos);

		// ref
		String ref = params.get(PARAM_REF_VALUE_INDEX);
		addCell(row, rowPos++, ref, STYLE_CELL_CENTERED);
		
		return linePos;
	}

	// method can be overridden
	protected int displayStartDate(Row row, int rowPos, MonitorEvent event, String rank) {
		Cell cell = addCell(row, rowPos++, convertToTimeZone(event.getStartDate()), STYLE_CELL_DATE_CENTERED);
		setColorHighlight(cell, rank, this.rankingHighlights);
		return rowPos;
	}

	private int displayExtraInfo(Sheet sheet, MonitorEvent event, List<String> params, Row row, int rowPos, int linePos) {
		StringBuilder extraInfo = new StringBuilder();

		for (int i=21; i<params.size(); i++){
			extraInfo.append(params.get(i));
			if (i%2 == 0 && (i+1 != params.size()))
				extraInfo.append("\n");
			else if (i%2 == 1)
				extraInfo.append(" : ");
		}
		Cell cell = addCell(row, rowPos++, extraInfo.toString(), STYLE_CELL_SMALL_TEXT_WRAPPED);
		
		// display stack
		if (event instanceof MonitorTaskEvent){
			displayStackAsComment(sheet, cell, (MonitorTaskEvent) event, linePos);
		}

		return rowPos;
	}

	private void displayStackAsComment(Sheet sheet, Cell cell, MonitorTaskEvent taskEvent, int linePos) {
		if (taskEvent.hasStackText()){
			StackText st = taskEvent.getStackText();
			
			// Prevent this Excel warning on file opening when stack text is huge:
			//   Repaired Records: Sorting from /xl/comments20.xml part (Comments)
			String comment = 
					(st.getText().length() + st.getDepth() > STACK_DISPLAY_MAX_SIZE) ?
					    st.getText().substring(0, STACK_DISPLAY_MAX_SIZE -1 -st.getDepth()) + TRUNCATED_STACK_MESSAGE
					  : st.getText();
			
			// Challenge is to display the comment with the right depth. 
			// In POI, the bottom of the comment is specified based on a number of rows. 
			// If rows are bigger, which is the case for events, it's getting difficult 
			//   to fit the comment text correctly : large empty space at the bottom or text half displayed..
			// We can't guess the row height for events. Below is therefore an attempt to adapt.
			int deltaBeforeEnd = this.lastEventLine - linePos;
			int rowDepth = Math.round(st.getDepth() / (1 + st.getDepth()/40F)); // the smaller it is, the more we keep space
			if (linePos + rowDepth > this.lastEventLine && rowDepth > deltaBeforeEnd)
				rowDepth = st.getDepth() - deltaBeforeEnd;
			
			addComment(sheet, cell, comment, rowDepth, st.getMaxlength() / CellText.COMMENT_COLUMN_CHARS_SIZE);
		}
	}

	// method can be overridden
	protected int displayDuration(MonitorEvent event, List<String> params, Row row, int rowPos) {
		String duration =  params.get(PARAM_DURATION_VALUE_INDEX);

		if (!NA_VALUE.equalsIgnoreCase(duration))
			addCell(row, rowPos++, duration, STYLE_CELL_CENTERED);
		else
			addCell(row, rowPos++, NA_VALUE, STYLE_CELL_CENTERED_SMALL_ITALIC);

		return rowPos;
	}

	// method can be overridden
	protected int displayEndDate(MonitorEvent event, List<String> params, Row row, int rowPos) {
		String endDate =  params.get(PARAM_END_DATE_VALUE_INDEX);

		if (NA_VALUE.equalsIgnoreCase(endDate))
				addCell(row, rowPos++, NA_VALUE, STYLE_CELL_CENTERED_SMALL_ITALIC);	
		else if (IN_PROGRESS_VALUE.equalsIgnoreCase(endDate))
			addCell(row, rowPos++, IN_PROGRESS_VALUE, STYLE_CELL_CENTERED_SMALL_ITALIC);
		else 
			addCell(row, rowPos++, convertToTimeZone(event.getEndDate()), STYLE_CELL_DATE_CENTERED);

		return rowPos;
	}

	private int displaySheetLinks(MonitorEvent event, Row row, int rowPos, List<String> sheetTypes) {
		CellRefRepository cellRefRepository = this.displayContext.getCellRefRepository();
		
    	for (String sheetType : sheetTypes){
			// If the target sheet is a monitoring sequence sheet, use the event id which is more precise than thread name + start date
			CellReference cellRef = cellRefRepository.getCellRef(sheetType, event.getId());
    		
			if (cellRef==null) {
				// otherwise target sheet is task sequence sheet 
				// (.. or the monitoring sheet handles a different set of events (rules) than the sequence sheet. 
				//     Those events could match potentially events from the other side just by the date. 
				//     in such case this is a initially a configuration mistake to link the 2 sheets..
				//     otherwise solution is to keep the sequence type (actions or events) with the sheetType identifier)
				if (cellRef==null && event instanceof MonitorTaskEvent)
					// use thread name + start date
					cellRef = cellRefRepository.getCellRef(sheetType, event.getPrintableParameters().get(PARAM_THREAD_VALUE_INDEX), event.getStartDate());
	    		else if(event instanceof MonitorSessionEvent)
	    			// just use start date
	    			cellRef = cellRefRepository.getRefColumn(sheetType, event.getStartDate());
				// system is not covered which is expected
			}
    		
			if (cellRef!=null)
				addCell(row, rowPos++, sheetType.substring(0, 1).toUpperCase(), cellRef, STYLE_LINK_CENTERED);
			else
				addEmptyCell(row, rowPos++);
    	}
    	
		return rowPos;
	}
	
	private int displayAction(MonitorEvent event, List<String> params, Row row, int rowPos) {
		
		if (event instanceof MonitorTaskEvent){
			String actionId =  params.get(PARAM_ACTION_VALUE_INDEX);
			addCell(row, rowPos++, actionId, STYLE_CELL_CENTERED_WRAPPED);
		}else{
			addCell(row, rowPos++, NA_VALUE, STYLE_CELL_CENTERED_SMALL_ITALIC); // action
		}

		return rowPos;
	}
	
	private int displayThreadId(MonitorEvent event, List<String> params, Row row, int rowPos) {
		
		if (event instanceof MonitorTaskEvent){
			String threadId =  params.get(PARAM_THREAD_VALUE_INDEX);
			addCell(row, rowPos++, threadId, STYLE_CELL_CENTERED);
		}else{
			addCell(row, rowPos++, NA_VALUE, STYLE_CELL_CENTERED_SMALL_ITALIC); // threadId
		}

		return rowPos;
	}

	private int displayLevelColor(Level level, Row row, int rowPos) {
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
		
		return rowPos;
	}

	private int displayHeaders(Sheet sheet, int linePos, List<String> sheetTypes) {
    	Row row = sheet.createRow(linePos++);
    	int rowPos=0;

    	sheet.setColumnWidth(rowPos, 3*256);
    	addHeaderCell(row, rowPos++, "", STYLE_THEME_HEADER2);

    	if (this.sheetCfg.isFunctionGraphDisplayed()){
    		sheet.setColumnWidth(rowPos, 4*256);
    		addHeaderCell(row, rowPos++, "Graph", STYLE_THEME_HEADER_VERY_SMALL_ROTATED);
    	}

    	if (this.sheetCfg.isContentionGraphDisplayed()){
    		sheet.setColumnWidth(rowPos, 4*256);
    		addHeaderCell(row, rowPos++, "Cont", STYLE_THEME_HEADER_VERY_SMALL_ROTATED);
    	}
    	
		// links
    	for (String sheetType : sheetTypes){
        	sheet.setColumnWidth(rowPos, 4*256);
        	addHeaderCell(row, rowPos++, sheetType, STYLE_THEME_HEADER2_VERY_SMALL_ROTATED);
    	}
    	
    	sheet.setColumnWidth(rowPos, 17*256);
    	addHeaderCell(row, rowPos++, "Start date", STYLE_THEME_HEADER2);
        
    	sheet.setColumnWidth(rowPos, 34*256);
    	addHeaderCell(row, rowPos++, "Event", STYLE_THEME_HEADER2);
    	
    	sheet.setColumnWidth(rowPos, 6*256);
    	addHeaderCell(row, rowPos++, "Id", STYLE_THEME_HEADER2);
    	
    	sheet.setColumnWidth(rowPos, 60*256);
    	addHeaderCell(row, rowPos++, "Recommendation", STYLE_THEME_HEADER2);

    	sheet.setColumnWidth(rowPos, 30*256);
    	addHeaderCell(row, rowPos++, "Additional information", STYLE_THEME_HEADER2);
    	
    	sheet.setColumnWidth(rowPos, 9*256);
    	addHeaderCell(row, rowPos++, "Rank", STYLE_THEME_HEADER2);
    	
    	sheet.setColumnWidth(rowPos, 17*256);
    	addHeaderCell(row, rowPos++, "End date", STYLE_THEME_HEADER2);
    	
        sheet.setColumnWidth(rowPos, 15*256);
        addHeaderCell(row, rowPos++, "Level", STYLE_THEME_HEADER2);

        sheet.setColumnWidth(rowPos, 10*256);
        addHeaderCell(row, rowPos++, "Scope", STYLE_THEME_HEADER2);

        sheet.setColumnWidth(rowPos, 10*256);
        addHeaderCell(row, rowPos++, "Count", STYLE_THEME_HEADER2);
        
    	sheet.setColumnWidth(rowPos, 50*256);
    	addHeaderCell(row, rowPos++, "Action (composite)", STYLE_THEME_HEADER2);
    	
    	sheet.setColumnWidth(rowPos, 29*256);
    	addHeaderCell(row, rowPos++, "Duration (sec)", STYLE_THEME_HEADER2);

    	sheet.setColumnWidth(rowPos, 50*256);
    	addHeaderCell(row, rowPos++, "Thread", STYLE_THEME_HEADER2);
    	
    	sheet.setColumnWidth(rowPos, 25*256);
    	addHeaderCell(row, rowPos++, "Ref", STYLE_THEME_HEADER2);
    	
    	return rowPos;
	}
	
	// method can be overridden
	protected List<MonitorEvent> buildEvents() throws JzrInitializationException, JzrMonitorException {
		Map<String, Sticker> stickers = this.displayContext.getMonitoringRepository().getStickers(
				this.sheetCfg.getConfigStickers(), 
				this.session,
				this.sheetCfg.getJzrLocationResolver(),
				this.displayContext.getSetupManager().getMonitorSetupManager());
		
		Multimap<String, MonitorTaskEvent> taskEvents = this.displayContext.getMonitoringRepository().getTaskEvents(this.sheetCfg);
		if (taskEvents == null){
			List<MonitorTaskRule> taskRules = this.displayContext.getMonitoringRepository().getTaskRules(this.sheetCfg, this.session);
			taskEvents = LinkedListMultimap.create();
			session.applyMonitorStickers(taskRules, stickers);
			session.applyMonitorTaskRules(taskRules, taskEvents, this.sheetCfg.getConfigMonitorRules().getApplicativeRuleManager().getApplicativeTaskRuleFilter());
			this.displayContext.getMonitoringRepository().addTaskEvents(this.sheetCfg, taskEvents);
		}
		
		Multimap<String, MonitorSessionEvent> sessionEvents = this.displayContext.getMonitoringRepository().getSessionEvents(this.sheetCfg);
		if (sessionEvents == null){
			List<MonitorSessionRule> sessionRules = this.displayContext.getMonitoringRepository().getSessionRules(this.sheetCfg, this.session);
			sessionEvents = LinkedListMultimap.create();
			session.applyMonitorStickers(sessionRules, stickers);
			session.applyMonitorSessionRules(sessionRules, sessionEvents, this.sheetCfg.getConfigMonitorRules().getApplicativeRuleManager().getApplicativeSessionRuleFilter(), this.sheetCfg.getConfigMonitorRules().isPublisherRulesAllowed());
			this.displayContext.getMonitoringRepository().addSessionEvents(this.sheetCfg, sessionEvents);
		}
		
		Multimap<String, MonitorSystemEvent> systemEvents = this.displayContext.getMonitoringRepository().getSystemEvents(this.sheetCfg);
		if (systemEvents == null){
			List<MonitorSystemRule> systemRules = this.displayContext.getMonitoringRepository().getSystemRules(this.sheetCfg, this.session);
			systemEvents = LinkedListMultimap.create();
			session.applyMonitorStickers(systemRules, stickers);
			session.applyMonitorSystemRules(systemRules, systemEvents, this.sheetCfg.getConfigMonitorRules().getApplicativeRuleManager().getApplicativeSystemRuleFilter());
			this.displayContext.getMonitoringRepository().addSystemEvents(this.sheetCfg, systemEvents);
		}

		updateHighestEvent(taskEvents, sessionEvents, systemEvents);
		
		return MonitorHelper.buildElectedEventSortedListForReport(
				taskEvents, 
				sessionEvents, 
				systemEvents, 
				this.sheetCfg.isGroupSorting(),
				this.sheetCfg.hasDuplicateEventCleanup());
	}

	private void updateHighestEvent(
			Multimap<String, MonitorTaskEvent> taskEvents,
			Multimap<String, MonitorSessionEvent> sessionEvents,
			Multimap<String, MonitorSystemEvent> systemEvents) {
		parseEvents(Arrays.asList(taskEvents.values().toArray(new MonitorEvent[0])));
		parseEvents(Arrays.asList(sessionEvents.values().toArray(new MonitorEvent[0])));
		parseEvents(Arrays.asList(systemEvents.values().toArray(new MonitorEvent[0])));
	}

	private void parseEvents(Collection<MonitorEvent> events) {
		if (Level.CRITICAL.equals(this.session.getHighestEventCategory()))
			return; // maximum already reached
		
		for (MonitorEvent event : events){
			if (event.isElected() && event.getLevel().isMoreCritical(this.session.getHighestEventCategory()) > 0){
				this.session.setHighestEventCategory(event.getLevel());
			}
		}
	}
	
	private int displayGraphPictures(Sheet sheet, Map<MonitorEvent, ExcelGraphPicture> functionGraphPictures, Map<MonitorEvent, ExcelGraphPicture> contentionGraphPictures, int linePos, int rowPos) {
		if (!this.sheetCfg.isFunctionGraphDisplayed() && !this.sheetCfg.isContentionGraphDisplayed())
			return linePos;
		
		linePos += 10;
		rowPos += 10;
		int contentionRowPos = rowPos;
		int contentionLinePos = linePos;
		Map<MonitorEvent, Integer> lines = null;
		
		// add each graph picture
		if (this.sheetCfg.isFunctionGraphDisplayed()){
			try{
				ExcelGraphPicture candidate = null;
				lines = new HashMap<MonitorEvent, Integer>(functionGraphPictures.size());
			
				// add each graph picture
				for (Map.Entry<MonitorEvent,ExcelGraphPicture> entry : functionGraphPictures.entrySet()){
					lines.put(entry.getKey(), linePos);
					linePos = addGraphPicture(sheet, this.sheetCfg.getName(), entry.getValue(), linePos, rowPos, ACTION_GRAPH);
					candidate = entry.getValue();
				}
				
				if (candidate != null){
					contentionRowPos += candidate.getExcelWidth() + 4;  // 2 for the borders, 2 for the separation
				}				
			}
			finally{
				// clean pictures
				clearGraphPictures(functionGraphPictures.values());				
			}
		}

		// display on the right
		if (this.sheetCfg.isContentionGraphDisplayed()){
			try{
				for (Map.Entry<MonitorEvent,ExcelGraphPicture> entry : contentionGraphPictures.entrySet()){
					if (lines!= null){
						if (!lines.containsKey(entry.getKey()))
							continue; // sometimes, contention graph may be created but not the function graph : discard this case
						contentionLinePos = lines.get(entry.getKey()); // align with function picture 
					}
					
					contentionLinePos = addGraphPicture(sheet, this.sheetCfg.getName(), entry.getValue(), contentionLinePos, contentionRowPos, CONTENTION_GRAPH);
				}
			}
			finally{
				// clean pictures
				clearGraphPictures(contentionGraphPictures.values());
			}
		}
		
		if (this.sheetCfg.isContentionGraphDisplayed() && !this.sheetCfg.isFunctionGraphDisplayed())
			linePos = contentionLinePos;  // in case only contention is displayed
		
		return linePos;
	}

	private void createFilters(Sheet sheet, CellRefRepository cellRefRepository, int linePos, int rowPos, List<String> sheetTypes) {
		int linkOffset = sheetTypes.size();
		
		int graphOffset = this.sheetCfg.isFunctionGraphDisplayed()? 1 : 0;
		graphOffset = this.sheetCfg.isContentionGraphDisplayed()? graphOffset + 1 : graphOffset;
		linkOffset += graphOffset;
		
		sheet.setAutoFilter(new CellRangeAddress(1, linePos-1, 1 + linkOffset, rowPos - 1));
		sheet.createFreezePane(linkOffset + 3, 2); // left and top
	}
	
	private int displayGraphPictureLink(Map<MonitorEvent, ExcelGraphPicture> graphPictures, MonitorEvent event, Row row, int rowPos) {
		// cell link will be created at later stage, so always create empty cell to start with
		Cell cell = addEmptyCell(row, rowPos++);
		
		ExcelGraphPicture picture = graphPictures.get(event);
		if (picture != null)
			// update the picture with the parent cell that will link it
			picture.setParentLinkCell(cell);
		
		return rowPos;
	}
}
